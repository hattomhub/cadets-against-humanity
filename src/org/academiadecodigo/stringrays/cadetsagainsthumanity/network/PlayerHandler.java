package org.academiadecodigo.stringrays.cadetsagainsthumanity.network;

import org.academiadecodigo.bootcamp.Prompt;
import org.academiadecodigo.bootcamp.scanners.menu.MenuInputScanner;
import org.academiadecodigo.bootcamp.scanners.string.StringInputScanner;
import org.academiadecodigo.stringrays.cadetsagainsthumanity.constants.Colors;
import org.academiadecodigo.stringrays.cadetsagainsthumanity.constants.Constants;
import org.academiadecodigo.stringrays.cadetsagainsthumanity.constants.Messages;
import org.academiadecodigo.stringrays.cadetsagainsthumanity.game.GameStatus;
import org.academiadecodigo.stringrays.cadetsagainsthumanity.game.cards.Card;
import org.academiadecodigo.stringrays.cadetsagainsthumanity.game.player.Player;

import java.io.*;
import java.net.Socket;

public class PlayerHandler implements Runnable {

    private Server server;
    private Socket playerSocket;
    private Prompt prompt;
    private PrintWriter out;
    private Player player;
    private GameStatus newStatus = GameStatus.PLAYER_WAITING;
    private GameStatus oldStatus;

    PlayerHandler(Server server, Socket playerSocket, Player player) {
        this.server = server;
        this.playerSocket = playerSocket;
        this.player = player;
    }

    @Override
    public void run() {
        init();
        chooseNickname();
        askIfReady();
        while (!Thread.currentThread().isInterrupted()) {
            waitingForInstructions();
        }
    }

    private void init() {

        server.getPlayerHandlers().add(this);

        try {
            prompt = new Prompt(playerSocket.getInputStream(), new PrintStream(playerSocket.getOutputStream(), true));
            out = new PrintWriter(playerSocket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        out.println(Messages.LOGIN_WELCOME);
    }

    private void waitingForInstructions() {

        if (newStatus == oldStatus) {
            return;
        }

        if (newStatus == GameStatus.PLAYER_WAITING) {
            out.println(Messages.PLAYER_TURN_WAIT);
        }

        if (newStatus == GameStatus.PLAYER_TURN) {
            chooseWhiteCard();
            newStatus = GameStatus.PLAYER_WAITING;
            return;
        }

        if (newStatus == GameStatus.CZAR_CHOOSING) {
            broadcastWhiteCardsSelect();
        }

        if (newStatus == GameStatus.CZAR_WAITING) {
            out.println("\n\t" + Colors.BG_BLACK + Colors.WHITE + " Black Card: " + server.getGame().getBlackCard().getMessage() + Colors.BG_RESET + Colors.RESET + "\n");
            out.println(Messages.CZAR_TURN_MESSAGE);

        }

        if (newStatus == GameStatus.CZAR_TURN) {
            chooseCzarCard();
            newStatus = GameStatus.PLAYER_WAITING;
        }

        oldStatus = newStatus;
    }

    private void chooseNickname() { //TODO CHECK IF THE NICKNAME IS REPEATED

        StringInputScanner scanner = new StringInputScanner();
        scanner.setMessage(Messages.LOGIN_MESSAGE);

        String name = prompt.getUserInput(scanner);

        if (server.getNicknames().contains(name)) {
            out.println(Messages.LOGIN_ERROR);
            chooseNickname();
            return;
        }

        server.getNicknames().add(name);
        player.setNickname(name);

    }


    public void askIfReady() {

        String[] options = {"YES"};

        MenuInputScanner scanner = new MenuInputScanner(options);

        scanner.setMessage(Messages.MAIN_MENU_READY);

        scanner.setError("Tell me when you're ready...");

        prompt.getUserInput(scanner);

        server.broadcastMessage(player.getNickname() + " is ready!");
        player.setReady(true);
        server.gameReady();
    }

    private void chooseWhiteCard() {

        int index = chooseCard(server.getGame().getBlackCard().getMessage(),
                player.getCardMessages(),
                Messages.PLAYER_TURN_MESSAGE);

        Card whiteCard = player.getCard(index);

        server.getGame().play(whiteCard, player);
    }

    private void chooseCzarCard() {

        int index = chooseCard(server.getGame().getBlackCard().getMessage(),
                server.getGame().getCzarHand().getCardMessages(),
                Messages.CZAR_TURN_CHOOSE);

        Card czarCard = server.getGame().getCzarHand().getCard(index);

        server.getGame().checkRoundWinner(czarCard);

    }

    private void broadcastWhiteCardsSelect() {
        out.println(Colors.BG_BLACK + Colors.WHITE + "\n\t" + " Black Card: " + server.getGame().getBlackCard().getMessage() + Colors.BG_RESET + Colors.RESET + "\n");

        for (String cardMessage : server.getGame().getCzarHand().getCardMessages()) {
            out.println(Colors.BG_WHITE + Colors.BLACK + " White Card: " + cardMessage + Colors.BG_RESET + Colors.RESET);
        }

    }

    public int chooseCard(String blackCard, String[] cardsMessages, String message) {
        MenuInputScanner scanner = new MenuInputScanner(cardsMessages);
        scanner.setMessage("\t" + Colors.BG_BLACK + Colors.WHITE + " Black Card: " + blackCard + Colors.BG_RESET + Colors.RESET + "\n\n" + message);
        scanner.setError(Messages.INVALID_OPTION);

        return prompt.getUserInput(scanner) - Constants.CONVERT_PROMPT_VIEW_INDEX;
    }

    public void sendMessageToPlayer(String message) {
        out.println(message);
    }

    private void close(Closeable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Player getPlayer() {
        return player;
    }

    public void setStatus(GameStatus status) {
        this.newStatus = status;
    }
}