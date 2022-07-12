package org.acme.serverless.loanbroker.aggregator.resources;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.acme.serverless.loanbroker.aggregator.IntegrationConstants;
import org.acme.serverless.loanbroker.aggregator.model.BankQuote;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.cloudevents.CloudEvent;
import io.cloudevents.jackson.PojoCloudEventDataMapper;

@Path("/")
@ApplicationScoped
public class QuotesCloudEventsResource {

    /**
     * Produced by Camel
     */
    @Produce("direct:aggregator")
    ProducerTemplate aggregatorProducer;

    @Inject
    ObjectMapper mapper;

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response consumeQuoteEvent(CloudEvent cloudEvent) {
        if (cloudEvent == null || cloudEvent.getData() == null) {
            return Response.status(400).entity(ResponseError.NO_DATA_EVENT_ERROR).build();
        }
        if (cloudEvent.getExtension(IntegrationConstants.KOGITO_FLOW_ID_HEADER) == null) {
            return Response.status(400).entity(ResponseError.NO_DATA_EVENT_ERROR).build();
        }
        aggregatorProducer.sendBodyAndHeader(
                "direct:aggregator",
                PojoCloudEventDataMapper.from(mapper, BankQuote.class).map(cloudEvent.getData()).getValue(),
                IntegrationConstants.KOGITO_FLOW_ID_HEADER,
                cloudEvent.getExtension(IntegrationConstants.KOGITO_FLOW_ID_HEADER).toString());
        return Response.ok(cloudEvent.getId()).build();
    }

}
