package org.acme.serverless.loanbroker.aggregator;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.acme.serverless.loanbroker.aggregator.model.BankQuote;
import org.apache.camel.CamelContext;
import org.apache.camel.component.mock.MockEndpoint;
import org.hamcrest.core.Is;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;

@QuarkusTest
public class HttpAggregatorRouteTest {

    static {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @Inject
    CamelContext context;

    MockEndpoint mock;

    @Inject
    ObjectMapper objectMapper;

    @Inject
    QuotesRespositoryProcessor quotesRepository;

    @BeforeEach
    void beforeTest() {
        if (mock == null) {
            mock = context.getEndpoint("mock:aggregated.quotes", MockEndpoint.class);
        }
    }

    @AfterEach
    void afterTest() {
        if (mock != null) {
            mock.reset();
        }
        if (quotesRepository != null) {
            quotesRepository.clear();
        }
    }

    @Test
    void verifyOneMessageAggregated() throws InterruptedException, JsonProcessingException {
        this.postMessageAndExpectSuccess(
                new BankQuote("BankPremium", 4.655600086643112), "123");

        mock.await(5, TimeUnit.SECONDS);
        mock.expectedMessageCount(1);
        mock.assertIsSatisfied();
        this.getQuotesAndAssert(1, "123");
    }

    /**
     * @throws InterruptedException
     * @throws JsonProcessingException
     */
    @Test
    void verifyManyQuotesSingleInstanceMessageAggregated() throws InterruptedException, JsonProcessingException {
        this.postMessageAndExpectSuccess(
                new BankQuote("BankPremium", 4.655600086643112), "123");
        this.postMessageAndExpectSuccess(
                new BankQuote("BankStar", 5.4342645), "123");

        mock.await(5, TimeUnit.SECONDS);
        mock.expectedMessageCount(1);
        mock.expectedHeaderReceived(IntegrationConstants.KOGITO_FLOW_ID_HEADER, "123");
        mock.expectedHeaderReceived(QuotesAggregationStrategy.HEADER_QUOTES_COUNT, "2");
        mock.assertIsSatisfied();

        this.getQuotesAndAssert(2, "123");
    }

    @Test
    void verifyManyQuotesManyInstancesMessageAggregated() throws InterruptedException, JsonProcessingException {
        this.postMessageAndExpectSuccess(
                new BankQuote("BankPremium", 4.655600086643112), "123");
        this.postMessageAndExpectSuccess(
                new BankQuote("BankPremium", 5.4342645), "456");

        mock.await(5, TimeUnit.SECONDS);
        mock.expectedMessageCount(2);
        mock.expectedHeaderReceived(QuotesAggregationStrategy.HEADER_QUOTES_COUNT, "1");
        mock.assertIsSatisfied();

        this.getQuotesAndAssert(1, "123");
        this.getQuotesAndAssert(1, "456");
    }

    private void postMessageAndExpectSuccess(final BankQuote bankQuote, final String workflowInstanceId)
            throws JsonProcessingException {
        RestAssured.given()
                .header("Content-Type", "application/json")
                .header(IntegrationConstants.KOGITO_FLOW_ID_HEADER, workflowInstanceId)
                .body(objectMapper.writeValueAsString(bankQuote))
                .when()
                .post("/test")
                .then()
                .statusCode(200);
    }

    private void getQuotesAndAssert(final int quotesCount, final String workflowInstanceId) {
        RestAssured.given()
                .get("/quotes/" + workflowInstanceId)
                .then()
                .statusCode(200)
                .and()
                .body("size()", Is.is(quotesCount));
    }
}
