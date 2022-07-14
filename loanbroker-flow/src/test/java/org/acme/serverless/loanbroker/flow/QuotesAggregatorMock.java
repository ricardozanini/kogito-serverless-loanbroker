package org.acme.serverless.loanbroker.flow;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;

import java.util.Collections;
import java.util.Map;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

/**
 * Mocks the aggregator service that must return a list of quotes
 */
public class QuotesAggregatorMock implements QuarkusTestResourceLifecycleManager {

    private WireMockServer wireMockServer;

    @Override
    public Map<String, String> start() {
        wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        wireMockServer.start();
        wireMockServer.stubFor(get(urlPathMatching("/quotes/*"))
                .willReturn(aResponse()
                        .withBody(
                                "{[{\"rate\":5.8170958335644,\"bankId\":\"BankUniversal\"}, { \"rate\":7.206690977561289,\"bankId\":\"BankPawnshop\"}]}")
                        .withStatus(200)));

        // inject the endpoint to the generated RESTClient Stub
        return Collections.singletonMap("quarkus.rest-client.aggregator_yaml.url",
                wireMockServer.baseUrl());
    }

    @Override
    public void stop() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    @Override
    public void inject(Object testInstance) {
        ((LoanBrokerFlowIT) testInstance).aggregatorServer = wireMockServer;
    }

}
