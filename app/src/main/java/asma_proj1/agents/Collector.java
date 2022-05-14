package asma_proj1.agents;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jade.core.AID;

import asma_proj1.agents.protocols.data.TradeOffer;
import asma_proj1.card.Card;
import asma_proj1.card.CardSet;
import asma_proj1.card.CardSource;
import asma_proj1.card.Rarity;
import asma_proj1.utils.LogPriority;
import asma_proj1.utils.RandomUtils;
import asma_proj1.utils.StringUtils;

public class Collector extends CardOwner {
    private static final int MIN_NEW_CARDS = 10, MAX_NEW_CARDS = 25;
    private Set<Card> desiredCards = new HashSet<>();
    private LinkedHashMap<Card, Long> desiredNotOwned = new LinkedHashMap<>();

    // Used solely for evaluating trade offers
    private static final Map<Rarity, Double> rarityValueMap = Map.of(
        Rarity.COMMON, 1.0,
        Rarity.UNCOMMON, 2.5,
        Rarity.RARE, 10.0
    );

    // Statistics
    private final Map<CardSource, Integer> sourceMap = new HashMap<>();

    @Override
    protected CardSet selectSet() {
        // Purchase a pack of the set with the highest number of desired cards
        Map<Integer, Integer> numDesired = new HashMap<>();

        for (Card card : desiredCards) {
            int setIdx = card.getId() / CardSet.SET_SIZE;
            numDesired.compute(setIdx, (k, v) -> v == null ? 1 : v + 1);
        }

        int maxIdx = 0;
        for (Map.Entry<Integer, Integer> entry : numDesired.entrySet()) {
            if (entry.getValue() > numDesired.get(maxIdx)) {
                maxIdx = entry.getKey();
            }
        }

        return cardSets.get(maxIdx);
    }

    @Override
    protected void handleNewCardSet(CardSet set) {
        int newCards = RandomUtils.intRangeInclusive(MIN_NEW_CARDS, MAX_NEW_CARDS);

        if (newCards > 0) {
            List<Card> newDesired = RandomUtils.sample(set.getCards(), newCards);
            desiredCards.addAll(newDesired);
            for (Card card : newDesired) {
                desiredNotOwned.put(card, System.nanoTime());
            }

            List<String> ids = StringUtils.cardIds(newDesired);
            StringUtils.logAgentMessage(this, "⭐ Wishes to collect " + ids.size() +
                " new cards: " + StringUtils.colorize(ids.toString(), StringUtils.YELLOW),
                LogPriority.HIGH);
        }
    }

    @Override
    protected void handleNewCards(List<Card> cards, CardSource source) {
        List<Card> wanted = new ArrayList<>(), unwanted = new ArrayList<>();

        for (Card card : cards) {
            if (desiredNotOwned.containsKey(card)) {
                wanted.add(card);
                desiredNotOwned.remove(card);
            }
            else {
                // Duplicate, or not interested in this card
                unwanted.add(card);
            }
        }

        listCards(unwanted);

        if (!wanted.isEmpty()) {
            StringUtils.logAgentMessage(this, "🍀 Got new wanted cards: " +
                StringUtils.colorize(StringUtils.cardIds(wanted).toString(), StringUtils.YELLOW),
                LogPriority.HIGH);
            sourceMap.compute(source, (k, v) -> v == null ? wanted.size() : v + wanted.size());
        }

        if (!unwanted.isEmpty()) {
            StringUtils.logAgentMessage(this, "📜 Listed " + unwanted.size() + " unwanted cards.");
        }
    }

    @Override
    protected LinkedHashSet<Card> wantedCards() {
        LinkedList<Card> wanted = new LinkedList<>();
        /* Insert cards in reverse order (to give more priority to cards that
           have been desired for a longer period of time) */
        for (Card card : desiredNotOwned.keySet()) {
            wanted.addFirst(card);
        }
        return new LinkedHashSet<>(wanted);
    }

    @Override
    protected ArrayList<Card> unwantedCards() {
        ArrayList<Card> unwanted = new ArrayList<>();

        for (Map.Entry<Card, Integer> entry : collection.entrySet()) {
            if (!desiredCards.contains(entry.getKey())) {
                unwanted.addAll(Collections.nCopies(entry.getValue(), entry.getKey()));
            }
            else if (entry.getValue() > 1) {
                unwanted.addAll(Collections.nCopies(entry.getValue() - 1, entry.getKey()));
            }
        }

        return unwanted;
    }

    @Override
    protected Set<AID> selectAgentsForTrade() {
        return selectAgentsWithCards(desiredNotOwned.keySet());
    }

    @Override
    public ArrayList<Card> selectCardsForTrade(ArrayList<Card> offered) {
        Set<Card> unique = new HashSet<>(offered);
        unique.retainAll(desiredNotOwned.keySet());
        return new ArrayList<>(unique);
    }

    @Override
    public double evaluateTradeOffer(TradeOffer offer) {
        double value = 0;
        Set<Card> desiredInOffer = new HashSet<>();

        for (Card card : offer.give) {
            desiredInOffer.add(card);
        }
        desiredInOffer.retainAll(desiredNotOwned.keySet());

        for (Card card : desiredInOffer) {
            value += rarityValueMap.get(card.getRarity());
        }

        value += calculatePriceDelta(offer);
        return value;
    }

    @Override
    protected int evaluateMaxBuyPrice(Card card) {
        if (!desiredNotOwned.containsKey(card)) return 0;

        double elapsedSeconds = System.nanoTime() - desiredNotOwned.get(card) / 1e9;
        double base;

        if (latestSnapshot.containsKey(card) && latestSnapshot.get(card).minPrice != null) {
            base = (latestSnapshot.get(card).minPrice *
                RandomUtils.doubleRangeInclusive(1.1, 1.4));
        }
        else {
            base = (basePrice.get(card.getRarity()) *
                RandomUtils.doubleRangeInclusive(1.0, 1.2));
        }

        double desireFactor = 1.4 - 0.4 / Math.exp(0.005 * elapsedSeconds);
        return (int) (base * desireFactor);
    }
}
