
package com.livebid.bidding.controller;

import com.livebid.auction.service.AuctionService;
import com.livebid.bidding.dto.BidResponse;
import com.livebid.bidding.dto.PlaceBidRequest;
import com.livebid.bidding.entity.Bid;
import com.livebid.bidding.repository.BidRepository;
import com.livebid.bidding.service.BiddingService;
import com.livebid.user.entity.User;
import com.livebid.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auctions/{auctionId}/bids")
public class BiddingController {

    private static final int MAX_PAGE_SIZE = 100;

    private final BidRepository bidRepository;
    private final AuctionService auctionService;
    private final BiddingService biddingService;
    private final UserService userService;

    public BiddingController(
            BidRepository bidRepository,
            AuctionService auctionService,
            BiddingService biddingService,
            UserService userService) {
        this.bidRepository = bidRepository;
        this.auctionService = auctionService;
        this.biddingService = biddingService;
        this.userService = userService;
    }

    @GetMapping
    public Page<BidResponse> listBids(
            @PathVariable Long auctionId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        if (auctionId <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Auction ID must be positive.");
        }

        if (page < 0 || size < 1 || size > MAX_PAGE_SIZE) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Page must be non-negative and size must be between 1 and 100.");
        }

        auctionService.getById(auctionId);

        Page<Bid> bids = bidRepository.findByAuctionIdOrderByAmountDesc(
                auctionId,
                PageRequest.of(
                        page,
                        size,
                        Sort.by(Sort.Direction.DESC, "amount")));

        return bids.map(BidResponse::from);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BidResponse placeBid(
            @PathVariable Long auctionId,
            @Valid @RequestBody PlaceBidRequest request,
            Authentication authentication) {

        if (auctionId <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Auction ID must be positive.");
        }

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authentication required.");
        }

        User bidder = userService.getByEmail(authentication.getName());

        Bid bid = biddingService.placeBid(
                auctionId,
                bidder.getId(),
                request.amount());

        return BidResponse.from(bid);
    }
}
