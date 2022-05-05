package asma_proj1.agents;

import java.util.ArrayList;
import java.util.List;

import asma_proj1.card.CardInstance;
import asma_proj1.utils.RandomUtils;
import jade.core.behaviours.TickerBehaviour;

public class CardOwner extends BaseAgent {
    private List<CardInstance> collection = new ArrayList<>();

    @Override
    protected void setup() {
        super.setup();
        addBehaviour(new ReceiveCapital(this));
    }

    private class ReceiveCapital extends TickerBehaviour {
        private static final int INTERVAL_SECONDS = 60;
        
        public ReceiveCapital(CardOwner cardOwner) {
            super(cardOwner, INTERVAL_SECONDS * 1000);
        }

        @Override
        protected void onTick() {
            int capital = RandomUtils.intRangeInclusive(50, 100);
            changeCapital(capital);
        }
    }
}
