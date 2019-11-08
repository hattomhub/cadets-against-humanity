package org.academiadecodigo.stringrays.game.cards;

import java.util.Vector;

public class Deck {

    private Vector<Card> deck;

    public Deck() {
        deck = new Vector<>();
    }

    public Card getCard(int index) {
        return deck.remove(index);
    }

    public void addCard(Card card) {
        deck.add(card);
    }




}