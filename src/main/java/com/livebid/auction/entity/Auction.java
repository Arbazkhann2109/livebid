
package com.livebid.auction.entity;

import com.livebid.user.entity.User;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.livebid.bidding.entity.Bid;
@Entity
@Table(name = "auctions")
public class Auction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "starting_price", nullable = false, precision = 13, scale = 2)
    private BigDecimal startingPrice;

    @Column(nullable = false, length = 3)
    private String currency = "INR";

    @Column(name = "starts_at", nullable = false)
    private LocalDateTime startsAt;

    @Column(name = "ends_at", nullable = false)
    private LocalDateTime endsAt;

    @Column(nullable = false, length = 20)
    private String status = "DRAFT";

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "winning_bid_id")
    private Bid winningBid;

    protected Auction() {
    }

    public static Auction createDraft(
            User seller,
            String title,
            String description,
            BigDecimal startingPrice,
            String currency,
            LocalDateTime startsAt,
            LocalDateTime endsAt
    ) {
        Auction auction = new Auction();
        auction.seller = seller;
        auction.title = title;
        auction.description = description;
        auction.startingPrice = startingPrice;
        auction.currency = currency;
        auction.startsAt = startsAt;
        auction.endsAt = endsAt;
        auction.status = "DRAFT";
        return auction;
    }


    public Long getId() { return id; }
    public User getSeller() { return seller; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public BigDecimal getStartingPrice() { return startingPrice; }
    public String getCurrency() { return currency; }
    public LocalDateTime getStartsAt() { return startsAt; }
    public LocalDateTime getEndsAt() { return endsAt; }
    public String getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public Bid getWinningBid() {return winningBid;}

    public void setSeller(User seller) { this.seller = seller; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setStartingPrice(BigDecimal startingPrice) { this.startingPrice = startingPrice; }
    public void setCurrency(String currency) { this.currency = currency; }
    public void setStartsAt(LocalDateTime startsAt) { this.startsAt = startsAt; }
    public void setEndsAt(LocalDateTime endsAt) { this.endsAt = endsAt; }
    public void setStatus(String status) { this.status = status; }
    public void setWinningBid(Bid winningBid) {this.winningBid = winningBid;}

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
