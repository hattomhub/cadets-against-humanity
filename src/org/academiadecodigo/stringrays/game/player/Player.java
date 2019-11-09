package org.academiadecodigo.stringrays.game.player;

import org.academiadecodigo.stringrays.constants.Constants;
import org.academiadecodigo.stringrays.constants.Messages;
import org.academiadecodigo.stringrays.game.cards.Card;
import org.academiadecodigo.stringrays.game.cards.Deck;
import org.academiadecodigo.stringrays.game.cards.Hand;
import org.academiadecodigo.stringrays.network.PlayerHandler;

import java.util.Collection;
import java.util.Set;

public class Player {

    private String nickname;
    private boolean isCzar;
    private boolean alreadyPlayed;
    private int score = 0;
    private Hand hand;
    private PlayerHandler playerHandler;
    private boolean ready;

    public Player() {
        this.hand = new Hand();
    }

    public void draw(Card card) {
        hand.addCard(card);

        //draw from the white deck and insert into player's hand
        //hand.addCard(whiteDeck.getCard());

    }

    public Card chooseWhiteCard(Card blackCard) {
        int cardIndex = playerHandler.chooseCard(blackCard.getMessage(), getCardMessages(), Messages.PLAYER_TURN_MESSAGE);
        return getCard(cardIndex - Constants.CONVERT_PROMPT_VIEW_INDEX);
    }

    public Card chooseWinner(Card blackCard, Hand czarHand) {
        int index = 1; //playerHandler.chooseCard(blackCard.getMessage(), getCardMessages(), Messages.PLAYER_TURN_MESSAGE);
        Card czarChosenCard = czarHand.getCard(index - Constants.CONVERT_PROMPT_VIEW_INDEX);
        System.out.println("Black Card: " + blackCard.getMessage());
        System.out.println("Czar " + getNickname() + " chose: " + czarChosenCard.getMessage());
        return czarChosenCard;
    }

    public void roundWon() {
        score++;
    }

    public Card getCard(int index) {
        return hand.getCard(index);
    }

    public boolean isCzar() {
        return isCzar;
    }

    public void setCzar(boolean czar) {
        isCzar = czar;
    }

    public boolean alreadyPlayed() {
        return alreadyPlayed;
    }

    public void waitForOthers(String message) {
        System.out.println(message);
        //playerHandler.sendMessageToPlayer(message);
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String[] getCardMessages() {
        return hand.getCardMessages();
    }



    public void setPlayerHandler(PlayerHandler playerHandler) {
        this.playerHandler = playerHandler;
    }

    public boolean isReady() {
        return ready;
    }

    public void setReady(boolean ready) {
        System.out.println(getNickname() + " is ready!");
        this.ready = ready;
    }

    public int getScore() {
        return score;
    }
}
