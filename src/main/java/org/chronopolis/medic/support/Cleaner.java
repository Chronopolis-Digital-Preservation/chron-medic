package org.chronopolis.medic.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

/**
 * Class to encapsulate some of the cleaning functionality which is shared
 * between repair/fulfillment
 *
 * Created by shake on 5/1/17.
 */
public class Cleaner implements Callable<Boolean> {

    private final Logger log = LoggerFactory.getLogger(Cleaner.class);

    private final Path root;
    private final Set<String> files;

    public Cleaner(Path root, Set<String> files) {
        this.root = root;
        this.files = files;
    }

    private void checkDir(Path dir) throws IOException {
        long count;
        try (Stream<Path> stream = Files.list(dir)) {
            count = stream.count();

        }

        // Ensure the Stream above is closed before removing
        if (count == 0) {
            log.debug("{} removing directory", dir);
            Files.delete(dir);
        }
    }

    @Override
    public Boolean call() {
        boolean success = true;
        log.debug("[{}] walking file tree", root.toString());
        try {
            Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Path relative = root.relativize(file);
                    // the relative directory is missing the leading "/", which is there because it is
                    // locally the "root" of the collection
                    boolean contains = files.contains("/" + relative.toString());
                    log.debug("{} to be removed: {}", relative, contains);
                    if (contains) {
                        Files.delete(file);
                    }
                    return FileVisitResult.CONTINUE;
                }

                public FileVisitResult postVisitDirectory(Path dir, IOException e)  throws IOException {
                    checkDir(dir);
                    return FileVisitResult.CONTINUE;
                }
            });

            checkDir(root.getParent());
        } catch (IOException e) {
            log.error("Problem while cleaning", e);
            success = false;
        }


        return success;
    }
}
