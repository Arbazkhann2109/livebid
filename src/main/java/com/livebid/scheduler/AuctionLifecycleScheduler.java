
package com.livebid.scheduler;

import com.livebid.auction.entity.Auction;
import com.livebid.auction.repository.AuctionRepository;
import com.livebid.auction.service.AuctionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class AuctionLifecycleScheduler {

    private static final Logger log =
            LoggerFactory.getLogger(AuctionLifecycleScheduler.class);

    private final AuctionRepository auctionRepository;
    private final AuctionService auctionService;

    public AuctionLifecycleScheduler(
            AuctionRepository auctionRepository,
            AuctionService auctionService) {
        this.auctionRepository = auctionRepository;
        this.auctionService = auctionService;
    }

    @Scheduled(fixedDelay = 5000)
    public void updateAuctionStatuses() {
        LocalDateTime now = LocalDateTime.now();

        List<Auction> dueToStart =
                auctionRepository
                        .findTop100ByStatusAndStartsAtLessThanEqualOrderByStartsAtAsc(
                                "SCHEDULED", now);

        for (Auction auction : dueToStart) {
            try {
                auctionService.activateScheduledAuction(auction.getId());
            } catch (Exception exception) {
                log.error(
                        "Failed to activate auction {}",
                        auction.getId(),
                        exception);
            }
        }

        List<Auction> dueToEnd =
                auctionRepository
                        .findTop100ByStatusAndEndsAtLessThanEqualOrderByEndsAtAsc(
                                "LIVE", now);

        for (Auction auction : dueToEnd) {
            try {
                auctionService.endExpiredAuction(auction.getId());
            } catch (Exception exception) {
                log.error(
                        "Failed to end auction {}",
                        auction.getId(),
                        exception);
            }
        }
    }
}
