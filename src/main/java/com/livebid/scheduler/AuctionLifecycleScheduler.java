
package com.livebid.scheduler;

import com.livebid.auction.entity.Auction;
import com.livebid.auction.repository.AuctionRepository;
import com.livebid.auction.service.AuctionService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
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

    @EventListener(ApplicationReadyEvent.class)
    public void recoverAuctionsOnStartup() {
        log.info("Starting auction lifecycle recovery");

        try {
            updateAuctionStatuses();
            log.info("Auction lifecycle recovery completed");
        } catch (Exception exception) {
            log.error("Auction lifecycle recovery failed", exception);
        }
    }

    @Scheduled(fixedDelay = 5000)
    public void updateAuctionStatuses() {

        LocalDateTime now = LocalDateTime.now();

        int activated = processScheduledAuctions(now);
        int ended = processExpiredAuctions(now);

        if (activated > 0 || ended > 0) {
            log.info(
                    "Auction lifecycle updated: {} activated, {} ended",
                    activated,
                    ended
            );
        }
    }



    private int processScheduledAuctions(LocalDateTime now) {

        int processed = 0;

        while (true) {
            List<Auction> batch =
                    auctionRepository
                            .findTop100ByStatusAndStartsAtLessThanEqualOrderByStartsAtAsc(
                                    "SCHEDULED", now);

            if (batch.isEmpty()) {
                break;
            }

            for (Auction auction : batch) {
                try {
                    auctionService.activateScheduledAuction(auction.getId());
                    processed++;
                } catch (Exception exception) {
                    log.error(
                            "Failed to activate auction {}",
                            auction.getId(),
                            exception
                    );
                }
            }

            // Stop if every auction in this batch failed.
            if (batch.size() > 0 && processed == 0) {
                break;
            }
        }

        return processed;
    }

    private int processExpiredAuctions(LocalDateTime now) {

        int processed = 0;

        while (true) {
            List<Auction> batch =
                    auctionRepository
                            .findTop100ByStatusAndEndsAtLessThanEqualOrderByEndsAtAsc(
                                    "LIVE", now);

            if (batch.isEmpty()) {
                break;
            }

            int batchProcessed = 0;

            for (Auction auction : batch) {
                try {
                    auctionService.endExpiredAuction(auction.getId());
                    processed++;
                    batchProcessed++;
                } catch (Exception exception) {
                    log.error(
                            "Failed to end auction {}",
                            auction.getId(),
                            exception
                    );
                }
            }

            // Prevent an infinite loop if a batch cannot be processed.
            if (batchProcessed == 0) {
                break;
            }
        }

        return processed;
    }
}
