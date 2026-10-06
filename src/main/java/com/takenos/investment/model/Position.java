package com.takenos.investment.model;

import java.math.BigDecimal;

public record Position(
        String userId,
        BigDecimal units,
        BigDecimal costUsd,
        BigDecimal realizedProfitUsd) {
}
