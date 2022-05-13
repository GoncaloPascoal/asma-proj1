package asma_proj1.agents;

import java.util.Set;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.TreeSet;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Collections;

import jade.core.AID;

import asma_proj1.card.Card;
import asma_proj1.card.CardSet;
import asma_proj1.card.Rarity;
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

    @Override
    public List<Card> selectCardsForTrade(List<Card> offered) {
        offered.retainAll(potentiallyBetterCards);
        return offered;
    }

    @Override
    public TradeOffer generateTradeOffer(TradeOfferData data) {
        if (data.offered.isEmpty()) return null;
        List<Card> give = new ArrayList<>(), receive = new ArrayList<>();
        List<Card> canGive = unwantedCards();

        Collections.sort(data.offered, cardPowerComparator);

        // TODO: refactor this
        Comparator<Card> comparator = Comparator.comparingDouble(c -> data.wanted.contains(c) ? 1 : 0);
        Map<Rarity, PriorityQueue<Card>> rarityPriorityMap = new HashMap<>();
        for (Rarity rarity : Rarity.values()) {
            rarityPriorityMap.put(rarity, new PriorityQueue<>(comparator.reversed()));
        }
        for (Card card : canGive) {
            Rarity rarity = card.getRarity();
            rarityPriorityMap.get(rarity).add(card);
        }

        // Not yet tested
        while (!data.offered.isEmpty() && !canGive.isEmpty()) {
            Card rCard = data.offered.remove(data.offered.size() - 1);
            Rarity rarity = rCard.getRarity();

            if (rarityPriorityMap.get(rarity).size() > 0) {
                Card gCard = rarityPriorityMap.get(rarity).poll();

                receive.add(rCard);
                give.add(gCard);
            }
        }

        if (give.isEmpty() || receive.isEmpty()) {
            return null;
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
