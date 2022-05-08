package asma_proj1.agents;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

import asma_proj1.agents.protocols.TradeOffer;
import asma_proj1.agents.protocols.TradeOfferData;
import asma_proj1.agents.protocols.TradeOfferResponder;
import asma_proj1.card.CardInstance;
import asma_proj1.card.CardSet;
import asma_proj1.utils.RandomUtils;
import asma_proj1.utils.StringUtils;

public abstract class CardOwner extends BaseAgent {
    public static final String DF_HAVE_TYPE = "have",
        DF_WANT_TYPE = "want";

    protected final Map<CardInstance, Integer> collection = new HashMap<>();
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

    protected void receiveCapital() {
        int capital = RandomUtils.intRangeInclusive(50, 100);
        changeCapital(capital);
        StringUtils.logAgentMessage(this, "Received periodic capital: " +
            changeCapitalMessage(capital));
    }

    protected void purchasePack(CardSet set) {
        if (changeCapital(-CardSet.PACK_PRICE)) {
            StringUtils.logAgentMessage(this, "Purchased a card pack: " + changeCapitalMessage(-CardSet.PACK_PRICE));

            List<CardInstance> pack = set.openPack();
            addCardsToCollection(pack);
        }
    }

    public void addCardsToCollection(List<CardInstance> cards) {
        for (CardInstance inst : cards) {
            collection.compute(inst, (k, v) -> v == null ? 1 : v + 1);
        }
        handleNewCards(cards);
    }

    public void removeCardsFromCollection(List<CardInstance> cards) {
        // TODO: implement
    }

    protected void updateDfd() {
        try {
            DFService.modify(this, dfd);
        }
        catch (FIPAException e) {
            e.printStackTrace();
        }
    }

    protected void listCards(List<CardInstance> cards, String type) {
        for (CardInstance inst : cards) {
            ServiceDescription sd = new ServiceDescription();
            sd.setType(type);
            sd.setName(String.valueOf(inst.getCard().getId()));
            dfd.addServices(sd);
        }

        updateDfd();
    }

    protected void unlistCards(List<CardInstance> cards, String type) {
        for (CardInstance inst : cards) {
            ServiceDescription sd = new ServiceDescription();
            sd.setType(type);
            sd.setName(String.valueOf(inst.getCard().getId()));
            dfd.removeServices(sd);
        }

        updateDfd();
    }

    protected abstract void handleNewCards(List<CardInstance> cards);
    public abstract List<CardInstance> selectCardsForTrade(List<CardInstance> offered);
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
