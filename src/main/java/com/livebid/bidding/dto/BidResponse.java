
package com.livebid.bidding.dto;

import com.livebid.bidding.entity.Bid;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BidResponse(
        Long id,
        Long auctionId,
        Long bidderId,
        BigDecimal amount,
        LocalDateTime placedAt
) {
    public static BidResponse from(Bid bid) {
        return new BidResponse(
                bid.getId(),
                bid.getAuction().getId(),
                bid.getBidder().getId(),
                bid.getAmount(),
                bid.getPlacedAt()
        );
    }
}
