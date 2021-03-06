package org.academiadecodigo.stringrays.cadetsagainsthumanity.game.cards;

public class Hand extends Deck {

    public String[] getCardMessages() {
        String[] cardMessages = new String[deck.size()];
        for (Card card : deck) {
            cardMessages[deck.indexOf(card)] = card.getMessage();
        }
        return cardMessages;
    }
}
