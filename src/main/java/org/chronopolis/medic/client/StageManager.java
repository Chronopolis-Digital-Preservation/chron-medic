package org.chronopolis.medic.client;

import org.chronopolis.rest.models.repair.Repair;

import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * Interface defining how our staging service will act
 *
 * Created by shake on 2/17/17.
 */
public interface StageManager {

    /**
     * Stage files requested by a repair
     *
     */
    StagingResult stage(Repair repair);

    /**
     * Remove files staged for a repair
     *
     */
    boolean clean(Repair repair);

    /**
     * Return a DirectoryStream of the top-most directories in the staging area
     *
     * This acts as a way to get repair ids and query against them
     *
     * This might not work for all staging strategies... but we'll figure it out
     *
     * @return DirectoryStream of ongoing replications
     * @throws IOException if there's an issue opening the directory
     */
    Stream<Path> ongoing() throws IOException;

}
