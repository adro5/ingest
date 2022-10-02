package collector.verticle;

import collector.collectors.AbstractFileCollector;
import collector.collectors.HDFSFileCollector;
import collector.collectors.LocalFileCollector;
import collector.collectors.S3FileCollector;
import collector.config.CollectorConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import config.OptionsConfiguration;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;

public class CollectorVerticle extends AbstractVerticle {
    final EventBus bus = vertx.eventBus();
    private CollectorConfig config;
    private static final Logger log = LogManager.getLogger(CollectorVerticle.class);
    private AbstractFileCollector collector;

    private void configure(String[] args) {
        OptionsConfiguration options = new OptionsConfiguration();
        CommandLineParser parser = new DefaultParser();

        try {
            CommandLine cmd = parser.parse(options, args);
            String configFile = cmd.getOptionValue("configFile");

            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            this.config = mapper.readValue(new File(configFile), CollectorConfig.class);

            log.log(Level.INFO, config.toString());
        } catch (Exception jx) {
            log.log(Level.ERROR, jx.getMessage());
        }

        log.log(Level.INFO, "Starting Collector...");
    }

    private void startShutdownServer() {
        Router router = Router.router(vertx);
        router.route().path("/shutdown").handler(context -> {
            context.json(new JsonObject()
                    .put("shutdown", "shutdown collector"));
            vertx.eventBus().publish("shutdownRequest", "shutdown");
        });

        vertx.createHttpServer()
                .requestHandler(router)
                .listen(config.getPortNumber())
                .onSuccess(server -> log.log(Level.INFO, "Started shutdown server"));
    }

    @Override
    public void start() {
        configure(this.processArgs().toArray(new String[0]));

        // Check to see if we have a request to shut down
        bus.consumer("shutdownRequest", shutdownMessage -> {
            if (shutdownMessage.body().toString().equals("shutdown")) {
                log.info("Stopping Collector...");
                vertx.close();
            }
        });

        startShutdownServer();
        try {
            // Deploying periodic verticles to handle file collecting
            if (config.getSrcURIScheme().equals("hdfs")) {
                collector = new HDFSFileCollector(config);
            } else if (config.getSrcURIScheme().equals("s3")) {
                collector = new S3FileCollector(config);
            } else if (config.getSrcURIScheme().equals("file")) {
                collector = new LocalFileCollector(config);
            }
        } catch (IOException ex) {
            log.error(ex.getMessage());
        }

        vertx.setPeriodic(config.getPollRate(), handler -> collector.collect());
    }
}
