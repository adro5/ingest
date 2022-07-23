package poller.provider.visitors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class CopyFileVisitor extends SimpleFileVisitor<Path> {
    final Path source;
    final Path dest;
    private static final Logger log = LogManager.getLogger(CopyFileVisitor.class);

    public CopyFileVisitor(Path source, Path dest) {
        this.source = source;
        this.dest = dest;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attributes) {
        Path newDir = dest.resolve(source.relativize(dir));
        try {
            if (!Files.exists(newDir)) {
                Files.copy(dir, newDir, StandardCopyOption.COPY_ATTRIBUTES);
            }
        } catch (IOException ex) {
            log.warn(ex.getMessage());
            ex.printStackTrace();
            return FileVisitResult.SKIP_SUBTREE;
        }
        return FileVisitResult.CONTINUE;
    }
    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException ex) {
        return FileVisitResult.CONTINUE;
    }
    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) {
        Path newFile = dest.resolve(source.relativize(file));
        try {
            Files.copy(file, newFile, StandardCopyOption.COPY_ATTRIBUTES);
        } catch (IOException ex) {
            log.warn(ex.getMessage());
        }
        return FileVisitResult.CONTINUE;
    }
    @Override
    public FileVisitResult visitFileFailed(Path file, IOException ex) {
        log.error(ex.getMessage());
        ex.printStackTrace();
        return FileVisitResult.CONTINUE;
    }
}
