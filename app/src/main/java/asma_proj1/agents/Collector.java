package asma_proj1.agents;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import asma_proj1.card.Card;
import asma_proj1.card.CardSet;
import asma_proj1.utils.RandomUtils;

public class Collector extends CardOwner {
    private static final int MAX_DESIRED_CARDS = 30, MAX_NEW_CARDS = 15;
    private Set<Card> desiredCards = new HashSet<>();

    protected void handleNewCardSet(CardSet set) {
        int newCards = RandomUtils.intRangeInclusive(0, MAX_NEW_CARDS);
        newCards = Math.max(0, Math.min(newCards, MAX_DESIRED_CARDS - desiredCards.size()));

        if (newCards > 0) {
            List<Card> newDesired = RandomUtils.sample(set.getCards(), newCards);
            desiredCards.addAll(newDesired);
        }
    }
}
