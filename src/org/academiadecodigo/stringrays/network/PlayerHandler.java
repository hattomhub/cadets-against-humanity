package org.academiadecodigo.stringrays.network;

import org.academiadecodigo.bootcamp.Prompt;
import org.academiadecodigo.bootcamp.scanners.menu.MenuInputScanner;
import org.academiadecodigo.bootcamp.scanners.string.StringInputScanner;
import org.academiadecodigo.stringrays.constants.Colors;
import org.academiadecodigo.stringrays.constants.Constants;
import org.academiadecodigo.stringrays.constants.Messages;
import org.academiadecodigo.stringrays.game.GameStatus;
import org.academiadecodigo.stringrays.game.cards.Card;
import org.academiadecodigo.stringrays.game.player.Player;

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
        //askIfReady();
        decipherStartMenu();

        while (!Thread.currentThread().isInterrupted()) {
            waitingForInstructions();
        }
    }

    private void init() {

        //player.setPlayerHandler(this);

        server.getPlayerHandlers().add(this);

        try {
            prompt = new Prompt(playerSocket.getInputStream(), new PrintStream(playerSocket.getOutputStream(), true));
            out = new PrintWriter(playerSocket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
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

        if (newStatus == GameStatus.CZAR_WAITING) {
            out.println(Colors.BLACK + "\n\t" + Messages.BLACK_CARD + server.getGame().getBlackCard().getMessage() + "\n");
            out.println(Messages.CZAR_TURN_MESSAGE);
        }

        if (newStatus == GameStatus.CZAR_TURN) {
            chooseCzarCard();
            newStatus = GameStatus.PLAYER_WAITING;
        }

        oldStatus = newStatus;
    }

    private void chooseNickname() { //TODO CHECK IF THE NICKNAME IS REPEATED
        out.println(Messages.LOGIN_WELCOME);
        StringInputScanner scanner = new StringInputScanner();
        scanner.setMessage(Messages.LOGIN_MESSAGE);
        player.setNickname(prompt.getUserInput(scanner));
    }

    public void askIfReady() {

        String[] options = {"YES"};

        MenuInputScanner scanner = new MenuInputScanner(options);

        scanner.setMessage(Messages.MAIN_MENU_READY);

        scanner.setError("Tell me when you're ready...");

        prompt.getUserInput(scanner);

        server.broadcastMessage(player.getNickname() + " is ready!");

        server.gameReady();
    }


    public int startMenu(){

        String[] options = {"START GAME", "FIND ROOM"};

        MenuInputScanner scanner = new MenuInputScanner(options);

        scanner.setMessage(Messages.LOGIN_WELCOME);

        scanner.setError("Select a valid option");

        return prompt.getUserInput(scanner);
    }

    public void decipherStartMenu(){

        int choice = startMenu();

        if (choice == 1) {
            askIfReady();}

        if (choice == 2 ) {
            server.broadcastMessage(player.getNickname() + " is ready!");
            server.gameReady();
        }

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

    public int chooseCard(String blackCard, String[] cardsMessages, String message) {
        MenuInputScanner scanner = new MenuInputScanner(cardsMessages);
        scanner.setMessage("\t" + Messages.BLACK_CARD + blackCard + "\n\n" + message);
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
