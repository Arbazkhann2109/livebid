
package com.livebid.bidding.entity;

import com.livebid.auction.entity.Auction;
import com.livebid.user.entity.User;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bids")
public class Bid {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "auction_id", nullable = false)
    private Auction auction;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bidder_id", nullable = false)
    private User bidder;

    @Column(nullable = false, precision = 13, scale = 2)
    private BigDecimal amount;

    @Column(name = "placed_at", nullable = false, updatable = false)
    private LocalDateTime placedAt;

    protected Bid() {
    }

    public static Bid place(Auction auction, User bidder, BigDecimal amount) {
        Bid bid = new Bid();
        bid.auction = auction;
        bid.bidder = bidder;
        bid.amount = amount;
        return bid;
    }


    public Long getId() { return id; }
    public Auction getAuction() { return auction; }
    public User getBidder() { return bidder; }
    public BigDecimal getAmount() { return amount; }
    public LocalDateTime getPlacedAt() { return placedAt; }

    public void setAuction(Auction auction) { this.auction = auction; }
    public void setBidder(User bidder) { this.bidder = bidder; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    @PrePersist
    protected void onCreate() {
        if (placedAt == null) {
            placedAt = LocalDateTime.now();
        }
    }
}
