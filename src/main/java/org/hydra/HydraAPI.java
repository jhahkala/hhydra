package org.hydra;

import java.io.IOException;

public interface HydraAPI {

    /**
     * Puts a key piece into the hydra service.
     * 
     * @param id The id the store the key piece with.
     * @param key The key piece to store.
     * @throws IOException In case there is a problem in communication or
     *             storing.
     */
    public void putKeyPiece(String id, KeyPiece key) throws IOException;

    /**
     * Retrieves a key piece from the hydra service.
     * 
     * @param id The id of the key piece to retrieve.
     * @return The key piece retrieved.
     * @throws IOException In case there is a problem in communication or
     *             retrieving.
     */
    public KeyPiece getKeyPiece(String id) throws IOException;

    /**
     * Removes a key piece from the hydra service.
     * 
     * @param id The id of the key piece to remove.
     * @throws IOException In case there is a problem in communication or
     *             removing.
     */
    public void removeKeyPiece(String id) throws IOException;

    /**
     * Returns the version of the service.
     * @return The service version, e.g. "0.5.2"
     * @throws IOException thrown if there is a communication problem.
     */
    public String getVersion() throws IOException;

}
