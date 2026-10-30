package com.livebid.auction.controller;

import com.livebid.auction.entity.Auction;
import com.livebid.auction.service.AuctionService;
import com.livebid.bidding.repository.BidRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.transaction.annotation.Transactional;


@Controller
public class AuctionPageController {

    private final AuctionService auctionService;
    private final BidRepository bidRepository;

    public AuctionPageController(
            AuctionService auctionService,
            BidRepository bidRepository
    ) {
        this.auctionService = auctionService;
        this.bidRepository = bidRepository;
    }

    @Transactional(readOnly = true)
    @GetMapping("/auctions/{id}")
    public String auctionDetails(
            @PathVariable Long id,
            Model model
    ) {
        Auction auction = auctionService.getById(id);

        Page<com.livebid.bidding.entity.Bid> bidPage =
                bidRepository.findByAuctionIdOrderByAmountDesc(
                        id,
                        PageRequest.of(0, 20)
                );

        var bids = bidPage.getContent();

        model.addAttribute("auction", auction);
        model.addAttribute("bids", bids);
        model.addAttribute(
                "totalBids",
                bidRepository.countByAuctionId(id)
        );

        if (bids.isEmpty()) {
            model.addAttribute("currentHighestBid", auction.getStartingPrice());
            model.addAttribute("leadingBidder", null);
        } else {
            var highestBid = bids.get(0);

            model.addAttribute("currentHighestBid", highestBid.getAmount());
            model.addAttribute(
                    "leadingBidder",
                    highestBid.getBidder().getDisplayName()
            );
        }

        return "auction-details";
    }
}