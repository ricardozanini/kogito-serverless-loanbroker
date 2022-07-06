package org.acme.serverless.loanbroker.aggregator;

import java.util.ArrayList;
import java.util.List;

import org.acme.serverless.loanbroker.aggregator.model.BankQuote;
import org.apache.camel.AggregationStrategy;
import org.apache.camel.Exchange;

public class QuotesAggregationStrategy implements AggregationStrategy {
    /**
     * Everything but Kogito and Camel
     */
    private static String REMOVE_HEADERS_PATTERN = "^(?!ce-kogito).*$";

    @Override
    public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
        final BankQuote quote = (BankQuote)newExchange.getIn().getBody();
        if (quote.getBankId() == null || quote.getBankId().isEmpty()) {
            return null;
        }

        if (oldExchange == null) {
            final List<BankQuote> quotes = new ArrayList<>();
            quotes.add(quote);
            newExchange.getIn().setBody(quote);
            newExchange.getIn().removeHeaders(REMOVE_HEADERS_PATTERN);
            return newExchange;
        }

        final List<BankQuote> quotes = (List<BankQuote>) oldExchange.getIn().getBody();
        quotes.add(quote);
        oldExchange.getIn().setBody(quotes);
        oldExchange.getIn().removeHeaders(REMOVE_HEADERS_PATTERN);
        return oldExchange;
    }
    
}
