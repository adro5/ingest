package poller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import poller.config.PollerConfig;
import poller.config.PollerOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import poller.provider.LocalFileProvider;

import java.io.File;
import java.util.concurrent.atomic.AtomicReference;

public class Poller extends AbstractVerticle {
    private PollerConfig config;
    private static final Logger log = LogManager.getLogger(Poller.class);

    protected void configure(String[] args) {
        PollerOptions options = new PollerOptions();
        CommandLineParser parser = new DefaultParser();

        try {
            CommandLine cmd = parser.parse(options, args);
            String configFile = cmd.getOptionValue("configFile");

            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            this.config = mapper.readValue(new File(configFile), PollerConfig.class);

            log.log(Level.INFO, config.toString());
        } catch (Exception jx) {
            log.log(Level.ERROR, jx.getMessage());
        }

        log.log(Level.INFO, "Welcome to Poller 0.1");
    }

    public PollerConfig getConfig() {
        return this.config;
    }

    @Override
    public void start() throws InterruptedException {
        configure(processArgs().toArray(new String[0]));
        AtomicReference<String> webDeployID = new AtomicReference<>("");

        // Create a new Vertx Verticle to handle web requests. Vertx manages it's own thread pool
        vertx.deployVerticle(new ServerVerticle(config), response -> {
            if (response.succeeded()) {
                log.info("Web server successfully deployed");
            }
            else if (response.failed()) {
                log.warn("Web service failed to deploy...Shutting down...");
                vertx.close();
            }
            webDeployID.set(response.result());
        });

        Thread.sleep(1000);

        // Deploying a periodic verticle to handle file polling
        vertx.setPeriodic(config.getPollRate(), handler -> {
            if (config.getSrcURIScheme().equals("file") && config.getDestURIScheme().equals("file")) {
                LocalFileProvider localFileProvider = new LocalFileProvider(config);
                localFileProvider.move();
            }
        });

        // Check to see if we have a request to shut down
        EventBus eb = vertx.eventBus();
        eb.consumer("shutdownRequest", shutdownMessage -> {
            if (shutdownMessage.body().toString().equals("shutdown")) {
                vertx.undeploy(webDeployID.get());
                vertx.close();
            }
        });
    }
}
