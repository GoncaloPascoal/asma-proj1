package asma_proj1.agents;

import java.util.HashMap;
import java.util.Map;

import jade.core.Agent;

import asma_proj1.card.Card;

public class CardDatabase extends Agent {
    private Map<Integer, Card> cards = new HashMap<>();

    public Card getCardById(int id) {
        return cards.get(id);
    }
}
