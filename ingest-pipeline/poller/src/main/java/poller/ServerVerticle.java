package poller;

import poller.config.PollerConfig;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;

public class ServerVerticle extends AbstractVerticle {
    PollerConfig config;

    public ServerVerticle(PollerConfig config) {
        this.config = config;
    }

    @Override
    public void start() {
        // TODO: Write production quality routing and port handling
        Router router = Router.router(vertx);
        router.route().path("/shutdown").handler(context -> {
            context.json(new JsonObject()
                    .put("shutdown", "Shutdown received"));
            vertx.eventBus().publish("shutdownRequest", "shutdown");
        });

        vertx.createHttpServer()
                .requestHandler(router)
                .listen(config.getPortNumber())
                .onSuccess(server -> {
                    System.out.println("Started server");
                });
    }
}
