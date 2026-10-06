package com.takenos.investment.resource;

import com.takenos.investment.dto.PositionTO;
import com.takenos.investment.service.PositionService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/v1/users/{userId}/position")
@Produces(MediaType.APPLICATION_JSON)
public class PositionResource {

    private final PositionService positionService;

    @Inject
    public PositionResource(PositionService positionService) {
        this.positionService = positionService;
    }

    @GET
    public PositionTO getPosition(@PathParam("userId") String userId) {
        return PositionTO.from(positionService.getPosition(userId));
    }
}
