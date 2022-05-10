package asma_proj1.agents.protocols.data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import asma_proj1.card.Card;

public class Transaction implements Serializable {
    public final List<Card> cards;
    public final Map<Card, Integer> priceMap;

    public Transaction(List<Card> cards, Map<Card, Integer> priceMap) {
        this.cards = cards;
        this.priceMap = priceMap;
    }
}
