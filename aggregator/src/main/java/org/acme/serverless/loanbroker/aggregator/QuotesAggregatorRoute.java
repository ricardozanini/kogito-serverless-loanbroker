package org.acme.serverless.loanbroker.aggregator;

import java.util.ArrayList;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.camel.builder.endpoint.EndpointRouteBuilder;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.cloudevents.CloudEvent;

/**
 * Aggregation Strategy for all quotes received.
 * The payload must be a reference to the BankQuote model and the header must
 * include the {@link IntegrationConstants#KOGITO_FLOW_ID_HEADER}
 */
@ApplicationScoped
public class QuotesAggregatorRoute extends EndpointRouteBuilder {

        @Inject
        QuotesRespositoryProcessor quotesRespository;

        @Inject
        QuotesToCloudEventsConverter quotesToCloudEventsConverter;

        @ConfigProperty(name = "org.acme.serverless.loanbroker.aggregator.replyTo")
        String replyTo;

        @Override
        public void configure() throws Exception {
                // @formatter:off
                getContext()
                        .getTypeConverterRegistry()
                        .addTypeConverter(CloudEvent.class, ArrayList.class,
                        quotesToCloudEventsConverter);
                
                from("direct:aggregator")
                        .routeId("quotes-aggregator")
                        .aggregate(header(IntegrationConstants.KOGITO_FLOW_ID_HEADER), new QuotesAggregationStrategy())
                        .completionInterval(3000)
                        .process(quotesRespository)
                        .convertBodyTo(CloudEvent.class)
                        // FIXME: replace with adviceWith
                        .to("mock:aggregated.quotes");
                        // TODO: add an integration test with WireMock to verify this event
                        //.toD(replyTo + "?copyHeaders=false");
                // @formatter:on
        }

}
