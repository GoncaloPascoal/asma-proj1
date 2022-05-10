package asma_proj1.agents;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import jade.core.AID;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.Property;
import jade.domain.FIPAAgentManagement.ServiceDescription;

import asma_proj1.agents.protocols.Snapshot;
import asma_proj1.agents.protocols.TradeOffer;
import asma_proj1.agents.protocols.TradeOfferData;
import asma_proj1.agents.protocols.TradeOfferResponder;
import asma_proj1.card.Card;
import asma_proj1.card.CardSet;
import asma_proj1.utils.ConversionUtils;
import asma_proj1.utils.RandomUtils;
import asma_proj1.utils.StringUtils;

public abstract class CardOwner extends BaseAgent {
    public static final String DF_HAVE_TYPE = "have";

    protected final Map<Card, Integer> collection = new HashMap<>();
    private final Map<Integer, Integer> cardsForTrade = new HashMap<>();
    public final Lock collectionLock = new ReentrantLock();

    protected final DFAgentDescription dfd = new DFAgentDescription();
    protected AID marketplace = null;
    protected Map<Card, Snapshot> latestSnapshot = new HashMap<>();

    @Override
    protected void setup() {
        super.setup();

        // Trading (yellow pages) setup
        dfd.setName(getAID());
        try {
            DFService.register(this, dfd);
        }
        catch (FIPAException e) {
            e.printStackTrace();
        }
        addBehaviour(new TradeOfferResponder(this));

        receiveCapital();
        addBehaviour(new ReceiveCapital(this));

        findMarketplace();
    }

    protected abstract void handleNewCards(List<Card> cards);
    public abstract List<Card> selectCardsForTrade(List<Card> offered);
    public abstract TradeOffer generateTradeOffer(TradeOfferData data);
    public abstract double evaluateTradeOffer(TradeOffer offer);

    public Map<Card, Integer> getCollection() {
        return Collections.unmodifiableMap(collection);
    }

    public void setLatestSnapshot(Map<Card, Snapshot> latestSnapshot) {
        this.latestSnapshot = latestSnapshot;
    }

    public boolean cardsInCollection(Collection<Card> cards) {
        Map<Card, Integer> countMap = ConversionUtils.collectionToCountMap(cards);

        for (Map.Entry<Card, Integer> entry : countMap.entrySet()) {
            if (collection.getOrDefault(entry.getKey(), 0) < entry.getValue()) {
                return false;
            }
        }

        return true;
    }

    protected void receiveCapital() {
        int capital = RandomUtils.intRangeInclusive(50, 100);
        changeCapital(capital);
        StringUtils.logAgentMessage(this, "Received periodic capital: " +
            changeCapitalMessage(capital));
    }

    protected void purchasePack(CardSet set) {
        if (changeCapital(-CardSet.PACK_PRICE)) {
            StringUtils.logAgentMessage(this, "Purchased a card pack: " + changeCapitalMessage(-CardSet.PACK_PRICE));

            List<Card> pack = set.openPack();
            addCardsToCollection(pack);
        }
    }

    public void addCardsToCollection(List<Card> cards) {
        for (Card card : cards) {
            collection.compute(card, (k, v) -> v == null ? 1 : v + 1);
        }
        handleNewCards(cards);
    }

    public void removeCardsFromCollection(List<Card> cards) {
        for (Card card : cards) {
            collection.compute(card, (k, v) -> v == null || v == 1 ? null : v - 1);
        }
        unlistCards(cards);
    }

    protected void updateDfd() {
        try {
            DFService.modify(this, dfd);
        }
        catch (FIPAException e) {
            e.printStackTrace();
        }
    }

    protected void listCards(List<Card> cards) {
        Map<Integer, Integer> countMap = new HashMap<>();
        for (Card card : cards) {
            countMap.compute(card.getId(), (k, v) -> v == null ? 1 : v + 1);
        }

        for (Map.Entry<Integer, Integer> entry : countMap.entrySet()) {
            cardsForTrade.compute(entry.getKey(), (k, v) -> v == null ? entry.getValue() : v + entry.getValue());
        }

        Iterator<?> it = dfd.getAllServices();
        while (it.hasNext()) {
            ServiceDescription sd = (ServiceDescription) it.next();

            if (sd.getType() == DF_HAVE_TYPE) {
                int id = Integer.valueOf(sd.getName());

                if (countMap.containsKey(id)) {
                    sd.removeProperties(new Property("count", null));
                    sd.addProperties(new Property("count", cardsForTrade.get(id)));
                    countMap.remove(id);
                }
            }
        }

        for (Map.Entry<Integer, Integer> entry : countMap.entrySet()) {
            int id = entry.getKey();
            ServiceDescription sd = new ServiceDescription();
            sd.setType(DF_HAVE_TYPE);
            sd.setName(String.valueOf(id));
            sd.addProperties(new Property("count", cardsForTrade.get(id)));
            dfd.addServices(sd);
        }

        updateDfd();
    }

    protected void unlistCards(List<Card> cards) {
        for (Card card : cards) {
            cardsForTrade.compute(card.getId(), (k, v) -> v == null || v == 1 ? null : v - 1);
        }

        Iterator<?> it = dfd.getAllServices();
        while (it.hasNext()) {
            ServiceDescription sd = (ServiceDescription) it.next();

            if (sd.getType() == DF_HAVE_TYPE) {
                int id = Integer.valueOf(sd.getName());

                if (cardsForTrade.containsKey(id)) {
                    sd.removeProperties(new Property("count", null));
                    sd.addProperties(new Property("count", cardsForTrade.get(id)));
                }
                else {
                    it.remove();
                }
            } 
        }

        updateDfd();
    }

    protected void findMarketplace() {
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType(Marketplace.SERVICE_TYPE);

        try {
            DFAgentDescription[] results = DFService.search(this, template);
            if (results.length > 0) {
                marketplace = results[0].getName();
            }
        }
        catch (FIPAException e) {
            e.printStackTrace();
        }
    }

    private class ReceiveCapital extends TickerBehaviour {
        private static final int INTERVAL_SECONDS = 25;
        
        public ReceiveCapital(CardOwner cardOwner) {
            super(cardOwner, INTERVAL_SECONDS * 1000);
        }

        @Override
        protected void onTick() {
            receiveCapital();
        }
    }
}
