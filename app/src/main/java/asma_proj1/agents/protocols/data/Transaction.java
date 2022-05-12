package asma_proj1.agents.protocols.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import asma_proj1.card.Card;

public class Transaction implements Serializable {
    public final List<Card> cards;
    public final List<Integer> prices;

    public Transaction(List<Card> cards, Map<Card, Integer> priceMap) {
        this.cards = cards;
        List<Integer> prices = new ArrayList<>();
        for (Card card : cards) {
            prices.add(priceMap.get(card));
        }
        this.prices = prices;
    }

    public Transaction(List<Card> cards, List<Integer> prices) {
        this.cards = cards;
        this.prices = prices;
    }

    public boolean isEmpty() {
        return cards.isEmpty();
    }

    public int totalPrice() {
        return prices.stream().mapToInt(v -> v).sum();
    }
}
