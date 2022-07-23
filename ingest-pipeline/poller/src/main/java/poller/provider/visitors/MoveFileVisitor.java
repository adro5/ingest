package poller.provider.visitors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;

public class MoveFileVisitor extends CopyFileVisitor {

    private static final Logger log = LogManager.getLogger(MoveFileVisitor.class);

    public MoveFileVisitor(Path source, Path dest) {
        super(source, dest);
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) {
        Path newFile = dest.resolve(source.relativize(file));
        try {
            Files.move(file, newFile, StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException ex) {
            log.warn(ex.getMessage());
        }
        return FileVisitResult.CONTINUE;
    }

}
