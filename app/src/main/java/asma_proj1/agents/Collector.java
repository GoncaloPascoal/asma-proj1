package asma_proj1.agents;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
import asma_proj1.card.CardInstance;
import asma_proj1.card.CardSet;
import asma_proj1.utils.RandomUtils;
import asma_proj1.utils.StringUtils;

public class Collector extends CardOwner {
    private static final int MAX_DESIRED_CARDS = 30, MIN_NEW_CARDS = 2, MAX_NEW_CARDS = 15;
    private Set<Card> desiredCards = new HashSet<>(), ownedDesiredCards = new HashSet<>();

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

            List<String> ids = StringUtils.cardIds(newDesired);
            StringUtils.logAgentMessage(this, "⭐ Wishes to collect " + ids.size() +
                " new cards: " + StringUtils.colorize(ids.toString(), StringUtils.YELLOW));

            List<CardInstance> wanted = newDesired.stream()
                .map(c -> new CardInstance(c, false))
                .collect(Collectors.toList());

            listCards(wanted, DF_WANT_TYPE);
        }
    }

    @Override
    protected void handleNewCards(List<CardInstance> cards) {
        List<CardInstance> wanted = new ArrayList<>(), unwanted = new ArrayList<>();

        for (CardInstance inst : cards) {
            Card card = inst.getCard();

            if (desiredCards.contains(card) && !ownedDesiredCards.contains(card)) {
                wanted.add(inst);
                ownedDesiredCards.add(card);
            }
            else {
                // Duplicate, or not interested in this card
                unwanted.add(inst);
            }
        }

        unlistCards(wanted, CardOwner.DF_WANT_TYPE);
        listCards(unwanted, CardOwner.DF_HAVE_TYPE);

        if (!wanted.isEmpty()) {
            StringUtils.logAgentMessage(this, "🍀 Got new wanted cards: " +
                StringUtils.colorize(StringUtils.cardIds(wanted).toString(), StringUtils.YELLOW));
        }

        if (!unwanted.isEmpty()) {
            StringUtils.logAgentMessage(this, "📜 Listed " + unwanted.size() + " unwanted cards.");
        }
    }

    @Override
    public List<CardInstance> selectCardsForTrade(List<CardInstance> offered) {
        Set<Card> relevantCards = new HashSet<>();
        List<CardInstance> cardsForTrade = new ArrayList<>();

        for (CardInstance inst : offered) {
            Card card = inst.getCard();

            if (desiredCards.contains(card) && !relevantCards.contains(card)) {
                relevantCards.add(card);
                cardsForTrade.add(inst);
            }
        }

        return cardsForTrade;
    }

    private List<CardInstance> unwantedCards() {
        List<CardInstance> unwanted = new ArrayList<>();

        for (Map.Entry<CardInstance, Integer> entry : collection.entrySet()) {
            if (!desiredCards.contains(entry.getKey().getCard())) {
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
        List<CardInstance> give = new ArrayList<>(), receive = new ArrayList<>();
        List<CardInstance> canGive = unwantedCards();

        while (!data.offered.isEmpty() && !canGive.isEmpty()) {
            CardInstance rInst = data.offered.remove(RandomUtils.random.nextInt(data.offered.size()));

            List<CardInstance> candidates = canGive.stream()
                .filter(c -> c.getCard().getRarity() == rInst.getCard().getRarity())
                .collect(Collectors.toList());

            if (candidates.size() > 0) {
                CardInstance gInst = candidates.get(RandomUtils.random.nextInt(candidates.size()));
                canGive.remove(gInst);

                receive.add(rInst);
                give.add(gInst);
            }
        }

        return new TradeOffer(give, receive);
    }

    @Override
    public double evaluateTradeOffer(TradeOffer offer) {
        return 0;
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

            // Look for possible trades
            Set<AID> haveAgents = new HashSet<>();

            for (Card card : desiredCards) {
                DFAgentDescription template = new DFAgentDescription();
                ServiceDescription sd = new ServiceDescription();
                sd.setType(CardOwner.DF_HAVE_TYPE);
                sd.setName(String.valueOf(card.getId()));
                template.addServices(sd);

                try {
                    DFAgentDescription[] results = DFService.search(myAgent, template);
                    for (DFAgentDescription result : results) {
                        if (result.getName() != getAID()) {
                            haveAgents.add(result.getName());
                        }
                    }
                }
                catch (FIPAException e) {
                    e.printStackTrace();
                }
            }

            if (!haveAgents.isEmpty()) {
                StringUtils.logAgentMessage(myAgent, "📢 Found " + haveAgents.size() +
                " possible agents to trade with.");

                List<Card> wanted = new ArrayList<>();
                wanted.addAll(desiredCards);
                wanted.removeAll(ownedDesiredCards);
                List<CardInstance> offered = unwantedCards();
                addBehaviour(new TradeOfferInitiator(collector, new TradeOfferData(wanted, offered), haveAgents));
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
