
package com.livebid.bidding.service;

import com.livebid.auction.entity.Auction;
import com.livebid.auction.repository.AuctionRepository;
import com.livebid.bidding.BidWebSocketPublisher;
import com.livebid.bidding.entity.Bid;
import com.livebid.bidding.repository.BidRepository;
import com.livebid.common.exception.ResourceNotFoundException;
import com.livebid.user.entity.User;
import com.livebid.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.livebid.bidding.dto.BidUpdateMessage;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class BiddingService {

    private final AuctionRepository auctionRepository;
    private final BidRepository bidRepository;
    private final UserService userService;
    private final BidWebSocketPublisher bidWebSocketPublisher;

    public BiddingService(
            AuctionRepository auctionRepository,
            BidRepository bidRepository,
            UserService userService,
            BidWebSocketPublisher bidWebSocketPublisher
    ) {
        this.auctionRepository = auctionRepository;
        this.bidRepository = bidRepository;
        this.userService = userService;
        this.bidWebSocketPublisher = bidWebSocketPublisher;
    }


    @Transactional
    public Bid placeBid(Long auctionId, Long bidderId, BigDecimal amount) {
        if (auctionId == null || auctionId <= 0) {
            throw new IllegalArgumentException("Auction ID must be positive.");
        }

        if (bidderId == null || bidderId <= 0) {
            throw new IllegalArgumentException("Bidder ID must be positive.");
        }

        validateAmount(amount);

        Auction auction = auctionRepository.findByIdForUpdate(auctionId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Auction not found: " + auctionId));

        User bidder = userService.getById(bidderId);

        if (!"LIVE".equals(auction.getStatus())) {
            throw new IllegalStateException("Auction is not live.");
        }

        LocalDateTime now = LocalDateTime.now();

        if (now.isBefore(auction.getStartsAt())) {
            throw new IllegalStateException("Auction has not started.");
        }

        if (!now.isBefore(auction.getEndsAt())) {
            throw new IllegalStateException("Auction has ended.");
        }

        if (!"ACTIVE".equals(bidder.getStatus())) {
            throw new IllegalStateException("Bidder account is not active.");
        }

        if (auction.getSeller().getId().equals(bidder.getId())) {
            throw new IllegalArgumentException("Sellers cannot bid on their own auctions.");
        }

        BigDecimal minimumAmount = auction.getStartingPrice();

        var highestBid = bidRepository.findTopByAuctionIdOrderByAmountDesc(auctionId);

        if (highestBid.isPresent()) {
            minimumAmount = highestBid.get().getAmount();
        }

        if (amount.compareTo(minimumAmount) <= 0) {
            throw new IllegalStateException(
                    "Bid must be greater than the current minimum: " + minimumAmount);
        }

        Bid bid = Bid.place(auction, bidder, amount);
        Bid savedBid = bidRepository.save(bid);

        BidUpdateMessage message = new BidUpdateMessage(
                auction.getId(),
                savedBid.getId(),
                bidder.getId(),
                bidder.getDisplayName(),
                savedBid.getAmount(),
                savedBid.getPlacedAt(),
                bidRepository.countByAuctionId(auction.getId())
        );

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            bidWebSocketPublisher.publishBid(message);
                        }
                    }
            );
        }

        return savedBid;
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null
                || amount.signum() <= 0
                || amount.scale() > 2
                || amount.precision() > 13) {
            throw new IllegalArgumentException(
                    "Bid amount must be positive, fit DECIMAL(13,2), and have at most two decimal places.");
        }
    }
}
