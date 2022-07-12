package org.acme.serverless.loanbroker.aggregator;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.core.MediaType;

import org.apache.camel.Exchange;
import org.apache.camel.TypeConversionException;
import org.apache.camel.support.TypeConverterSupport;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;
import io.cloudevents.core.data.PojoCloudEventData;

@Singleton
public class QuotesToCloudEventsConverter extends TypeConverterSupport {

    @Inject
    ObjectMapper mapper;

    @Override
    @SuppressWarnings("unchecked")
    public <T> T convertTo(Class<T> type, Exchange exchange, Object value) throws TypeConversionException {
        if (CloudEvent.class.equals(type) && value instanceof List) {
            final CloudEvent event = CloudEventBuilder.v1()
                    .withId(UUID.randomUUID().toString())
                    .withExtension(IntegrationConstants.KOGITO_FLOW_ID_HEADER,
                            exchange.getIn().getHeader(IntegrationConstants.KOGITO_FLOW_ID_HEADER).toString())
                    .withType("kogito.serverless.loanbroker.aggregated.quotes.response")
                    .withSource(URI.create("/kogito/serverless/loanbroker/aggregator"))
                    .withDataContentType(MediaType.APPLICATION_JSON)
                    .withData(PojoCloudEventData.wrap(value, mapper::writeValueAsBytes))
                    .build();
            return (T) event;
        }
        return null;
    }

}
