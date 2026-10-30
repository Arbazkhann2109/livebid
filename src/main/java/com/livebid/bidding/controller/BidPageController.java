
package com.livebid.bidding.controller;

import com.livebid.bidding.service.BiddingService;
import com.livebid.user.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
public class BidPageController {

    private final BiddingService biddingService;
    private final UserService userService;

    public BidPageController(
            BiddingService biddingService,
            UserService userService
    ) {
        this.biddingService = biddingService;
        this.userService = userService;
    }

    @PostMapping("/auctions/{auctionId}/bids")
    public String placeBid(
            @PathVariable Long auctionId,
            @RequestParam BigDecimal amount,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null) {
            redirectAttributes.addFlashAttribute(
                    "bidError",
                    "Please log in before placing a bid."
            );
            return "redirect:/login";
        }

        try {
            var bidder = userService.getByEmail(authentication.getName());

            biddingService.placeBid(
                    auctionId,
                    bidder.getId(),
                    amount
            );

            redirectAttributes.addFlashAttribute(
                    "bidSuccess",
                    "Your bid was placed successfully."
            );
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute(
                    "bidError",
                    ex.getMessage()
            );
        }

        return "redirect:/auctions/" + auctionId;
    }
}
