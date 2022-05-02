package asma_proj1.agents;

import java.util.HashMap;
import java.util.Map;

import jade.core.AID;
import jade.core.Agent;
import jade.core.ServiceException;
import jade.core.behaviours.TickerBehaviour;
import jade.core.messaging.TopicManagementHelper;
import jade.lang.acl.ACLMessage;

import asma_proj1.agents.protocol.CardInfoRespond;
import asma_proj1.card.Card;

public class CardDatabase extends Agent {
    public static final String TOPIC_NAME = "database";

    private AID topic;
    private final Map<Integer, Card> cards = new HashMap<>();

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
        addBehaviour(new CardInfoRespond(this));
    }

    public Card getCardById(int id) {
        return cards.get(id);
    }

    public void addCard(Card card) {
        cards.put(card.getId(), card);
    }

    private class GenerateCardSet extends TickerBehaviour {
        private static final int intervalSeconds = 60;

        public GenerateCardSet(CardDatabase database) {
            super(database, intervalSeconds * 1000);
        }

        @Override
        protected void onTick() {
            ACLMessage message = new ACLMessage(ACLMessage.INFORM);
            message.addReceiver(topic);
            message.setContent("New card set");
            send(message);
        }
    }
}
