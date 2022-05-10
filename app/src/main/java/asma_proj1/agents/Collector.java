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
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

import asma_proj1.agents.protocols.TradeOffer;
import asma_proj1.agents.protocols.TradeOfferData;
import asma_proj1.agents.protocols.TradeOfferInitiator;
import asma_proj1.card.Card;
import asma_proj1.card.CardSet;
import asma_proj1.card.Rarity;
import asma_proj1.utils.RandomUtils;
import asma_proj1.utils.StringUtils;

public class Collector extends CardOwner {
    private static final int MAX_DESIRED_CARDS = 30, MIN_NEW_CARDS = 2, MAX_NEW_CARDS = 15;
    private Set<Card> desiredCards = new HashSet<>(), desiredNotOwned = new HashSet<>();

    private static final Map<Rarity, Double> rarityValueMap = Map.of(
        Rarity.COMMON, 10.0,
        Rarity.UNCOMMON, 25.0,
        Rarity.RARE, 100.0
    );

    @Override
    protected void setup() {
        super.setup();
        addBehaviour(new CollectorBehaviour(this));
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
    public List<Card> selectCardsForTrade(List<Card> offered) {
        Set<Card> unique = new HashSet<>(offered);
        unique.retainAll(desiredNotOwned);
        return new ArrayList<>(unique);
    }

    private List<Card> unwantedCards() {
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

    private class CollectorBehaviour extends TickerBehaviour {
        private static final int INTERVAL_SECONDS = 15;
        private final Collector collector;

        public CollectorBehaviour(Collector collector) {
            super(collector, INTERVAL_SECONDS * 1000);
            this.collector = collector;
        }

        @Override
        protected void onTick() {
            if (desiredCards.isEmpty()) return; 

            if (!collection.isEmpty()) {
                // Look for possible trades
                Set<AID> agents = new HashSet<>();

                for (Card card : desiredNotOwned) {
                    DFAgentDescription template = new DFAgentDescription();
                    ServiceDescription sd = new ServiceDescription();
                    sd.setType(CardOwner.DF_HAVE_TYPE);
                    sd.setName(String.valueOf(card.getId()));
                    template.addServices(sd);

                    try {
                        DFAgentDescription[] results = DFService.search(myAgent, template);
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

                if (!agents.isEmpty()) {
                    StringUtils.logAgentMessage(myAgent, "📢 Found " + agents.size() +
                    " possible agents to trade with.");

                    List<Card> offered = unwantedCards();
                    addBehaviour(new TradeOfferInitiator(collector,
                        new TradeOfferData(desiredNotOwned, offered), agents));
                }
            }

            // Purchase a pack of the set with the highest number of desired cards
            if (!cardSets.isEmpty()) {
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

                purchasePack(cardSets.get(maxIdx));
            }
        }
    }
}
