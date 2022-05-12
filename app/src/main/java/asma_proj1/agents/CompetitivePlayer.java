package asma_proj1.agents;

import java.util.Set;
import java.util.List;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.ArrayList;
import java.util.Comparator;

import jade.core.AID;

import asma_proj1.card.Card;
import asma_proj1.card.CardSet;
import asma_proj1.agents.protocols.data.TradeOffer;
import asma_proj1.agents.protocols.data.TradeOfferData;

public class CompetitivePlayer extends CardOwner {
    boolean DEBUG = false;
    private static final int BEST_MAX_SIZE = 100;
    private final TreeSet<Card> bestCards = new TreeSet<>(
        Comparator.comparingDouble(c -> c.getPower())
    );
    private CardSet bestSet = null;

    private static double averagePower(CardSet set) {
        double totalPower = 0;
        for (Card card : set.getCards()) {
            totalPower += card.getPower();
        }
        return totalPower / CardSet.SET_SIZE;
    }

    @Override
    protected void handleNewCardSet(CardSet set) {
        if (bestSet == null || averagePower(set) > averagePower(bestSet)) {
            bestSet = set;
        }
    }

    @Override
    protected CardSet selectSet() {
        return bestSet;
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

    @Override
    protected Set<Card> wantedCards() {
        // TODO Auto-generated method stub
        return new HashSet<>();
    }

    @Override
    protected List<Card> unwantedCards() {
        // TODO Auto-generated method stub
        return new ArrayList<>();
    }

    @Override
    protected Set<AID> selectAgentsForTrade() {
        return new HashSet<>();
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
}
