package org.acme.serverless.loanbroker.aggregator;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.acme.serverless.loanbroker.aggregator.model.BankQuote;
import org.apache.camel.builder.endpoint.EndpointRouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;

/**
 * Exposing a `test` endpoint to do a few experimentations and verify the
 * Aggregator behavior.
 * <p/>
 * Just POST the Quote Payload with the HTTP Header `ce-kogitoprocinstanceid` to
 * `/test`
 */
@ApplicationScoped
public class HttpAggregatorRoute extends EndpointRouteBuilder {

    @Inject
    QuotesRespositoryProcessor quotesRespository;

    @Override
    public void configure() throws Exception {
        restConfiguration().bindingMode(RestBindingMode.json);
        rest("/test")
                .post()
                .type(BankQuote.class)
                .to("direct:aggregator");

        from("direct:aggregator")
                .aggregate(header(IntegrationConstants.KOGITO_FLOW_ID_HEADER), new QuotesAggregationStrategy())
                .completionInterval(3000)
                .process(quotesRespository)
                .to("mock:aggregated.quotes");
    }

}
