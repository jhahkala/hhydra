package org.hydra.client;

import com.beust.jcommander.Parameter;

public class CommandPut {
    @Parameter(names = "--id", description = "The id to store the key part under.")
    public String id;
    @Parameter(names = "--key", description = "The key information to store.")
    public String key;
    @Parameter(names = "--iv", description = "The initialization vector.")
    public String iv;
    @Parameter(names = "--min", description = "The minimum number of key parts required to reconstruct the key.")
    public int minParts;
    @Parameter(names = "--part", description = "The key part number.")
    public int partNumber;

}
