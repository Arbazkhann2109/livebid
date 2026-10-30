
package com.livebid;

import com.livebid.auction.service.AuctionService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final AuctionService auctionService;

    public HomeController(AuctionService auctionService) {
        this.auctionService = auctionService;
    }

    @GetMapping("/")
    public String home(Authentication authentication, Model model) {
        var auctions = auctionService.getAuctions(
                PageRequest.of(
                        0,
                        12,
                        Sort.by(Sort.Direction.DESC, "createdAt")
                )
        );

        boolean authenticated =
                authentication != null
                        && authentication.isAuthenticated()
                        && !(authentication instanceof AnonymousAuthenticationToken);

        model.addAttribute("appName", "LiveBid");
        model.addAttribute("auctions", auctions.getContent());
        model.addAttribute("isAuthenticated", authenticated);

        return "index";
    }
}
