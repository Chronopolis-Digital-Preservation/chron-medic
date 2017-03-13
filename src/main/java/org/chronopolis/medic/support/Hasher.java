package org.chronopolis.medic.support;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import org.chronopolis.common.ace.CompareFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Helper class to Hash a file and return a {@link CompareFile}
 *
 * Created by shake on 3/13/17.
 */
public class Hasher {
    private final Logger log = LoggerFactory.getLogger(Hasher.class);

    private final Path root;

    public Hasher(Path root) {
        this.root = root;
    }

    public CompareFile hash(Path path) {
        CompareFile compare = new CompareFile();
        String hash;
        try {
            HashCode hashcode = Files.hash(path.toFile(), Hashing.sha256());
            hash = hashcode.toString();
        } catch (IOException e) {
            log.error("Error hashing file", e);
            hash = ""; // just a null hash, will automatically be invalid when comparing
        }

        compare.setDigest(hash);
        // Needs to be relative with a leading '/'
        compare.setPath(path.toString());
        return compare;
    }
}
