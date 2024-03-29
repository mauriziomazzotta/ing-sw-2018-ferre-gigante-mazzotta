package it.polimi.se2018.view.cli.commands;

import it.polimi.se2018.view.cli.DicePrinter;
import it.polimi.se2018.view.cli.PlayerViewCli;

public class ShowTrack implements Command {
    @Override
    public String getText() {
        return "Show track";
    }

    @Override
    public void handle(PlayerViewCli view) {
        DicePrinter.printTrack(view.getPlayerViewBase().getTrackDices());
    }

    @Override
    public boolean canPerform(PlayerViewCli view) {
        return view.isGameSetUp() && view.isGameStarted();
    }
}