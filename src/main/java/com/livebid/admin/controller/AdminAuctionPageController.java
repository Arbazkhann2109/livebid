package com.livebid.admin.controller;

import com.livebid.auction.entity.Auction;
import com.livebid.auction.repository.AuctionRepository;
import com.livebid.auction.service.AuctionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.livebid.bidding.repository.BidRepository;

@Controller
@RequestMapping("/admin/auctions")
public class AdminAuctionPageController {

    private final AuctionRepository auctionRepository;
    private final AuctionService auctionService;
    private final BidRepository bidRepository;

    public AdminAuctionPageController(
            AuctionRepository auctionRepository,
            AuctionService auctionService,
            BidRepository bidRepository
    ) {
        this.auctionRepository = auctionRepository;
        this.auctionService = auctionService;
        this.bidRepository = bidRepository;
    }

    @GetMapping
    public String auctions(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "0") int page,
            Model model
    ) {
        int pageNumber = Math.max(page, 0);

        PageRequest pageable = PageRequest.of(
                pageNumber,
                20,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<Auction> auctions;

        if (search.isBlank()) {
            auctions = auctionRepository.findAllWithSeller(pageable);
        } else {
            auctions = auctionRepository.findByTitleContainingIgnoreCase(
                    search.trim(),
                    pageable
            );
        }

        model.addAttribute("auctions", auctions);
        model.addAttribute("search", search);
        model.addAttribute("currentPage", pageNumber);

        return "admin/auctions";
    }

    @PostMapping("/{auctionId}/cancel")
    public String cancelAuction(
            @PathVariable Long auctionId,
            RedirectAttributes redirectAttributes
    ) {
        auctionService.cancelAuction(auctionId);

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Auction cancelled successfully."
        );

        return "redirect:/admin/auctions";
    }


    @GetMapping("/{auctionId}")
    public String inspectAuction(
            @PathVariable Long auctionId,
            Model model
    ) {
        Auction auction = auctionRepository.findWithDetailsById(auctionId)
                .orElseThrow(() -> new com.livebid.common.exception.ResourceNotFoundException(
                        "Auction not found: " + auctionId
                ));

        PageRequest pageable = PageRequest.of(
                0,
                100,
                Sort.by(Sort.Direction.DESC, "amount")
        );

        model.addAttribute("auction", auction);
        model.addAttribute(
                "bids",
                bidRepository.findByAuctionIdOrderByAmountDesc(auctionId, pageable)
        );

        return "admin/auction-details";
    }

}