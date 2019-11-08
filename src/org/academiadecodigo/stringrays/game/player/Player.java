package org.academiadecodigo.stringrays.game.player;


import org.academiadecodigo.stringrays.game.cards.Card;
import org.academiadecodigo.stringrays.game.cards.Deck;

public class Player {

    private String nickname;
    private boolean isCzar;

    private Deck hand;

    public Player() {
        this.hand = new Deck();
    }


    public void draw(){

        //draw from the white deck and insert into player's hand
        //hand.addCard(whiteDeck.getCard());

    }

    public Card choose(int index){

        //choose the index one of the cards from the hand deck
        //return the card to the game choices

        return hand.getCard(index);
    }


    public Card play(int index){

        draw();

        return choose(index);
    }

    public boolean isCzar() {
        return isCzar;
    }

    public void setCzar(boolean czar) {
        isCzar = czar;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
}