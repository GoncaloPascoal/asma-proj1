package asma_proj1.agents;

import java.util.ArrayList;
import java.util.List;

import jade.core.behaviours.TickerBehaviour;

import asma_proj1.card.CardInstance;
import asma_proj1.card.CardSet;
import asma_proj1.utils.RandomUtils;
import asma_proj1.utils.StringUtils;

public abstract class CardOwner extends BaseAgent {
    private List<CardInstance> collection = new ArrayList<>();

    @Override
    protected void setup() {
        super.setup();
        addBehaviour(new ReceiveCapital(this));
    }

    protected void purchasePack(CardSet set) {
        if (changeCapital(-CardSet.PACK_PRICE)) {
            List<CardInstance> pack = set.openPack();
            collection.addAll(pack);

            StringUtils.logAgentMessage(this, "Purchased a card pack (" + capitalChangeMessage(-CardSet.PACK_PRICE) + ")");

            handleNewCardPack(pack);
        }
    }

    protected void listCardForTrade(CardInstance instance) {
        // TODO: implement
    }

    protected abstract void handleNewCardPack(List<CardInstance> pack);

    private class ReceiveCapital extends TickerBehaviour {
        private static final int INTERVAL_SECONDS = 60;
        
        public ReceiveCapital(CardOwner cardOwner) {
            super(cardOwner, INTERVAL_SECONDS * 1000);
        }

        @Override
        protected void onTick() {
            int capital = RandomUtils.intRangeInclusive(50, 100);
            changeCapital(capital);
            StringUtils.logAgentMessage(myAgent, "Received periodic capital: " +
                capitalChangeMessage(capital));
        }
    }
}
