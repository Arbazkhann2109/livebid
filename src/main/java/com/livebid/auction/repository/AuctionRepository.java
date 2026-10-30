
package com.livebid.auction.repository;

import com.livebid.auction.entity.Auction;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

import java.util.Optional;

public interface AuctionRepository extends JpaRepository<Auction, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Auction a WHERE a.id = :id")
    Optional<Auction> findByIdForUpdate(@Param("id") Long id);

    List<Auction> findTop100ByStatusAndStartsAtLessThanEqualOrderByStartsAtAsc(
            String status,
            LocalDateTime now);

    List<Auction> findTop100ByStatusAndEndsAtLessThanEqualOrderByEndsAtAsc(
            String status,
            LocalDateTime now);

}
