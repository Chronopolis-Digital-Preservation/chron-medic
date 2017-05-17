package org.chronopolis.medic.support;

import org.chronopolis.rest.models.repair.Repair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Class with a couple functions to create and remove data
 * from a test staging area
 *
 * Created by shake on 5/17/17.
 */
public class Staging {
    private static final Logger log = LoggerFactory.getLogger(Staging.class);

    public static void populate(Path directory, Repair repair) throws IOException {
        log.info("{} creating files for testing", directory);
        repair.getFiles().stream()
                .map(f -> Paths.get(directory.toString(), f))
                .peek(p -> {
                    try {
                        Files.createDirectories(p.getParent());
                    } catch (IOException e) {
                        log.warn("", e);
                    }
                })
                .forEach(p -> {
                    try {
                        Files.createFile(p);
                    } catch (IOException e) {
                        log.warn("", e);
                    }
                });
    }

}
