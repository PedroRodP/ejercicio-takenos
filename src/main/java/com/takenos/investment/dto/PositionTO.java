package com.takenos.investment.dto;

import com.takenos.investment.model.Position;

import java.math.BigDecimal;

public record PositionTO(
        String userId,
        BigDecimal units,
        BigDecimal costUsd,
        BigDecimal realizedProfitUsd) {

    public static PositionTO from(Position position) {
        return new PositionTO(
                position.userId(),
                position.units(),
                position.costUsd(),
                position.realizedProfitUsd());
    }
}
