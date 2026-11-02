
package com.livebid.auction.service;

import com.livebid.auction.entity.Auction;
import com.livebid.auction.repository.AuctionRepository;
import com.livebid.bidding.repository.BidRepository;
import com.livebid.common.exception.ResourceNotFoundException;
import com.livebid.user.entity.User;
import com.livebid.user.service.UserService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class AuctionService {

    private static final Set<String> SUPPORTED_CURRENCIES =
            Set.of("INR", "USD", "EUR");

    private final AuctionRepository auctionRepository;
    private final UserService userService;
    private final BidRepository bidRepository;

    public AuctionService(
            AuctionRepository auctionRepository,
            UserService userService,
            BidRepository bidRepository
    ) {
        this.auctionRepository = auctionRepository;
        this.userService = userService;
        this.bidRepository = bidRepository;
    }

    public Auction getById(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Auction ID must be positive.");
        }

        return auctionRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Auction not found: " + id));
    }

    public Page<Auction> getAuctions(Pageable pageable) {
        return getPublicAuctions(pageable);
    }

    public Page<Auction> getPublicAuctions(Pageable pageable) {
        return auctionRepository.findByStatusIn(
                List.of("SCHEDULED", "LIVE", "ENDED"),
                pageable
        );
    }

    public Page<Auction> getMyAuctions(Long sellerId, Pageable pageable) {
        if (sellerId == null || sellerId <= 0) {
            throw new IllegalArgumentException("Seller ID must be positive.");
        }

        return auctionRepository.findBySellerId(sellerId, pageable);
    }

    private void selectWinner(Auction auction) {
        var winningBid = bidRepository
                .findFirstByAuctionIdOrderByAmountDescPlacedAtAscIdAsc(
                        auction.getId()
                )
                .orElse(null);

        auction.setWinningBid(winningBid);
    }

    @Transactional
    public Auction createAuction(
            Long sellerId,
            String title,
            String description,
            BigDecimal startingPrice,
            String currency,
            LocalDateTime startsAt,
            LocalDateTime endsAt
    ) {
        if (sellerId == null || sellerId <= 0) {
            throw new IllegalArgumentException("Seller ID must be positive.");
        }

        if (title == null || title.isBlank() || title.trim().length() > 200) {
            throw new IllegalArgumentException("Title must contain 1–200 characters.");
        }

        if (startingPrice == null || startingPrice.signum() < 0
                || startingPrice.scale() > 2
                || startingPrice.precision() > 13) {
            throw new IllegalArgumentException(
                    "Starting price must be non-negative with at most 2 decimal places.");
        }

        if (currency == null || !SUPPORTED_CURRENCIES.contains(
                currency.trim().toUpperCase(Locale.ROOT))) {
            throw new IllegalArgumentException("Unsupported currency.");
        }

        if (startsAt == null || endsAt == null || !endsAt.isAfter(startsAt)) {
            throw new IllegalArgumentException("End time must be after start time.");
        }

        User seller = userService.getById(sellerId);

        if (!"ACTIVE".equals(seller.getStatus())) {
            throw new IllegalArgumentException("Seller account is not active.");
        }

        Auction auction = Auction.createDraft(
                seller,
                title.trim(),
                description,
                startingPrice,
                currency.trim().toUpperCase(Locale.ROOT),
                startsAt,
                endsAt
        );

        return auctionRepository.save(auction);
    }

    @Transactional
    public Auction scheduleAuction(Long auctionId, Long sellerId) {
        if (auctionId == null || auctionId <= 0) {
            throw new IllegalArgumentException("Auction ID must be positive.");
        }

        if (sellerId == null || sellerId <= 0) {
            throw new IllegalArgumentException("Seller ID must be positive.");
        }

        Auction auction = auctionRepository.findByIdForUpdate(auctionId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Auction not found: " + auctionId));

        if (!auction.getSeller().getId().equals(sellerId)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Only the auction seller can schedule this auction.");
        }

        if (!"DRAFT".equals(auction.getStatus())) {
            throw new IllegalStateException(
                    "Only draft auctions can be scheduled.");
        }

        LocalDateTime now = LocalDateTime.now();

        if (!auction.getStartsAt().isAfter(now)) {
            throw new IllegalStateException(
                    "Auction start time must be in the future.");
        }

        auction.setStatus("SCHEDULED");

        return auctionRepository.save(auction);
    }

    @Transactional
    public void activateScheduledAuction(Long auctionId) {
        Auction auction = auctionRepository.findByIdForUpdate(auctionId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Auction not found: " + auctionId));

        LocalDateTime now = LocalDateTime.now();

        if (!"SCHEDULED".equals(auction.getStatus())
                || auction.getStartsAt().isAfter(now)) {
            return;
        }

        if (!auction.getEndsAt().isAfter(now)) {
            selectWinner(auction);
            auction.setStatus("ENDED");
        } else {
            auction.setStatus("LIVE");
        }

        auctionRepository.save(auction);
    }

    @Transactional
    public void endExpiredAuction(Long auctionId) {
        Auction auction = auctionRepository.findByIdForUpdate(auctionId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Auction not found: " + auctionId));

        if ("LIVE".equals(auction.getStatus())
                && !auction.getEndsAt().isAfter(LocalDateTime.now())) {

            selectWinner(auction);
            auction.setStatus("ENDED");

            auctionRepository.save(auction);
        }
    }

    @Transactional
    public void cancelAuction(Long auctionId) {

        if (auctionId == null || auctionId <= 0) {
            throw new IllegalArgumentException(
                    "Auction ID must be positive."
            );
        }

        Auction auction = auctionRepository.findByIdForUpdate(auctionId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Auction not found: " + auctionId
                        )
                );

        String status = auction.getStatus();

        if (!"DRAFT".equals(status)
                && !"SCHEDULED".equals(status)
                && !"LIVE".equals(status)) {

            throw new IllegalStateException(
                    "Only draft, scheduled, or live auctions can be cancelled."
            );
        }

        auction.setStatus("CANCELLED");

        auctionRepository.save(auction);
    }

}
