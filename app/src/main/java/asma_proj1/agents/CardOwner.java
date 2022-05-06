package asma_proj1.agents;

import java.util.ArrayList;
import java.util.List;

import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

import asma_proj1.card.CardInstance;
import asma_proj1.card.CardSet;
import asma_proj1.utils.RandomUtils;
import asma_proj1.utils.StringUtils;

public abstract class CardOwner extends BaseAgent {
    public static final String DF_HAVE_TYPE = "have",
        DF_WANT_TYPE = "want";

    private List<CardInstance> collection = new ArrayList<>();
    protected final DFAgentDescription dfd = new DFAgentDescription();

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
            List<CardInstance> pack = set.openPack();
            collection.addAll(pack);
            StringUtils.logAgentMessage(this, "Purchased a card pack: " + changeCapitalMessage(-CardSet.PACK_PRICE));

            handleNewCardPack(pack);
        }
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

    protected abstract void handleNewCardPack(List<CardInstance> pack);

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
