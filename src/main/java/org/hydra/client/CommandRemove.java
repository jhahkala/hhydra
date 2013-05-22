package org.hydra.client;

import com.beust.jcommander.Parameter;

public class CommandRemove {
    @Parameter(names = "--id", description = "The id of the key to remove.")
    public String id;

}
