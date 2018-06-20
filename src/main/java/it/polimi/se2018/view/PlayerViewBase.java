package it.polimi.se2018.view;

import it.polimi.se2018.controller.events.InitialSetupEvent;
import it.polimi.se2018.model.*;
import it.polimi.se2018.util.Observer;
import it.polimi.se2018.controller.actions.Action;
import it.polimi.se2018.controller.events.Event;
import it.polimi.se2018.rmi.RmiClient;
import it.polimi.se2018.network.Client;
import it.polimi.se2018.network.ConnectionType;
import it.polimi.se2018.socket.SocketClient;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PlayerViewBase implements Observer, PlayerView {

    private static final Logger LOGGER = Logger.getLogger("PlayerView");

    private final String playerId;
    private final Client client;
    private final PlayerView playerViewImpl;

    // Player properties
    private Player.Color playerColor;
    private List<WindowPatternCard> windowPatternCards;
    private WindowFrame windowFrame;
    private List<ObjectiveCard> privateObjectCards;
    private int favorTokens;

    // Game properties
    private List<ObjectiveCard> publicObjectCards;
    private List<ToolCard> toolCards;
    private int turnTimeout;
    private List<String> playerIds;
    private Map<String, WindowFrame> rivalFrames;

    public PlayerViewBase(PlayerView playerViewImpl, String playerId, ConnectionType connectionType)
            throws IOException, NotBoundException {
        this.playerViewImpl = playerViewImpl;
        this.playerId = playerId;
        if (connectionType.equals(ConnectionType.SOCKET)) {
            client = new SocketClient(this);
        } else {
            client = new RmiClient(this);
        }
    }

    public void login() throws IOException {
        client.login(getPlayerId());
    }

    public void logout() throws IOException {
        client.logout();
    }

    public String getPlayerId() {
        return playerId;
    }

    @Override
    public void handle(Event event) {
        event.update(this);
    }

    @Override
    public void send(Action action) {
        client.send(action);
    }

    public Map<String, WindowFrame> getRivalFrames() {
        return rivalFrames;
    }

    public Player.Color getPlayerColor() {
        return playerColor;
    }

    public List<WindowPatternCard> getWindowPatternCards() {
        return windowPatternCards;
    }

    public List<ObjectiveCard> getPrivateObjectCards() {
        return privateObjectCards;
    }

    public int getFavorTokens() {
        return favorTokens;
    }

    public List<ObjectiveCard> getPublicObjectCards() {
        return publicObjectCards;
    }

    public List<ToolCard> getToolCards() {
        return toolCards;
    }

    public int getTurnTimeout() {
        return turnTimeout;
    }

    public List<String> getPlayerIds() {
        return playerIds;
    }

    public void setWindowFrame(WindowPattern windowPattern) {
        windowFrame = new WindowFrame(windowPattern);
    }

    public WindowFrame getWindowFrame() {
        return windowFrame;
    }

    @Override
    public PlayerViewBase getPlayerViewBase() {
        return this;
    }

    @Override
    public void onLogin(boolean result) {
        playerViewImpl.onLogin(result);
    }

    @Override
    public void onInitialSetup(InitialSetupEvent.Data data) {
        this.playerColor = data.playerColor;
        this.windowPatternCards = data.windowPatternCards;
        this.privateObjectCards = data.privateObjectCards;
        this.favorTokens = data.favorTokens;

        this.publicObjectCards = data.publicObjectCards;
        this.toolCards = data.toolCards;
        this.turnTimeout = data.turnTimeout;
        this.playerIds = data.playerIds;

        playerViewImpl.onInitialSetup(data);
    }

    @Override
    public void onDicePlaced(Position position, Dice dice) {
        playerViewImpl.onDicePlaced(position, dice);
    }

    @Override
    public void onDraftPoolChanged() {
        playerViewImpl.onDraftPoolChanged();
    }

    @Override
    public void onGameOver() {
        playerViewImpl.onGameOver();
    }

    @Override
    public void onInvalidAction(Action action) {
        playerViewImpl.onInvalidAction(action);
    }

    @Override
    public void onNewTurn() {
        playerViewImpl.onNewTurn();
    }

    @Override
    public void onPointsChanged(int points) {
        playerViewImpl.onPointsChanged(points);
    }

    @Override
    public void onTokensChanged(int tokens) {
        playerViewImpl.onTokensChanged(tokens);
    }

    @Override
    public void onGameStarted(Map<String, WindowPattern> windowPatternMap) {
        for (Map.Entry<String, WindowPattern> entry : windowPatternMap.entrySet()) {
            String rivalId = entry.getKey();
            WindowFrame rivalWindowFrame = new WindowFrame(entry.getValue());
            rivalFrames.put(rivalId, rivalWindowFrame);
        }
        playerViewImpl.onGameStarted(windowPatternMap);
    }
}