package com.takenos.investment.resource

import com.takenos.investment.dto.PositionTO
import com.takenos.investment.model.Position
import com.takenos.investment.service.PositionService
import spock.lang.Specification
import spock.lang.Subject

class PositionResourceTest extends Specification {

    PositionService positionService = Mock(PositionService)

    @Subject
    PositionResource positionResource = new PositionResource(positionService)

    def "getPosition should delegate to the service and return the position"() {
        given:
        def position = new Position("user-1", new BigDecimal("8"), new BigDecimal("120"), new BigDecimal("20"))

        when:
        def response = positionResource.getPosition("user-1")

        then:
        1 * positionService.getPosition("user-1") >> position
        response == PositionTO.from(position)
    }
}
