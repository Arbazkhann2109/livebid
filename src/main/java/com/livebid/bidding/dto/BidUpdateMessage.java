package com.livebid.bidding.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BidUpdateMessage(
        Long auctionId,
        Long bidId,
        Long bidderId,
        String bidderName,
        BigDecimal amount,
        LocalDateTime placedAt,
        Long totalBids
) {
}