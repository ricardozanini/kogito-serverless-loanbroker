const { CloudEvent, HTTP } = require('cloudevents');
/**
 * Your HTTP handling function, invoked with each request. This is an example
 * function that echoes its input to the caller, and returns an error if
 * the incoming request is something other than an HTTP POST or GET.
 *
 * In can be invoked with 'func invoke'
 * It can be tested with 'npm test'
 *
 * @param {Context} context a context object.
 * @param {object} context.body the request body if any
 * @param {object} context.query the query string deserialized as an object, if any
 * @param {object} context.log logging object with methods for 'info', 'warn', 'error', etc.
 * @param {object} context.headers the HTTP request headers
 * @param {string} context.method the HTTP request method
 * @param {string} context.httpVersion the HTTP protocol version
 * See: https://github.com/knative-sandbox/kn-plugin-func/blob/main/docs/guides/nodejs.md#the-context-object
 */
const handle = async (context, event) => {
  console.log("context");
  console.log(JSON.stringify(context, null, 2));

  console.log("event");
  console.log(JSON.stringify(event, null, 2));

  const requestId = context.cloudevent.kogitoprocinstanceid;
  const bankId = process.env.BANK_ID;
  const eventType = "kogito.serverless.loanbroker.bank.offer"

  const response = bankQuote(context.cloudevent.data, bankId);

  return HTTP.binary(new CloudEvent({
    source: "/kogito/serverless/loanbroker/bank/" + bankId,
    type: eventType,
    data: response,
    extensions: { requestid: requestId }
  }));;
}

function calcRate(amount, term, score, history) {
  if (amount <= process.env.MAX_LOAN_AMOUNT && score >= process.env.MIN_CREDIT_SCORE) {
    return parseFloat(process.env.BASE_RATE) + Math.random() * ((1000 - score) / 100.0);
  }
}

bankQuote = (quoteRequest, bankId) => {
  const rate = calcRate(quoteRequest.amount, quoteRequest.term, quoteRequest.credit.score, quoteRequest.credit.history);

  if (rate) {
    console.log('%s offering Loan at %f', bankId, rate);
    return { "rate": rate, "bankId": bankId };
  } else {
    console.log('%s rejecting Loan', bankId);
    return {};
  }
}

// Export the function
module.exports = { handle };
