package it.polimi.se2018.network;

import it.polimi.se2018.util.Observable;

/**
 * This interface defines the methods that a server side client handler
 * must define to allow the communication.
 */
public interface ClientHandler extends Observable {
    /**
     * Handle the login request of the remote client
     *
     * @param playerId id of the player logging in
     */
    void handleLogin(String playerId);
}
