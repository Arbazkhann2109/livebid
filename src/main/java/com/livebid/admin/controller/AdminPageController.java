package com.livebid.admin.controller;

import com.livebid.auction.repository.AuctionRepository;
import com.livebid.bidding.repository.BidRepository;
import com.livebid.user.repository.UserRepository;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminPageController {

    private final UserRepository userRepository;
    private final AuctionRepository auctionRepository;
    private final BidRepository bidRepository;

    public AdminPageController(
            UserRepository userRepository,
            AuctionRepository auctionRepository,
            BidRepository bidRepository
    ) {
        this.userRepository = userRepository;
        this.auctionRepository = auctionRepository;
        this.bidRepository = bidRepository;
    }

    @GetMapping
    public String dashboard(Model model) {

        model.addAttribute("totalUsers", userRepository.count());
        model.addAttribute("totalAuctions", auctionRepository.count());
        model.addAttribute("liveAuctions", auctionRepository.countByStatus("LIVE"));
        model.addAttribute("completedAuctions", auctionRepository.countByStatus("ENDED"));
        model.addAttribute("totalBids", bidRepository.count());

        // Auction status breakdown
        Map<String, Long> auctionStatuses = new LinkedHashMap<>();
        auctionStatuses.put("Draft", auctionRepository.countByStatus("DRAFT"));
        auctionStatuses.put("Scheduled", auctionRepository.countByStatus("SCHEDULED"));
        auctionStatuses.put("Live", auctionRepository.countByStatus("LIVE"));
        auctionStatuses.put("Ended", auctionRepository.countByStatus("ENDED"));
        auctionStatuses.put("Cancelled", auctionRepository.countByStatus("CANCELLED"));

        model.addAttribute("auctionStatuses", auctionStatuses);

        // Monthly analytics for the last six months
        List<Map<String, Object>> monthlyStats = new ArrayList<>();

        YearMonth currentMonth = YearMonth.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy");

        for (int i = 5; i >= 0; i--) {

            YearMonth month = currentMonth.minusMonths(i);

            LocalDateTime start = month.atDay(1).atStartOfDay();
            LocalDateTime end = month.plusMonths(1).atDay(1).atStartOfDay();

            Map<String, Object> stats = new LinkedHashMap<>();

            stats.put("month", month.format(formatter));

            stats.put("users",
                    userRepository.countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                            start, end
                    ));

            stats.put("auctions",
                    auctionRepository.countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                            start, end
                    ));

            stats.put("bids",
                    bidRepository.countByPlacedAtGreaterThanEqualAndPlacedAtLessThan(
                            start, end
                    ));

            monthlyStats.add(stats);
        }

        model.addAttribute("monthlyStats", monthlyStats);

        return "admin/dashboard";
    }
}