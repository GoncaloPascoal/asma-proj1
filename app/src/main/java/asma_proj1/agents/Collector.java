package asma_proj1.agents;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import asma_proj1.card.Card;
import asma_proj1.card.CardInstance;
import asma_proj1.card.CardSet;
import asma_proj1.utils.RandomUtils;
import asma_proj1.utils.StringUtils;

public class Collector extends CardOwner {
    private static final int MAX_DESIRED_CARDS = 30, MAX_NEW_CARDS = 15;
    private Set<Card> desiredCards = new HashSet<>();

    @Override
    protected void handleNewCardSet(CardSet set) {
        List<Integer> ids = new ArrayList<>();

        int newCards = RandomUtils.intRangeInclusive(0, MAX_NEW_CARDS);
        newCards = Math.max(0, Math.min(newCards, MAX_DESIRED_CARDS - desiredCards.size()));

        if (newCards > 0) {
            List<Card> newDesired = RandomUtils.sample(set.getCards(), newCards);
            desiredCards.addAll(newDesired);

            ids.addAll(newDesired.stream().mapToInt(c -> c.getId()).boxed()
                .collect(Collectors.toList()));
        }

        StringUtils.logAgentMessage(this, "⭐ Wishes to collect " + ids.size() +
            " new cards: " + StringUtils.colorize(ids.toString(), StringUtils.YELLOW));
    }

    @Override
    protected void handleNewCardPack(List<CardInstance> pack) {
        for (CardInstance inst : pack) {
            Card card = inst.getCard();

            if (desiredCards.contains(card)) {
                desiredCards.remove(card);
            }
            else {
                // Duplicate, or not interested in this card
                listCardForTrade(inst);
            }
        }
    }
}
