package asma_proj1.agents;

import java.util.Set;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Collections;

import jade.core.AID;

import asma_proj1.card.Card;
import asma_proj1.card.CardSet;
import asma_proj1.utils.RandomUtils;
import asma_proj1.utils.StringUtils;
import asma_proj1.agents.protocols.data.Snapshot;
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

    /**
     * Returns whether or not a card would increase the power of the player's collection.
     */
    private boolean isCardMorePowerful(Card card) {
        return bestCards.size() < BEST_MAX_SIZE || card.getPower() >= bestCards.first().getPower();
    }

    @Override
    protected void handleNewCardSet(CardSet set) {
        if (bestSet == null || averagePower(set) > averagePower(bestSet)) {
            bestSet = set;
        }

        for (Card card : set.getCards()) {
            if (isCardMorePowerful(card)) {
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
            if (!bestCards.contains(card) && isCardMorePowerful(card)) {
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
    protected LinkedHashSet<Card> wantedCards() {
        return new LinkedHashSet<>(potentiallyBetterCards);
    }

    @Override
    protected ArrayList<Card> unwantedCards() {
        ArrayList<Card> unwanted = new ArrayList<>();

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

    @Override
    public ArrayList<Card> selectCardsForTrade(ArrayList<Card> offered) {
        offered.retainAll(potentiallyBetterCards);
        return offered;
    }

    @Override
    public TradeOffer generateTradeOffer(TradeOfferData data) {
        Collections.sort(data.offered, cardPowerComparator);
        return super.generateTradeOffer(data);
    }

    @Override
    public double evaluateTradeOffer(TradeOffer offer) {
        double value = 0;

        // TODO: include marketplace price in calculation as a secondary factor?
        for (Card card : offer.give) {
            if (isCardMorePowerful(card)) {
                value += card.getPower();
            }
        }

        return value;
    }

    @Override
    protected int evaluateMaxBuyPrice(Card card) {
        if (!potentiallyBetterCards.contains(card)) return 0;

        double base;

        if (latestSnapshot.containsKey(card)) {
            Snapshot snapshot = latestSnapshot.get(card);
            base = (snapshot.minPrice *
                RandomUtils.doubleRangeInclusive(1.1, 1.4));
        }
        else {
            base = (basePrice.get(card.getRarity()) *
                RandomUtils.doubleRangeInclusive(1.0, 1.2));
        }

        double desireFactor = Math.max(
            1.0,
            1.4 - 0.4 / Math.exp(0.4 * card.getPower())
        );
        return (int) (base * desireFactor);
    }
}
