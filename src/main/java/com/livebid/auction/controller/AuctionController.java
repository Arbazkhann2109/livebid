package com.livebid.auction.controller;

import com.livebid.auction.dto.AuctionResponse;
import com.livebid.auction.dto.CreateAuctionRequest;
import com.livebid.auction.entity.Auction;
import com.livebid.auction.service.AuctionService;
import com.livebid.user.entity.User;
import com.livebid.user.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auctions")
@Validated
public class AuctionController {

    private final AuctionService auctionService;
    private final UserService userService;

    public AuctionController(
            AuctionService auctionService,
            UserService userService) {
        this.auctionService = auctionService;
        this.userService = userService;
    }

    @GetMapping
    public Page<AuctionResponse> listAuctions(
            @RequestParam(defaultValue = "0")
            @Min(0) int page,

            @RequestParam(defaultValue = "20")
            @Min(1) @Max(100) int size) {

        Page<Auction> auctions = auctionService.getAuctions(
                PageRequest.of(
                        page,
                        size,
                        Sort.by(Sort.Direction.DESC, "createdAt")));

        return auctions.map(AuctionResponse::from);
    }

    @GetMapping("/{id}")
    public AuctionResponse getAuction(@PathVariable Long id) {
        return AuctionResponse.from(auctionService.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AuctionResponse createAuction(
            @Valid @RequestBody CreateAuctionRequest request,
            Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authentication required.");
        }

        User seller = userService.getByEmail(authentication.getName());

        Auction auction = auctionService.createAuction(
                seller.getId(),
                request.title(),
                request.description(),
                request.startingPrice(),
                request.currency(),
                request.startsAt(),
                request.endsAt());

        return AuctionResponse.from(auction);
    }


    @PostMapping("/{id}/schedule")
    public AuctionResponse scheduleAuction(
            @PathVariable Long id,
            Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authentication required.");
        }

        User seller = userService.getByEmail(authentication.getName());

        Auction auction = auctionService.scheduleAuction(
                id,
                seller.getId());

        return AuctionResponse.from(auction);
    }


}
