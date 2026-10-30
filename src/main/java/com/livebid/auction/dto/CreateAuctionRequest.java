
package com.livebid.auction.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CreateAuctionRequest(

        @NotBlank
        @Size(max = 200)
        String title,

        @Size(max = 10000)
        String description,

        @NotNull
        @DecimalMin("0.00")
        @Digits(integer = 11, fraction = 2)
        BigDecimal startingPrice,

        @NotBlank
        @Pattern(regexp = "INR|USD|EUR")
        String currency,

        @NotNull
        @Future
        LocalDateTime startsAt,

        @NotNull
        @Future
        LocalDateTime endsAt
) {
}
