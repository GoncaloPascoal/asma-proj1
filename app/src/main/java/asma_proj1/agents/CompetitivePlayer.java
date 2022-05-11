package asma_proj1.agents;

import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

import jade.core.behaviours.TickerBehaviour;

import asma_proj1.card.Card;
import asma_proj1.agents.protocols.data.TradeOffer;
import asma_proj1.agents.protocols.SnapshotInitiator;
import asma_proj1.agents.protocols.data.TradeOfferData;

public class CompetitivePlayer extends CardOwner {
    private static final int BEST_MAX_SIZE = 100;
    private final TreeSet<Card> bestCards = new TreeSet<>(
        Comparator.comparingDouble(c -> c.getPower())
    );

    @Override
    protected void setup() {
        super.setup();
        addBehaviour(new CompetitiveBehaviour(this));
    }

    @Override
    protected void handleNewCards(List<Card> cards) {
        for (Card card : cards) {
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

    @Override
    public List<Card> selectCardsForTrade(List<Card> offered) {
        return offered;
    }

    @Override
    public TradeOffer generateTradeOffer(TradeOfferData data) {
        return null;
    }

    @Override
    public double evaluateTradeOffer(TradeOffer offer) {
        return 0;
    }

    private class CompetitiveBehaviour extends TickerBehaviour {
        private static final int INTERVAL_SECONDS = 15;
        private final CompetitivePlayer competitive;

        public CompetitiveBehaviour(CompetitivePlayer competitive) {
            super(competitive, INTERVAL_SECONDS * 1000);
            this.competitive = competitive;
        }

        @Override
        protected void onTick() {

            if (marketplace == null) {
                findMarketplace();
            }
            else {
                addBehaviour(new SnapshotInitiator(competitive, marketplace));
            }




            // Purchase a pack of the set with the highest power cards

            purchasePack(cardSets.get(maxIdx));

        }
    }
}
