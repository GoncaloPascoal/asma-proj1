package asma_proj1.agents;

import java.util.ArrayList;
import java.util.List;

import jade.core.AID;
import jade.core.Agent;
import jade.core.ServiceException;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.messaging.TopicManagementHelper;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

import asma_proj1.card.CardSet;
import asma_proj1.utils.StringUtils;

public abstract class BaseAgent extends Agent {
    private int capital = 0;
    private AID topic;
    protected final List<CardSet> cardSets = new ArrayList<>();

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

    protected static String changeCapitalMessage(int delta) {
        String color = StringUtils.GREEN;
        if (delta < 0) color = StringUtils.RED;

        return StringUtils.colorize(String.valueOf(delta), color) + " 💵";
    }

    protected void handleNewCardSet(CardSet set) {}

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
                    cardSets.add(set);
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
