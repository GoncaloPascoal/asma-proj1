package asma_proj1.agents;

import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.HashSet;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;

import jade.core.behaviours.TickerBehaviour;

import asma_proj1.card.Card;
import asma_proj1.agents.protocols.data.TradeOffer;
import asma_proj1.agents.protocols.data.TradeOfferData;


public class CompetitivePlayer extends CardOwner {
    boolean DEBUG = false;
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

    
    /** 
     * @param cards List of newly acquired cards
     */
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

    /** 
     * @param offered
     * @return List<Card> cards that the agent is willing to give in exchange for others in a trade 
     */
    @Override
    public List<Card> selectCardsForTrade(List<Card> offered) {
        Set<Card> unique = new HashSet<>(offered);
        // retainAll -> removes from 1st all elements that aren't in 2nd
        // removeAll -> removes from 1st all element that are in the 2nd
        unique.removeAll(bestCards);
        return new ArrayList<>(unique);
    }

    /***
     * 
     * @return get list of cards that the agent is willing to get rid off
     */
    private List<Card> disposableCards() {
        List<Card> disposable = new ArrayList<>();

        // TODO what is considered disposable? not in bestCards or under avg power?

        return disposable;
    }
    
    /** 
     * @param data
     * @return TradeOffer
     */
    @Override
    public TradeOffer generateTradeOffer(TradeOfferData data) {
        if (data.offered.isEmpty()) return null;
        List<Card> give = new ArrayList<>(), receive = new ArrayList<>();
        List<Card> canGive = disposableCards();

        // TODO

        return new TradeOffer(give, receive);
    }

    
    /** 
     * @param offer trade offer received
     * @return double value for the agent of the received offer 
     */
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
            if (DEBUG) {
                for (Map.Entry<Integer, Double> entry : setPotentials.entrySet()) {
                    System.out.println("Idx #" + entry.getKey() + " | value: " + entry.getValue());
                }    

                System.out.println("Best set to get: " +
                Collections.max(setPotentials.entrySet(), Comparator.comparingDouble(Map.Entry::getValue)).getKey() +
                " with potential power: " + 
                Collections.max(setPotentials.entrySet(), Comparator.comparingDouble(Map.Entry::getValue)));
            }

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
                // TODO
                // how to access cards of other agents to check their power?



            }

            // Purchase a pack of the set with the highest power but not owned cards
            if (!cardSets.isEmpty()) {
                purchasePack(cardSets.get(evalCardSets()));
            }
            
        }
    }
}
