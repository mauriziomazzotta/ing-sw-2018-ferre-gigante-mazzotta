package it.polimi.se2018.controller;

import it.polimi.se2018.util.Observable;
import it.polimi.se2018.controller.actions.Action;
import it.polimi.se2018.controller.events.Event;
import it.polimi.se2018.controller.events.InvalidActionEvent;
import it.polimi.se2018.controller.events.LoginEvent;
import it.polimi.se2018.model.*;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Controller {

    private static final Controller INSTANCE = new Controller();

    private static final Logger LOGGER = Logger.getLogger("Controller");

    private static final int MIN_PLAYERS = 2;
    private static final int MAX_PLAYERS = 4;

    private static final int CREATION_TIMEOUT = 30;

    private static final int PATTERN_CARDS_PER_PLAYER = 2;

    private final List<String> waitingPlayers = new LinkedList<>();
    private final Map<String, Player> playersMap = new HashMap<>();
    private final Map<String, Observable> observablesMap = new HashMap<>();
    private final List<Game> games = new LinkedList<>();

    private final ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture scheduledGameCreationTimer;
    private Runnable createGameRunnable;
    private boolean startASAP = false;

    private Controller() {
        createGameRunnable = () -> {
            if (waitingPlayers.size() >= MIN_PLAYERS) {
                LOGGER.info("We have enough players, create a new Game");
                newGame();
            } else {
                LOGGER.info("Not enough players, starting when new player logs in");
                startASAP = true;
            }
        };
        startGameCreationTimer();
    }

    public static Controller getInstance() {
        return INSTANCE;
    }

    private void startGameCreationTimer() {
        startASAP = false;
        if (scheduledGameCreationTimer != null && !scheduledGameCreationTimer.isDone()) {
            scheduledGameCreationTimer.cancel(true);
        }
        scheduledGameCreationTimer = scheduledExecutor.schedule(createGameRunnable, CREATION_TIMEOUT, TimeUnit.SECONDS);
    }

    public void send(Event event) {
        String playerId = event.getPlayerId();
        Observable observable = getObservable(playerId);
        if (observable != null) {
            getObservable(playerId).send(event);
        }
    }

    public void handle(Action action) {
        Player player = getPlayer(action.getPlayerId());
        if (player == null) {
            LOGGER.severe("No player found, ignoring action. ID: " + action.getPlayerId());
            return;
        }
        try {
            synchronized (player.getGame()) {
                action.perform(player);
            }
        } catch (RuntimeException e) {
            LOGGER.log(Level.SEVERE, "Invalid action", e);
            send(new InvalidActionEvent(action.getPlayerId(), action));
        }
    }

    public void removeClient(String playerId) {
        observablesMap.remove(playerId);
        waitingPlayers.remove(playerId);
        Player player = playersMap.get(playerId);
        if (player != null && player.getGame() != null) {
            player.getGame().suspendPlayer(player);
        }
    }

    private Player getPlayer(String id) {
        return playersMap.get(id);
    }

    private Observable getObservable(String id) {
        return observablesMap.get(id);
    }

    public synchronized boolean canJoin(String playerId) {
        return !waitingPlayers.contains(playerId) && !playersMap.containsKey(playerId);
    }

    public synchronized void joinGame(String playerId, Observable observable) {
        if (!canJoin(playerId)) {
            LOGGER.log(Level.INFO, "Duplicate player ID: " + playerId);

            return;
        }
        waitingPlayers.add(playerId);
        observablesMap.put(playerId, observable);
        observable.send(new LoginEvent(playerId, true));


        if (waitingPlayers.size() >= MAX_PLAYERS || (startASAP && waitingPlayers.size() >= MIN_PLAYERS)) {
            newGame();
        }
    }

    private synchronized void newGame() {
        startGameCreationTimer();
        Iterator<String> it = waitingPlayers.iterator();
        List<String> waitingClientsRemoved = new ArrayList<>();
        while (it.hasNext() && waitingClientsRemoved.size() < MAX_PLAYERS) {
            waitingClientsRemoved.add(it.next());
            it.remove();
        }

        List<WindowPatternCard> windowPatternCards = new WindowPatternsDeck().getWindowPatternCards();
        Collections.shuffle(windowPatternCards);
        List<Player> newPlayers = new ArrayList<>();
        for (int i = 0; i < waitingClientsRemoved.size(); i++) {
            Player.Color playerColor = Player.Color.values()[i];
            String playerId = waitingClientsRemoved.get(i);
            List<WindowPatternCard> playerWindowPatternCards = new ArrayList<>();
            for (int j = 0; j < PATTERN_CARDS_PER_PLAYER; j++) {
                playerWindowPatternCards.add(windowPatternCards.remove(0));
            }

            Player player = new Player(playerId, playerWindowPatternCards, playerColor);
            newPlayers.add(player);
            playersMap.put(playerId, player);
        }
        Game game = new Game(newPlayers);
        games.add(game);
    }
}