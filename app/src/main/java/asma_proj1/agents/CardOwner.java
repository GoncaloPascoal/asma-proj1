package asma_proj1.agents;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.Property;
import jade.domain.FIPAAgentManagement.ServiceDescription;

import asma_proj1.agents.protocols.TradeOffer;
import asma_proj1.agents.protocols.TradeOfferData;
import asma_proj1.agents.protocols.TradeOfferResponder;
import asma_proj1.card.Card;
import asma_proj1.card.CardSet;
import asma_proj1.utils.RandomUtils;
import asma_proj1.utils.StringUtils;

public abstract class CardOwner extends BaseAgent {
    public static final String DF_HAVE_TYPE = "have",
        DF_WANT_TYPE = "want";

    protected final Map<Card, Integer> collection = new HashMap<>();
    protected final DFAgentDescription dfd = new DFAgentDescription();
    public final Lock collectionLock = new ReentrantLock();

    @Override
    protected void setup() {
        super.setup();

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
    }

    public Map<Card, Integer> getCollection() {
        return Collections.unmodifiableMap(collection);
    }

    public boolean cardsInCollection(Collection<Card> cards) {
        Map<Card, Integer> amountMap = new HashMap<>();
        for (Card card : cards) {
            amountMap.compute(card, (k, v) -> v == null ? 1 : v + 1);
        }

        for (Map.Entry<Card, Integer> entry : amountMap.entrySet()) {
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
        Set<Card> unique = new HashSet<>(cards);

        for (Card card : cards) {
            collection.compute(card, (k, v) -> v == null || v == 1 ? null : v - 1);
        }

        for (Card card : unique) {
            ServiceDescription sd = new ServiceDescription();
            sd.setType(DF_HAVE_TYPE);
            sd.setName(String.valueOf(card.getId()));
            dfd.removeServices(sd);
            
            if (collection.containsKey(card)) {
                sd.addProperties(new Property("count", collection.get(card)));
                dfd.addServices(sd);
            }
        }

        updateDfd();
    }

    protected void updateDfd() {
        try {
            DFService.modify(this, dfd);
        }
        catch (FIPAException e) {
            e.printStackTrace();
        }
    }

    protected void listCards(List<Card> cards, String type) {
        for (Card card : cards) {
            ServiceDescription sd = new ServiceDescription();
            sd.setType(type);
            sd.setName(String.valueOf(card.getId()));

            if (collection.containsKey(card)) {
                sd.addProperties(new Property("count", collection.get(card)));
                dfd.addServices(sd);
            }
        }

        updateDfd();
    }

    protected void unlistCards(List<Card> cards, String type) {
        for (Card card : cards) {
            ServiceDescription sd = new ServiceDescription();
            sd.setType(type);
            sd.setName(String.valueOf(card.getId()));
            dfd.removeServices(sd);
        }

        updateDfd();
    }

    protected abstract void handleNewCards(List<Card> cards);
    public abstract List<Card> selectCardsForTrade(List<Card> offered);
    public abstract TradeOffer generateTradeOffer(TradeOfferData data);
    public abstract double evaluateTradeOffer(TradeOffer offer);

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
