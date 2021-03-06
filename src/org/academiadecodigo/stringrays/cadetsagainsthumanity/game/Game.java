package org.academiadecodigo.stringrays.cadetsagainsthumanity.game;

import org.academiadecodigo.stringrays.cadetsagainsthumanity.constants.Colors;
import org.academiadecodigo.stringrays.cadetsagainsthumanity.constants.Constants;
import org.academiadecodigo.stringrays.cadetsagainsthumanity.constants.Messages;
import org.academiadecodigo.stringrays.cadetsagainsthumanity.constants.Random;
import org.academiadecodigo.stringrays.cadetsagainsthumanity.game.cards.Card;
import org.academiadecodigo.stringrays.cadetsagainsthumanity.game.cards.Deck;
import org.academiadecodigo.stringrays.cadetsagainsthumanity.game.cards.Hand;
import org.academiadecodigo.stringrays.cadetsagainsthumanity.game.cards.PopulateDeck;
import org.academiadecodigo.stringrays.cadetsagainsthumanity.game.player.Player;
import org.academiadecodigo.stringrays.cadetsagainsthumanity.network.Server;

import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

public class Game implements Runnable {

    private Deck blackDeck;
    private Deck whiteDeck;
    private volatile ConcurrentHashMap<Card, Player> playedCards;
    private volatile Hand czarHand;
    private volatile Vector<Player> players;
    private Player czar;
    private Player winner;
    private Server server;
    private Card blackCard;
    private Card czarCard;
    private volatile boolean gameStart;

    public void init() {
        blackDeck = PopulateDeck.fillDeck(Constants.blackDeck);
        whiteDeck = PopulateDeck.fillDeck(Constants.whiteDeck);
        players = new Vector<>();
    }

    public void waitingForInstructions() {
        if (gameStart) {
            start();
        }
    }


    public void start() {

        resetPlayers();

        players.get(0).setCzar(true);

        while (!playerWon()) {
            System.out.println(Messages.NEW_ROUND);
            server.broadcastMessage(Messages.NEW_ROUND);
            newRound();
        }

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        start();
    }

    public Player createPlayer() {
        //instance of new player
        Player newPlayer = new Player();

        //gives cards, to the new player
        for (int i = 0; i < Constants.PLAYER_HAND_SIZE; i++) {
            drawWhiteCard(newPlayer);
        }

        //adding player to the list of players in game
        players.add(newPlayer);

        return newPlayer;
    }

    private void resetPlayers() {
        for (Player player : players) {
            player.reset();
            for (int i = 0; i < Constants.PLAYER_HAND_SIZE; i++) {
                drawWhiteCard(player);
            }
        }
    }

    private void newRound() {

        blackCard = getNewBlackCard();
        playedCards = new ConcurrentHashMap<>();
        czarHand = new Hand();

        System.out.println(Colors.BG_BLACK + Colors.WHITE + " Black Card: " + blackCard.getMessage() + Colors.BG_RESET + Colors.RESET);

        server.broadcastNewRound();

        while (playedCards.size() < players.size() - 1) {
            //waiting for players to play
        }

        server.broadcastCzarRound();

        while (czarHand.getSizeDeck() > players.size() - 2) {
            //waiting for czar to choose card
        }

        //print to server cards played
        for (Card card : playedCards.keySet()) {
            System.out.println(playedCards.get(card).getNickname() + " played White Card: " + card.getMessage());
        }

        //print player who wins
        System.out.println("\n" + Colors.GREEN + winner.getNickname() + Messages.PLAYER_WIN + Colors.RESET);
        server.broadcastMessage("\n" + Colors.GREEN + winner.getNickname() + Messages.PLAYER_WIN + Colors.RESET);

        //prints black card of the round
        System.out.println("\t" + Colors.BG_BLACK + Colors.WHITE + " Black Card: " + blackCard.getMessage() + Colors.BG_RESET + Colors.RESET + "\n");
        server.broadcastMessage("\t" + Colors.BG_BLACK + Colors.WHITE + " Black Card: " + blackCard.getMessage() + Colors.BG_RESET + Colors.RESET + "\n");

        //prints the white card that won
        System.out.println("\t" + Colors.BG_WHITE + Colors.BLACK + " White Card: " + czarCard.getMessage() + Colors.BG_RESET + Colors.RESET);
        server.broadcastMessage("\t" + Colors.BG_WHITE + Colors.BLACK + " White Card: " + czarCard.getMessage() + Colors.BG_RESET + Colors.RESET);

        playersDrawWhiteCards();

        setNextCzar();

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void drawWhiteCard(Player player) {
        if (!player.isCzar()) {
            player.draw(whiteDeck.getCard(Random.getRandomNumber(0, whiteDeck.getSizeDeck())));
        }
    }

    private Card getNewBlackCard() {
        return blackDeck.getCard(Random.getRandomNumber(0, blackDeck.getSizeDeck()));
    }

    private void setNextCzar() {

        for (Player player : players) {

            if (player.isCzar()) {

                //verify and removes the actual czar
                int lastCzar = players.indexOf(player);
                player.setCzar(false);

                //if czar is the last index of the ArrayList, sets czar for the first index (0)
                if (lastCzar == players.size() - 1) {
                    czar = players.get(0);
                    czar.setCzar(true);
                    return;
                }

                //sets the next czar
                czar = players.get(lastCzar + 1);
                czar.setCzar(true);
                return;
            }
        }
    }

    private void playersDrawWhiteCards() {
        for (Player player : players) {
            if (!player.isCzar()) {
                drawWhiteCard(player);
            }
        }
    }

    public void checkRoundWinner(Card choosenCard) {
        czarCard = choosenCard;
        winner = playedCards.get(czarCard);
        winner.roundWon();
    }

    private boolean playerWon() {

        boolean isWinner = false;

        for (Player player : players) {
            if (player.getScore() == Constants.SCORE_TO_WIN) {
                isWinner = true;
                System.out.println(Colors.GREEN + "\n" + player.getNickname() + Messages.PLAYER_WON + Colors.RESET);
                server.broadcastMessage(Colors.GREEN + "\n" + player.getNickname() + Messages.PLAYER_WON + Colors.RESET);
                return isWinner;
            }
        }

        return isWinner;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public Card getBlackCard() {
        return blackCard;
    }

    public Hand getCzarHand() {
        return czarHand;
    }

    public void play(Card card, Player player) {
        czarHand.addCard(card);
        playedCards.put(card, player);
    }

    public void setGameStart(boolean gameStart) {
        this.gameStart = gameStart;
    }

    public boolean isGameStart() {
        return gameStart;
    }

    @Override
    public void run() {
        init();
        while (true) {
            waitingForInstructions();
        }
    }
}