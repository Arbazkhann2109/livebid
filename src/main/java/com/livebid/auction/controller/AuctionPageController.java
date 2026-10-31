
package com.livebid.auction.controller;

import com.livebid.auction.dto.CreateAuctionRequest;
import com.livebid.auction.entity.Auction;
import com.livebid.auction.service.AuctionService;
import com.livebid.bidding.entity.Bid;
import com.livebid.bidding.repository.BidRepository;
import com.livebid.user.entity.User;
import com.livebid.user.service.UserService;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuctionPageController {

    private final AuctionService auctionService;
    private final BidRepository bidRepository;
    private final UserService userService;

    public AuctionPageController(
            AuctionService auctionService,
            BidRepository bidRepository,
            UserService userService
    ) {
        this.auctionService = auctionService;
        this.bidRepository = bidRepository;
        this.userService = userService;
    }

    // AUCTION DETAILS PAGE

    @Transactional(readOnly = true)
    @GetMapping("/auctions/{id}")
    public String auctionDetails(
            @PathVariable Long id,
            Authentication authentication,
            Model model
    ) {
        Auction auction = auctionService.getById(id);

        Page<Bid> bidPage =
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
        var winningBid = auction.getWinningBid();

        model.addAttribute("winningBid", winningBid);

        model.addAttribute(
                "winnerName",
                winningBid != null
                        ? winningBid.getBidder().getDisplayName()
                        : null
        );

        model.addAttribute(
                "winningAmount",
                winningBid != null
                        ? winningBid.getAmount()
                        : null
        );

        // Check whether the logged-in user owns this auction.
        boolean isAuctionOwner = false;

        if (isAuthenticated(authentication)) {
            User currentUser = userService.getByEmail(
                    authentication.getName()
            );

            isAuctionOwner = auction.getSeller().getId()
                    .equals(currentUser.getId());
        }

        model.addAttribute("isAuctionOwner", isAuctionOwner);

        if (bids.isEmpty()) {
            model.addAttribute(
                    "currentHighestBid",
                    auction.getStartingPrice()
            );

            model.addAttribute("leadingBidder", null);

        } else {
            var highestBid = bids.get(0);

            model.addAttribute(
                    "currentHighestBid",
                    highestBid.getAmount()
            );

            model.addAttribute(
                    "leadingBidder",
                    highestBid.getBidder().getDisplayName()
            );
        }

        return "auction-details";
    }

    // SHOW CREATE AUCTION PAGE

    @GetMapping("/auctions/new")
    public String showCreateAuctionForm(
            Authentication authentication,
            Model model
    ) {
        if (!isAuthenticated(authentication)) {
            return "redirect:/login";
        }

        model.addAttribute(
                "createAuctionRequest",
                new CreateAuctionRequest(
                        null, null, null, "INR", null, null
                )
        );

        return "create-auction";
    }

    // CREATE AUCTION

    @PostMapping("/auctions/create")
    public String createAuction(
            @Valid @ModelAttribute("createAuctionRequest")
            CreateAuctionRequest request,
            BindingResult bindingResult,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (!isAuthenticated(authentication)) {
            return "redirect:/login";
        }

        if (bindingResult.hasErrors()) {
            return "create-auction";
        }

        try {
            User seller = userService.getByEmail(
                    authentication.getName()
            );

            Auction auction = auctionService.createAuction(
                    seller.getId(),
                    request.title(),
                    request.description(),
                    request.startingPrice(),
                    request.currency(),
                    request.startsAt(),
                    request.endsAt()
            );

            redirectAttributes.addFlashAttribute(
                    "auctionCreated",
                    "Your auction was created successfully!"
            );

            return "redirect:/auctions/" + auction.getId();

        } catch (IllegalArgumentException exception) {

            model.addAttribute(
                    "pageError",
                    exception.getMessage()
            );

            return "create-auction";
        }
    }

    // SCHEDULE AUCTION

    @PostMapping("/auctions/{id}/schedule")
    public String scheduleAuction(
            @PathVariable Long id,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        if (!isAuthenticated(authentication)) {
            return "redirect:/login";
        }

        try {
            User seller = userService.getByEmail(
                    authentication.getName()
            );

            auctionService.scheduleAuction(
                    id,
                    seller.getId()
            );

            redirectAttributes.addFlashAttribute(
                    "scheduleSuccess",
                    "Your auction has been scheduled successfully!"
            );

        } catch (IllegalStateException exception) {

            redirectAttributes.addFlashAttribute(
                    "scheduleError",
                    exception.getMessage()
            );
        }

        return "redirect:/auctions/" + id;
    }

    // AUTHENTICATION CHECK

    private boolean isAuthenticated(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)
                && authentication.getName() != null;
    }
}
