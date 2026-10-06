package com.takenos.investment.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record BuyRequestTO(
        @NotNull @DecimalMin(value = "0", inclusive = false) BigDecimal amountUsd,
        @NotNull @DecimalMin(value = "0", inclusive = false) BigDecimal pricePerUnit,
        @NotBlank String reference) {
}
