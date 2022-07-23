package poller.provider;

import poller.config.PollerConfig;
import io.vertx.core.AbstractVerticle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import poller.provider.visitors.CopyFileVisitor;
import poller.provider.visitors.MoveFileVisitor;

import java.io.IOException;
import java.nio.file.Files;

public class LocalFileProvider extends AbstractVerticle implements FileProvider {

    private final PollerConfig pollerConfig;
    private static final Logger logger = LogManager.getLogger(LocalFileProvider.class);

    public LocalFileProvider(PollerConfig config) {
        this.pollerConfig = config;
    }

    @Override
    public boolean move() {
        if (pollerConfig.isCopyOnWrite()) {
            try {
                logger.info("Copying files from {} to {}", pollerConfig.getSrcDirectory(), pollerConfig.getDestDirectory());
                Files.walkFileTree(pollerConfig.getNormalSrcDirectory(), new CopyFileVisitor(pollerConfig.getNormalSrcDirectory(), pollerConfig.getNormalDestDirectory()));
                return true;
            } catch (IOException ex) {
                logger.error(ex.getMessage());
            }
        }
        else {
            try {
                logger.info("Moving files from {} to {}", pollerConfig.getSrcDirectory(), pollerConfig.getDestDirectory());
                Files.walkFileTree(pollerConfig.getNormalSrcDirectory(), new MoveFileVisitor(pollerConfig.getNormalSrcDirectory(), pollerConfig.getNormalDestDirectory()));
                return true;
            } catch (IOException ex) {
                logger.error(ex.getMessage());
            }
        }
        return false;
    }
}
