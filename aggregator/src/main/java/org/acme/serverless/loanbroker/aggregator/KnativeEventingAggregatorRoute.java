package org.acme.serverless.loanbroker.aggregator;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.knative.KnativeComponent;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.quarkus.arc.profile.UnlessBuildProfile;

/**
 * Our Aggregator implementation is handled by Apache Camel
 * 
 * @see <a href=
 *      "https://camel.apache.org/components/3.17.x/eips/aggregate-eip.html">Camel
 *      - AGGREGATE</a>
 */
@ApplicationScoped
// disable for now, we need more tests
public class KnativeEventingAggregatorRoute extends RouteBuilder {

    @Inject
    CamelContext camelContext;

    @PostConstruct
    public void init() {
        final KnativeComponent component = camelContext.getComponent("knative", KnativeComponent.class);
        component.setEnvironmentPath("classpath:camel-knative.json");
        component.init();
    }

    @ConfigProperty(name = "org.acme.serverless.loanbroker.aggregator.broker", defaultValue = "default")
    String brokerName;

    @Override
    public void configure() throws Exception {
        from("knative:event/quotes-agreggator").to("log:info");
    }

}
