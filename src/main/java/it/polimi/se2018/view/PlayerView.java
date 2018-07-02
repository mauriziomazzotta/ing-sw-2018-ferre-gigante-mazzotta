package it.polimi.se2018.view;

import it.polimi.se2018.controller.actions.Action;
import it.polimi.se2018.controller.events.InitialSetupEvent;
import it.polimi.se2018.model.Dice;
import it.polimi.se2018.model.Position;
import it.polimi.se2018.model.WindowFrame;
import it.polimi.se2018.model.WindowPattern;

import java.util.List;
import java.util.Map;

public interface PlayerView {
    PlayerViewBase getPlayerViewBase();

    void onLogin(boolean result);

    void onInitialSetup(InitialSetupEvent.Data data);

    void onDicePlaced(String playerPlacingDice, Position position, Dice dice);

    void onDraftPoolChanged(List<Dice> draftPool);

    void onGameOver();

    void onInvalidAction(Action action);

    void onNewTurn(String playerId);

    void onPointsChanged(int points);

    void onTokensChanged(int tokens);

    void onGameStarted(Map<String, WindowFrame> windowFramesMap);

    void onNewDraftDice(Dice dice);

    void onPlayerSuspended();
}
