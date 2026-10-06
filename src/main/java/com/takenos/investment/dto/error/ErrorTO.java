package com.takenos.investment.dto.error;

import java.util.List;

public record ErrorTO(
        String title,
        int status,
        String detail,
        List<ErrorTO> subErrors) {
}
