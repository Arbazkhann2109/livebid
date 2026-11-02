package com.livebid.auction.repository;

import com.livebid.auction.entity.Auction;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;

public interface AuctionRepository extends JpaRepository<Auction, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Auction a WHERE a.id = :id")
    Optional<Auction> findByIdForUpdate(@Param("id") Long id);



    List<Auction> findTop100ByStatusAndStartsAtLessThanEqualOrderByStartsAtAsc(
            String status,
            LocalDateTime now
    );

    List<Auction> findTop100ByStatusAndEndsAtLessThanEqualOrderByEndsAtAsc(
            String status,
            LocalDateTime now
    );

    Optional<Auction> findFirstByStatusOrderByStartingPriceDesc(String status);

    long countByStatus(String status);

    Page<Auction> findBySellerId(Long sellerId, Pageable pageable);

    Page<Auction> findByStatusIn(List<String> statuses, Pageable pageable);

    // Search auctions by title
    @EntityGraph(attributePaths = {"seller"})
    Page<Auction> findByTitleContainingIgnoreCase(
            String title,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"seller"})
    @Query("SELECT a FROM Auction a")
    Page<Auction> findAllWithSeller(Pageable pageable);

    @EntityGraph(attributePaths = {
            "seller",
            "winningBid",
            "winningBid.bidder"
    })
    Optional<Auction> findWithDetailsById(Long id);

    long countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(
            LocalDateTime start,
            LocalDateTime end
    );
}