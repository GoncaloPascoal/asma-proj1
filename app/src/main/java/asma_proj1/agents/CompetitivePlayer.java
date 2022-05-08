package asma_proj1.agents;

import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

import asma_proj1.agents.protocols.TradeOffer;
import asma_proj1.agents.protocols.TradeOfferData;
import asma_proj1.card.Card;
import asma_proj1.card.CardInstance;

public class CompetitivePlayer extends CardOwner {
    private static final int BEST_MAX_SIZE = 100;
    private final TreeSet<Card> bestCards = new TreeSet<>(
        Comparator.comparingDouble(c -> c.getPower())
    );

    @Override
    protected void handleNewCards(List<CardInstance> cards) {
        for (CardInstance inst : cards) {
            Card card = inst.getCard();

            if (!bestCards.contains(card) && (bestCards.size() < BEST_MAX_SIZE ||
                    card.getPower() > bestCards.first().getPower())) {
                if (bestCards.size() == BEST_MAX_SIZE) {
                    Card oldCard = bestCards.pollFirst();
                    bestCards.add(oldCard);
                    // TODO: list old card
                }
            }
        }
    }

    // TODO: Implement methods
    @Override
    public TradeOffer generateTradeOffer(TradeOfferData data) {
        return null;
    }

    @Override
    public List<CardInstance> selectCardsForTrade(List<CardInstance> offered) {
        return offered;
    }

    @Override
    public double evaluateTradeOffer(TradeOffer offer) {
        return 0;
    }
}
