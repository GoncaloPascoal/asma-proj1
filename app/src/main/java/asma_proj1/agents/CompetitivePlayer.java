package asma_proj1.agents;

import java.util.Set;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;

import jade.core.AID;

import asma_proj1.card.Card;
import asma_proj1.card.CardSet;
import asma_proj1.utils.RandomUtils;
import asma_proj1.utils.StringUtils;
import asma_proj1.agents.protocols.data.TradeOffer;
import asma_proj1.agents.protocols.data.TradeOfferData;

public class CompetitivePlayer extends CardOwner {
    private static final int BEST_MAX_SIZE = 100;
    private static final Comparator<Card> cardPowerComparator = Comparator.comparingDouble(
        c -> c.getPower()
    );
    private final TreeSet<Card> bestCards = new TreeSet<>(cardPowerComparator),
        potentiallyBetterCards = new TreeSet<>(cardPowerComparator);

    private CardSet bestSet = null;

    private static double averagePower(CardSet set) {
        double totalPower = 0;
        for (Card card : set.getCards()) {
            totalPower += card.getPower();
        }
        return totalPower / CardSet.SET_SIZE;
    }

    private static double averageCollectionPower(Map<Card, Integer> collection) {
        double totalPower = 0;
        for (Map.Entry<Card,Integer> entry : collection.entrySet()) {
            totalPower += entry.getKey().getPower() * entry.getValue();
        }
        return totalPower / collection.values().stream().mapToInt(Integer::intValue).sum();
    }

    @Override
    protected void handleNewCardSet(CardSet set) {
        if (bestSet == null || averagePower(set) > averagePower(bestSet)) {
            bestSet = set;
        }

        for (Card card : set.getCards()) {
            if (bestCards.isEmpty() || card.getPower() >= bestCards.first().getPower()) {
                potentiallyBetterCards.add(card);
            }
        }
    }

    @Override
    protected CardSet selectSet() {
        return bestSet;
    }

    @Override
    protected void handleNewCards(List<Card> cards) {
        TreeSet<Card> wanted = new TreeSet<>(cardPowerComparator);
        List<Card> unwanted = new ArrayList<>();

        for (Card card : cards) {
            if (!bestCards.contains(card) && (bestCards.size() < BEST_MAX_SIZE ||
                    card.getPower() > bestCards.first().getPower())) {
                if (bestCards.size() == BEST_MAX_SIZE) {
                    unwanted.add(bestCards.pollFirst());
                }
                wanted.add(card);
                bestCards.add(card);
            }
            else {
                unwanted.add(card);
            }
        }

        if (!bestCards.isEmpty()) {
            while (!potentiallyBetterCards.isEmpty() &&
                    potentiallyBetterCards.first().getPower() < bestCards.first().getPower()) {
                potentiallyBetterCards.pollFirst();
            }
        }

        listCards(unwanted);

        if (!wanted.isEmpty()) {
            StringUtils.logAgentMessage(this, "🍀 Got " + wanted.size() + " more powerful cards. Card power range: " +
                StringUtils.colorize(String.format("[%.3f, %.3f]", bestCards.first().getPower(), bestCards.last().getPower()), StringUtils.YELLOW));
        }

        if (!unwanted.isEmpty()) {
            StringUtils.logAgentMessage(this, "📜 Listed " + unwanted.size() + " unwanted cards.");
        }
    }

    @Override
    protected Set<Card> wantedCards() {
        return potentiallyBetterCards;
    }

    @Override
    protected List<Card> unwantedCards() {
        List<Card> unwanted = new ArrayList<>();

        for (Map.Entry<Card, Integer> entry : collection.entrySet()) {
            Card card = entry.getKey();
            int copies = entry.getValue();

            if (bestCards.contains(card)) copies -= 1;

            for (int i = 0; i < copies; ++i) {
                unwanted.add(card);
            }
        }

        return unwanted;
    }

    @Override
    protected Set<AID> selectAgentsForTrade() {
        return selectAgentsWithCards(potentiallyBetterCards);
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

    /**
     * 
     * @return set of all the cards the agent would consider useful to improve the collection
     */
    private TreeSet<Card> allCardsWanted() {

        TreeSet<Card> allCards = new TreeSet<>();

        for (CardSet cardSet : cardSets) {
            for (Card card : cardSet.getCards()) allCards.add(card);
        }

        allCards.removeAll(new TreeSet<>(collection.keySet())); // removing all cards already owned

        if (bestCards.size() < BEST_MAX_SIZE) {
            allCards.removeIf(c -> (c.getPower() < averageCollectionPower(collection))); // remove all cards that have power under the current collection average
        }
        else { // when bestCards is full
            Set<Double> powers = new HashSet<>();
            for (Card c : bestCards) powers.add(c.getPower());
            allCards.removeIf(c -> (c.getPower() < Collections.min(powers))); // remove all cards that have power under the lowest found in bestCards
        }

        return allCards;
    }
    
    /** 
     * @param data
     * @return TradeOffer
     */
    @Override
    public TradeOffer generateTradeOffer(TradeOfferData data) {
        if (data.offered.isEmpty()) return null;
        List<Card> give = new ArrayList<>(), receive = new ArrayList<>();
        List<Card> canGive = unwantedCards();
        
        // keep in data.offered all cards that are acceptable for the agent
        data.offered.retainAll(allCardsWanted());

        // Not yet tested
        while (!data.offered.isEmpty() && !canGive.isEmpty()) {

            Card rCard = data.offered.remove(RandomUtils.random.nextInt(data.offered.size()));
            Card gCard = canGive.remove(RandomUtils.random.nextInt(data.offered.size()));

            receive.add(rCard);
            give.add(gCard);
        }

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
