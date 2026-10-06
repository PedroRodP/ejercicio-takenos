package com.takenos.investment.resource;

import com.takenos.investment.dto.BuyRequestTO;
import com.takenos.investment.dto.PurchaseTO;
import com.takenos.investment.service.PurchaseService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/v1/users/{userId}/purchases")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PurchaseResource {

    private final PurchaseService purchaseService;

    @Inject
    public PurchaseResource(PurchaseService purchaseService) {
        this.purchaseService = purchaseService;
    }

    @POST
    public Response buy(@PathParam("userId") String userId, @Valid @NotNull BuyRequestTO request) {
        PurchaseTO purchase = PurchaseTO.from(purchaseService.buy(userId, request));
        return Response.status(Response.Status.CREATED).entity(purchase).build();
    }
}
