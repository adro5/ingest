package poller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import poller.collector.AbstractCollector;
import poller.collector.HDFSCollector;
import poller.collector.S3Collector;
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
import poller.provider.FileProvider;
import poller.provider.HDFSFileProvider;
import poller.provider.LocalFileProvider;
import poller.provider.S3FileProvider;

import java.io.File;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;

public class Poller extends AbstractVerticle {
    private PollerConfig config;
    private static final Logger log = LogManager.getLogger(Poller.class);
    private AbstractCollector<Path> collector;
    private FileProvider provider;

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

        // Deploying periodic verticles to handle file collecting
        // We can condense this code down once LFP gets a Collector
        // Eventually we will be to do other things besides Remote -> Local or Local -> Local transfers
        if (config.getSrcURIScheme().equals("hdfs")) {
            collector = new HDFSCollector<>(Path::compareTo, config);
            vertx.setPeriodic(config.getPollRate(), handler -> {
                collector.collect();
            });
            provider = new LocalFileProvider(config, collector);
            vertx.setPeriodic(config.getPollRate(), handler -> {
                provider.move();
            });
        } else if (config.getSrcURIScheme().equals("s3")) {
            collector = new S3Collector<>(Path::compareTo, config);
            vertx.setPeriodic(config.getPollRate(), handler -> {
                collector.collect();
            });
            provider = new LocalFileProvider(config, collector);
            vertx.setPeriodic(config.getPollRate(), handler -> {
                provider.move();
            });
        } else if (config.getSrcURIScheme().equals("file") && config.getDestURIScheme().equals("file")) {
            // Deploying a periodic verticle to handle file providing
            vertx.setPeriodic(config.getPollRate(), handler -> {
                if (config.getDestURIScheme().equals("file")) {
                    LocalFileProvider localFileProvider = new LocalFileProvider(config);
                    localFileProvider.move();
                }
            });
        }

        // Check to see if we have a request to shut down
        EventBus eb = vertx.eventBus();
        eb.consumer("shutdownRequest", shutdownMessage -> {
            if (shutdownMessage.body().toString().equals("shutdown")) {
                log.info("Shutting down");
                vertx.undeploy(webDeployID.get());
                vertx.close();
            }
        });
    }
}
