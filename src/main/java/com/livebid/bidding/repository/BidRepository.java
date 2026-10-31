
package com.livebid.bidding.repository;

import com.livebid.bidding.entity.Bid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BidRepository extends JpaRepository<Bid, Long> {

    @EntityGraph(attributePaths = {"bidder"})
    Page<Bid> findByAuctionIdOrderByAmountDesc(
            Long auctionId,
            Pageable pageable
    );

    List<Bid> findByBidderIdOrderByPlacedAtDesc(Long bidderId);
    long countByAuctionId(Long auctionId);
    Optional<Bid> findTopByAuctionIdOrderByAmountDesc(Long auctionId);

    @EntityGraph(attributePaths = {"bidder"})
    Optional<Bid> findFirstByAuctionIdOrderByAmountDescPlacedAtAscIdAsc(
            Long auctionId
    );
}
