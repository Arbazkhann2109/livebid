
package com.livebid;

import com.livebid.auction.entity.Auction;
import com.livebid.auction.repository.AuctionRepository;
import com.livebid.auction.service.AuctionService;
import com.livebid.bidding.repository.BidRepository;
import com.livebid.user.repository.UserRepository;

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
    private final AuctionRepository auctionRepository;
    private final UserRepository userRepository;
    private final BidRepository bidRepository;

    public HomeController(
            AuctionService auctionService,
            AuctionRepository auctionRepository,
            UserRepository userRepository,
            BidRepository bidRepository) {

        this.auctionService = auctionService;
        this.auctionRepository = auctionRepository;
        this.userRepository = userRepository;
        this.bidRepository = bidRepository;
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

        // Featured live auction
        Auction featuredAuction = auctionRepository
                .findFirstByStatusOrderByStartingPriceDesc("LIVE")
                .orElse(null);

        // Marketplace statistics
        long totalAuctions = auctionRepository.count();

        long liveAuctions = auctionRepository
                .countByStatus("LIVE");

        long totalUsers = userRepository.count();

        long totalBids = bidRepository.count();

        model.addAttribute("appName", "LiveBid");
        model.addAttribute("auctions", auctions.getContent());
        model.addAttribute("isAuthenticated", authenticated);

        model.addAttribute("featuredAuction", featuredAuction);

        model.addAttribute("totalAuctions", totalAuctions);
        model.addAttribute("liveAuctions", liveAuctions);
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalBids", totalBids);

        return "index";
    }
}
