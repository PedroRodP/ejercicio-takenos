package com.takenos.investment.resource;

import com.takenos.investment.dto.SaleTO;
import com.takenos.investment.dto.SellRequestTO;
import com.takenos.investment.service.SaleService;
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

@Path("/v1/users/{userId}/sales")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class SaleResource {

    private final SaleService saleService;

    @Inject
    public SaleResource(SaleService saleService) {
        this.saleService = saleService;
    }

    @POST
    public Response sell(@PathParam("userId") String userId, @Valid @NotNull SellRequestTO request) {
        SaleTO sale = SaleTO.from(saleService.sell(userId, request));
        return Response.status(Response.Status.CREATED).entity(sale).build();
    }
}
