function refreshLoans() {
    $.getJSON("{workflowURL}/loanbroker", (subs) => {
        printLoansTable(subs);
    }).fail(function () {
        showError("An error was produced during the loans refresh, please check that que loanbroker-flow application is running.");
    });
}

function printLoansTable(loans) {
    const loansTable = $('#pendingLoans');
    loansTable.children().remove();
    const loansTableBody = $('<tbody>').appendTo(loansTable);
    printLoansTableHeader(loansTable)
    for (const loan of loans) {
        printLoanRow(loansTableBody, loan)
    }
}

function printLoansTableHeader(subscriptionTable) {
    const header = $('<thead class="thead-dark">').appendTo(subscriptionTable);
    const headerTr = $('<tr>').appendTo(header);
    $('<th scope="col">#Workflow instance</th>').appendTo(headerTr);
    $('<th scope="col">SSN</th>').appendTo(headerTr);
    $('<th scope="col">Term</th>').appendTo(headerTr);
    $('<th scope="col">Amount</th>').appendTo(headerTr);
    $('<th scope="col">Score</th>').appendTo(headerTr);
    $('<th scope="col">History</th>').appendTo(headerTr);
}

function printLoanRow(loanTableBody, loan) {
    if (loan.workflowdata && loan.workflowdata.credit) {
        const queryRow = $('<tr>').appendTo(loanTableBody);
        queryRow.append($(`<th scope="row" style="width: 30%;">$\{loan.id}</th>`));
        queryRow.append($(`<td style="width: 30%;">$\{loan.workflowdata.credit.SSN}</td>`));
        queryRow.append($(`<td style="width: 30%;">$\{loan.workflowdata.term}</td>`));
        queryRow.append($(`<td style="width: 30%;">$\{loan.workflowdata.amount}</td>`));
        queryRow.append($(`<td style="width: 30%;">$\{loan.workflowdata.credit.score}</td>`));
        queryRow.append($(`<td style="width: 30%;">$\{loan.workflowdata.credit.history}</td>`));
    }
}

function showError(message) {
    const notification = $(`<div class="toast" role="alert" aria-live="assertive" aria-atomic="true" style="min-width: 30rem"/>`)
            .append($(`<div class="toast-header bg-danger">
                 <strong class="me-auto text-white">Error</strong>
                 <button type="button" class="btn-close" data-bs-dismiss="toast" aria-label="Close"></button>
               </div>`))
            .append($(`<div class="toast-body"/>`)
                    .append($(`<p/>`).text(message))
            );
    $("#notificationPanel").append(notification);
    notification.toast({
        delay: 30000
    });
    notification.toast("show");
}

function showLoanForm() {
    const form = $('#newLoanForm');
    form.find('#txtSSN').val('');
    form.find('#txtTerm').val('');
    form.find('#txtAmount').val('');
    form.modal('show');
}

function newLoanRequest() {
    const form = $('#newLoanForm');
    const ssn = form.find('#txtSSN').val();
    const term = form.find('#txtTerm').val();
    const amount = form.find('#txtAmount').val();
    const processInputJson = {
        "workflowdata": {
            "SSN": ssn,
            "term": parseInt(term),
            "amount": parseInt(amount)
        }
    };
    const processInput = JSON.stringify(processInputJson);
    $.ajax({
        url: "{workflowURL}/loanbroker",
        type: "POST",
        dataType: "json",
        contentType: "application/json; charset=UTF-8",
        data: processInput,
        success: function (result) {
            console.log(JSON.stringify(result));
            form.modal('hide');
            refreshLoans();
        },
        error: function(xhr, status, error) {
            form.modal('hide');
            showError("An error was produced during the serverless workflow instance create attempt, please check that que loanbroker-flow application is running:\n" + xhr.responseText);
        }
    });
}

$(document).ready(function () {
    //Initial queries loading
    refreshLoans();

    //Initialize button listeners
    $('#refreshButton').click(function () {
        refreshLoans();
    });

    $("#askNewLoan").click(function () {
        showLoanForm();
    });

});