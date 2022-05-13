package asma_proj1.agents;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import jade.core.AID;

import asma_proj1.agents.protocols.data.TradeOffer;
import asma_proj1.card.Card;
import asma_proj1.card.CardSet;
import asma_proj1.card.Rarity;
import asma_proj1.utils.RandomUtils;
import asma_proj1.utils.StringUtils;

public class Collector extends CardOwner {
    private static final int MAX_DESIRED_CARDS = 30, MIN_NEW_CARDS = 5, MAX_NEW_CARDS = 15;
    private Set<Card> desiredCards = new HashSet<>(), desiredNotOwned = new HashSet<>();

    // Used solely for evaluating trade offers
    private static final Map<Rarity, Double> rarityValueMap = Map.of(
        Rarity.COMMON, 10.0,
        Rarity.UNCOMMON, 25.0,
        Rarity.RARE, 100.0
    );

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
        newCards = Math.max(0, Math.min(newCards, MAX_DESIRED_CARDS - desiredCards.size()));

        if (newCards > 0) {
            List<Card> newDesired = RandomUtils.sample(set.getCards(), newCards);
            desiredCards.addAll(newDesired);
            desiredNotOwned.addAll(newDesired);

            List<String> ids = StringUtils.cardIds(newDesired);
            StringUtils.logAgentMessage(this, "⭐ Wishes to collect " + ids.size() +
                " new cards: " + StringUtils.colorize(ids.toString(), StringUtils.YELLOW));
        }
    }

    @Override
    protected void handleNewCards(List<Card> cards) {
        List<Card> wanted = new ArrayList<>(), unwanted = new ArrayList<>();

        for (Card card : cards) {
            if (desiredNotOwned.contains(card)) {
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
                StringUtils.colorize(StringUtils.cardIds(wanted).toString(), StringUtils.YELLOW));
        }

        if (!unwanted.isEmpty()) {
            StringUtils.logAgentMessage(this, "📜 Listed " + unwanted.size() + " unwanted cards.");
        }
    }

    @Override
    protected TreeSet<Card> wantedCards() {
        return new TreeSet<>(desiredNotOwned);
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
        return selectAgentsWithCards(desiredNotOwned);
    }

    @Override
    public ArrayList<Card> selectCardsForTrade(ArrayList<Card> offered) {
        Set<Card> unique = new HashSet<>(offered);
        unique.retainAll(desiredNotOwned);
        return new ArrayList<>(unique);
    }

    @Override
    public double evaluateTradeOffer(TradeOffer offer) {
        double value = 0;
        Set<Card> desiredInOffer = new HashSet<>();

        for (Card card : offer.give) {
            desiredInOffer.add(card);
        }
        desiredInOffer.retainAll(desiredNotOwned);

        for (Card card : desiredInOffer) {
            value += rarityValueMap.get(card.getRarity());
        }

        return value;
    }
}
