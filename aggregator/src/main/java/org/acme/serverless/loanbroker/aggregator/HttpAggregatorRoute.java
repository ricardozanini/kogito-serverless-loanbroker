package org.acme.serverless.loanbroker.aggregator;

import javax.enterprise.context.ApplicationScoped;

import org.acme.serverless.loanbroker.aggregator.model.BankQuote;
import org.apache.camel.builder.AggregationStrategies;
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

    @Override
    public void configure() throws Exception {

        restConfiguration().bindingMode(RestBindingMode.json);
        rest("/test")
                .post()
                .type(BankQuote.class)
                .to("direct:aggregator");

        from("direct:aggregator")
        .aggregate(header("ce-kogitoprocinstanceid"), new QuotesAggregationStrategy())
            .completionInterval(3000)
        .to("log:info");
    }

}
