package com.takenos.investment.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record SellRequestTO(
        @NotNull @DecimalMin(value = "0", inclusive = false) BigDecimal units,
        @NotNull @DecimalMin(value = "0", inclusive = false) BigDecimal pricePerUnit) {
}
