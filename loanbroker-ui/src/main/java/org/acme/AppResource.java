package org.acme;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.cloudevents.CloudEvent;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;

@Path("/")
@ApplicationScoped
public class AppResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppResource.class);

    @ConfigProperty(name = "org.acme.loanbroker.ui.workflowURL")
    String workflowURL;

    @CheckedTemplate
    static class Templates {
        static native TemplateInstance app(String workflowURL);
    }

    @Path("app.js")
    @GET
    @Produces("text/javascript")
    public TemplateInstance appJS() {
        return Templates.app(workflowURL);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response consumeQuotesEvent(CloudEvent cloudEvent) {
        LOGGER.info("Received Cloud Event {}", cloudEvent);
        if (cloudEvent == null || cloudEvent.getData() == null) {
            return Response.status(400).entity("{ \"message\": \"CloudEvent without data\" }").build();
        }
        // TODO: save the event internally, expose it via websockets
        return Response.ok(cloudEvent.getId()).build();
    }

}
