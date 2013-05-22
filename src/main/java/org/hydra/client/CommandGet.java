package org.hydra.client;

import com.beust.jcommander.Parameter;

public class CommandGet {
    @Parameter(names = "--id", description = "The id of the key to retrieve.")
    public String id;

}
