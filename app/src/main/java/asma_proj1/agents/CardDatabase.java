package asma_proj1.agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.ServiceException;
import jade.core.behaviours.TickerBehaviour;
import jade.core.messaging.TopicManagementHelper;
import jade.lang.acl.ACLMessage;

import asma_proj1.card.CardGenerator;
import asma_proj1.card.CardSet;

public class CardDatabase extends Agent {
    public static final String TOPIC_NAME = "database";

    private AID topic;
    private final CardGenerator generator = new CardGenerator();

    @Override
    protected void setup() {
        try {
            TopicManagementHelper helper = (TopicManagementHelper) getHelper(TopicManagementHelper.SERVICE_NAME);
            topic = helper.createTopic(TOPIC_NAME);
        }
        catch (ServiceException e) {
            e.printStackTrace();
        }

        addBehaviour(new GenerateCardSet(this));
    }

    public void generateCardSet() {
        CardSet set = new CardSet(generator);
        // ACLMessage message = new ACLMessage(ACLMessage.INFORM);
        // message.addReceiver(topic);
        // message.setContentObject(set);
        // send(message);
    }

    private class GenerateCardSet extends TickerBehaviour {
        private static final int INTERVAL_SECONDS = 120;

        public GenerateCardSet(CardDatabase database) {
            super(database, INTERVAL_SECONDS * 1000);
        }

        @Override
        protected void onTick() {
            generateCardSet();
        }
    }
}
