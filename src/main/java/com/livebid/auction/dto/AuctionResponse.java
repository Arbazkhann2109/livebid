
package com.livebid.auction.dto;

import com.livebid.auction.entity.Auction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AuctionResponse(
        Long id,
        Long sellerId,
        String title,
        String description,
        BigDecimal startingPrice,
        String currency,
        LocalDateTime startsAt,
        LocalDateTime endsAt,
        String status,
        LocalDateTime createdAt
) {
    public static AuctionResponse from(Auction auction) {
        return new AuctionResponse(
                auction.getId(),
                auction.getSeller().getId(),
                auction.getTitle(),
                auction.getDescription(),
                auction.getStartingPrice(),
                auction.getCurrency(),
                auction.getStartsAt(),
                auction.getEndsAt(),
                auction.getStatus(),
                auction.getCreatedAt()
        );
    }
}
