
package com.livebid.auction.controller;

import com.livebid.auction.entity.Auction;
import com.livebid.auction.service.AuctionService;
import com.livebid.user.entity.User;
import com.livebid.user.service.UserService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class MyAuctionsController {

    private final AuctionService auctionService;
    private final UserService userService;

    public MyAuctionsController(
            AuctionService auctionService,
            UserService userService
    ) {
        this.auctionService = auctionService;
        this.userService = userService;
    }

    @GetMapping("/my-auctions")
    public String myAuctions(
            Authentication authentication,
            @RequestParam(defaultValue = "ALL") String status,
            Model model
    ) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return "redirect:/login";
        }

        User user = userService.getByEmail(authentication.getName());

        List<String> allowedStatuses = List.of(
                "ALL", "DRAFT", "SCHEDULED", "LIVE", "ENDED", "CANCELLED"
        );

        if (!allowedStatuses.contains(status)) {
            status = "ALL";
        }
        final String selectedStatus = status;

        Page<Auction> auctions = auctionService.getMyAuctions(
                user.getId(),
                PageRequest.of(
                        0,
                        50,
                        Sort.by(Sort.Direction.DESC, "createdAt")
                )
        );

        List<Auction> filteredAuctions = auctions.getContent()
                .stream()
                .filter(auction ->
                        "ALL".equals(selectedStatus)
                                || selectedStatus.equals(auction.getStatus())
                )
                .toList();

        model.addAttribute("auctions", filteredAuctions);
        model.addAttribute("selectedStatus", selectedStatus);
        model.addAttribute("totalAuctions", auctions.getTotalElements());

        model.addAttribute(
                "draftCount",
                auctions.getContent().stream()
                        .filter(a -> "DRAFT".equals(a.getStatus())).count()
        );

        model.addAttribute(
                "scheduledCount",
                auctions.getContent().stream()
                        .filter(a -> "SCHEDULED".equals(a.getStatus())).count()
        );

        model.addAttribute(
                "liveCount",
                auctions.getContent().stream()
                        .filter(a -> "LIVE".equals(a.getStatus())).count()
        );

        model.addAttribute(
                "endedCount",
                auctions.getContent().stream()
                        .filter(a -> "ENDED".equals(a.getStatus())).count()
        );

        return "my-auctions";
    }
}
