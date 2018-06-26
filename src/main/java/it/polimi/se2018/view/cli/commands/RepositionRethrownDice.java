package it.polimi.se2018.view.cli.commands;

import it.polimi.se2018.controller.actions.RepositionRethrownDiceAction;
import it.polimi.se2018.model.Dice;
import it.polimi.se2018.model.Position;
import it.polimi.se2018.view.cli.InputHelper;
import it.polimi.se2018.view.cli.PlayerViewCli;

import java.util.Scanner;

public class RepositionRethrownDice implements Command {

    @Override
    public String getText() {
        return "Position rethrown dice";
    }

    @Override
    public void handle(PlayerViewCli view) {
        Scanner input = view.getScanner();

        System.out.println("Select target position:");
        Position position = InputHelper.choosePosition(input, view.getPlayerViewBase().getWindowFrame());

        Dice dice = view.getPlayerViewBase().getRethrownDice();
        if (dice == null || !view.getPlayerViewBase().getWindowFrame().isPositionValid(dice, position, null)) {
            System.out.println("The move is not valid");
        } else {
            view.getPlayerViewBase().send(new RepositionRethrownDiceAction(position));
        }
    }

    @Override
    public boolean canPerform(PlayerViewCli view) {
        return view.isMyTurn() && view.getPlayerViewBase().getRethrownDice() != null;
    }
}