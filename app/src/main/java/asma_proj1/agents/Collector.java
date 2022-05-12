package asma_proj1.agents;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import jade.core.AID;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

import asma_proj1.agents.protocols.data.Snapshot;
import asma_proj1.agents.protocols.data.TradeOffer;
import asma_proj1.agents.protocols.data.TradeOfferData;
import asma_proj1.agents.protocols.data.Transaction;
import asma_proj1.card.Card;
import asma_proj1.card.CardSet;
import asma_proj1.card.Rarity;
import asma_proj1.utils.RandomUtils;
import asma_proj1.utils.StringUtils;

public class Collector extends CardOwner {
    private static final int MAX_DESIRED_CARDS = 30, MIN_NEW_CARDS = 5, MAX_NEW_CARDS = 15;
    private Set<Card> desiredCards = new HashSet<>(), desiredNotOwned = new HashSet<>();

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
    protected Set<Card> wantedCards() {
        return desiredNotOwned;
    }

    @Override
    protected List<Card> unwantedCards() {
        List<Card> unwanted = new ArrayList<>();

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
        Set<AID> agents = new HashSet<>();

        for (Card card : desiredNotOwned) {
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType(CardOwner.DF_HAVE_TYPE);
            sd.setName(String.valueOf(card.getId()));
            template.addServices(sd);

            try {
                DFAgentDescription[] results = DFService.search(this, template);
                for (DFAgentDescription result : results) {
                    if (result.getName() != getAID()) {
                        agents.add(result.getName());
                    }
                }
            }
            catch (FIPAException e) {
                e.printStackTrace();
            }
        }

        return agents;
    }

    @Override
    public List<Card> selectCardsForTrade(List<Card> offered) {
        Set<Card> unique = new HashSet<>(offered);
        unique.retainAll(desiredNotOwned);
        return new ArrayList<>(unique);
    }
    
    @Override
    public TradeOffer generateTradeOffer(TradeOfferData data) {
        if (data.offered.isEmpty()) return null;
        List<Card> give = new ArrayList<>(), receive = new ArrayList<>();
        List<Card> canGive = unwantedCards();

        Comparator<Card> comparator = Comparator.comparingDouble(c -> data.wanted.contains(c) ? 1 : 0);
        Map<Rarity, PriorityQueue<Card>> rarityPriorityMap = new HashMap<>();
        for (Rarity rarity : Rarity.values()) {
            rarityPriorityMap.put(rarity, new PriorityQueue<>(comparator.reversed()));
        }
        for (Card card : canGive) {
            Rarity rarity = card.getRarity();
            rarityPriorityMap.get(rarity).add(card);
        }

        while (!data.offered.isEmpty() && !canGive.isEmpty()) {
            Card rCard = data.offered.remove(RandomUtils.random.nextInt(data.offered.size()));
            Rarity rarity = rCard.getRarity();

            if (rarityPriorityMap.get(rarity).size() > 0) {
                Card gCard = rarityPriorityMap.get(rarity).poll();

                receive.add(rCard);
                give.add(gCard);
            }
        }

        return new TradeOffer(give, receive);
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

    @Override
    public Transaction selectCardsToBuy() {
        List<Card> cards = new ArrayList<>();
        List<Integer> prices = new ArrayList<>();
        int totalPrice = 0;

        for (Card card : desiredNotOwned) {
            // TODO: Create new constants for probability / ratios
            if (RandomUtils.random.nextDouble() <= 0.25) {
                int maxPrice;
                if (latestSnapshot.containsKey(card)) {
                    Snapshot snapshot = latestSnapshot.get(card);
                    maxPrice = (int) (snapshot.minPrice *
                        RandomUtils.doubleRangeInclusive(1.0, 1.4));
                }
                else {
                    maxPrice = (int) ((double) rarityValueMap.get(card.getRarity()) *
                        RandomUtils.doubleRangeInclusive(1.0, 1.2));
                }

                if (totalPrice + maxPrice <= getCapital()) {
                    totalPrice += maxPrice;
                    cards.add(card);
                    prices.add(maxPrice);
                }
            }
        }

        return new Transaction(cards, prices);
    }
}
