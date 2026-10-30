
package com.livebid.bidding.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PlaceBidRequest(

        @NotNull
        @DecimalMin(value = "0.01")
        @Digits(integer = 11, fraction = 2)
        BigDecimal amount
) {
}
