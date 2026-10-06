package com.takenos.investment.resource;

import com.takenos.investment.dto.error.ErrorTO;
import com.takenos.investment.exception.InsufficientUnitsException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

import java.util.List;

public class ExceptionMappers {

    @ServerExceptionMapper
    public Response handleConstraintViolationException(ConstraintViolationException exception) {
        List<ErrorTO> subErrors = exception.getConstraintViolations().stream()
                .map(this::toSubError)
                .toList();
        ErrorTO error = new ErrorTO("Invalid request", Response.Status.BAD_REQUEST.getStatusCode(), exception.getMessage(), subErrors);
        return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
    }

    @ServerExceptionMapper
    public Response handleInsufficientUnitsException(InsufficientUnitsException exception) {
        int status = 422;
        ErrorTO error = new ErrorTO("Insufficient units", status, exception.getMessage(), List.of());
        return Response.status(status).entity(error).build();
    }

    private ErrorTO toSubError(ConstraintViolation<?> violation) {
        String field = violation.getPropertyPath().toString();
        return new ErrorTO(field, Response.Status.BAD_REQUEST.getStatusCode(), violation.getMessage(), List.of());
    }
}
