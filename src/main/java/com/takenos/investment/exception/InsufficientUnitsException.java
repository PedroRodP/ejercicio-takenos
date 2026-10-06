package com.takenos.investment.exception;

import java.math.BigDecimal;

public class InsufficientUnitsException extends RuntimeException {

    public InsufficientUnitsException(String userId, BigDecimal requested, BigDecimal available) {
        super("User %s requested to sell %s units but only has %s".formatted(userId, requested, available));
    }
}
