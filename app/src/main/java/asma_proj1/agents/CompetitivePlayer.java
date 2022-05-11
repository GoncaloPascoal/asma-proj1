package asma_proj1.agents;

import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.Comparator;
import java.util.Collections;

import jade.core.behaviours.TickerBehaviour;

import asma_proj1.card.Card;
import asma_proj1.card.CardSet;
import asma_proj1.utils.StringUtils;
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
        System.out.println("Added competitive behaviour!");
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
        double value = 0;
        
        for (Card card : offer.give) {
            value += card.getPower();
        }
        
        return value;
    }

    private class CompetitiveBehaviour extends TickerBehaviour {
        private static final int INTERVAL_SECONDS = 15;
        private final CompetitivePlayer competitive;

        public CompetitiveBehaviour(CompetitivePlayer competitive) {
            super(competitive, INTERVAL_SECONDS * 1000);
            this.competitive = competitive;
        }

        private int evalCardSets() {
            double setPotential;
            Map<Integer, Double> setPotentials = new HashMap<>();

            // for each card set
            for (int i = 0; i < cardSets.size(); i++) {
                setPotential = 0;

                // add each not owned card's power to get total potential power of set
                for (Card c : cardSets.get(i).getCards()) {
                    if (!collection.containsKey(c)) setPotential += c.getPower();
                }
                setPotentials.put(i, setPotential);
            }
            
            // for debugging
            if (true) {
                for (Map.Entry<Integer, Double> entry : setPotentials.entrySet()) {
                    System.out.println("Idx #" + entry.getKey() + " | value: " + entry.getValue());
                }    
            }
            
            System.out.println("Best set to get: " +
                Collections.max(setPotentials.entrySet(), Comparator.comparingDouble(Map.Entry::getValue)).getKey() +
                " with potential power: " + 
                Collections.max(setPotentials.entrySet(), Comparator.comparingDouble(Map.Entry::getValue)));

            return Collections.max(setPotentials.entrySet(), Comparator.comparingDouble(Map.Entry::getValue)).getKey();
        }

        @Override
        protected void onTick() {

            // if (marketplace == null) {
            //     findMarketplace();
            // }
            // else {
            //     addBehaviour(new SnapshotInitiator(competitive, marketplace));
            // }


            // Look for possible trades
            if (!collection.isEmpty()) {

            }

            // Purchase a pack of the set with the highest power but not owned cards
            if (!cardSets.isEmpty()) {
                purchasePack(cardSets.get(evalCardSets()));
            }
            
        }
    }
}
