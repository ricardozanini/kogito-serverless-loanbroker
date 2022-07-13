package org.acme.serverless.loanbroker.aggregator;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import java.util.HashMap;
import java.util.Map;

import com.github.tomakehurst.wiremock.WireMockServer;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

public class SinkMockTestResource implements QuarkusTestResourceLifecycleManager {
    private WireMockServer server;

    @Override
    public Map<String, String> start() {
        // Setup & start the server
        server = new WireMockServer(
                wireMockConfig().dynamicPort());
        server.start();

        // Stub a HTTP endpoint. Note that WireMock also supports a record and playback
        // mode
        // http://wiremock.org/docs/record-playback/
        server.stubFor(
                post(urlEqualTo("/"))
                        .willReturn(aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody("{\"123456\"}")));

        // Ensure the camel component API client passes requests through the WireMock
        // proxy
        Map<String, String> conf = new HashMap<>();
        conf.put("org.acme.serverless.loanbroker.aggregator.replyTo", server.baseUrl());
        return conf;
    }

    @Override
    public void stop() {
        if (server != null) {
            server.stop();
        }
    }
}
