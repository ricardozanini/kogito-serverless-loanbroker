# Kogito Serverless Loan Broker Example

Serverless Loan Broker example Application on Kogito Workflows and Knative.
This example is based on the [Gregor Hohpe's blog post](https://www.enterpriseintegrationpatterns.com/ramblings/loanbroker_gcp_workflows.html). Please read the original blog to have a better understanding of this use case.

## How it works

This is an implementation of the same use case Gregor Hohpe discussed in his blog, but using only open source technology not tied to any cloud provider. You can deploy all the services, functions, and the workflow in any cloud given that you have access to Kubernetes with Knative.

This is the original architecture view of the use case:

![](loanbroker_serverless.png)

This is the implementation:

WIP

## Known Issues

- Lack of timeout. It's on Kogito's roadmap to have a consistent timeout implementation on version 1.28. As soon as we get there, I'll update the example.

## Deploying on Minikube

You can easily deploy this example on Minikube by using the provided `deploy.sh` script. But first, make sure that you have:

1. Installed Minikube
2. Installed [Knative Quickstart](https://knative.dev/docs/getting-started/quickstart-install/) on your Minikube installation. It adds a new `knative` profile to your cluster, so bear in mind that every command on Minikube must be followed by `-p knative`.
3. Installed JDK 11, Maven, NPM, and Docker in order to build all the parts of the example.

Now just run `./deploy.sh`. It will build all the services, create the Kubernetes object, and push the images to your Minikube's internal registry.

The UI will be exposed as a NodePort on `8080`. Access it using http://localhost:8080.

> It's likely to work on any Kubernetes cluster if you have the permissions to create namespaces. Otherwise, just change the `deploy.sh` script's `NAMESPACE` variable to install in another one.
