package asma_proj1.agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.ServiceException;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.messaging.TopicManagementHelper;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

import asma_proj1.card.CardSet;

public abstract class BaseAgent extends Agent {
    private int capital = 0;
    private AID topic;

    protected void setup() {
        try {
            TopicManagementHelper helper = (TopicManagementHelper) getHelper(TopicManagementHelper.SERVICE_NAME);
            topic = helper.createTopic(CardDatabase.TOPIC_NAME);
            helper.register(topic);
        }
        catch (ServiceException e) {
            e.printStackTrace();
        }

        addBehaviour(new HandleDatabaseMessages(this));
    }

    public int getCapital() {
        return capital;
    }

    public AID getTopic() {
        return topic;
    }

    public boolean changeCapital(int delta) {
        int newCapital = capital + delta;
        
        if (newCapital >= 0) {
            capital = newCapital;
            return true;
        }

        return false;
    }

    protected void handleNewCardSet(CardSet set) {
        // TODO: make abstract?
    }

    private class HandleDatabaseMessages extends CyclicBehaviour {
        private final BaseAgent agent;

        public HandleDatabaseMessages(BaseAgent agent) {
            this.agent = agent;
        }

        @Override
        public void action() {
            ACLMessage message = agent.receive(MessageTemplate.MatchTopic(agent.getTopic()));

            if (message != null) {
                try {
                    CardSet set = (CardSet) message.getContentObject();
                    agent.handleNewCardSet(set);
                }
                catch (UnreadableException e) {
                    e.printStackTrace();
                }
            }
            else {
                block();
            }
        }
    }
}
