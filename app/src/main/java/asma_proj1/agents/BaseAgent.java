package asma_proj1.agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.ServiceException;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.messaging.TopicManagementHelper;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class BaseAgent extends Agent {
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

        System.out.println("Setting up BaseAgent...");
        System.out.println("BaseAgent " + getAID().getName() + " is ready!");
    }

    // Put agent clean-up operations here
    protected void takeDown() {
        System.out.println("Base agent " + getAID().getName() + " terminating.");
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
        }

        return false;
    }

    private class HandleDatabaseMessages extends CyclicBehaviour {
        private BaseAgent agent;

        public HandleDatabaseMessages(BaseAgent agent) {
            this.agent = agent;
        }

        @Override
        public void action() {
            ACLMessage message = agent.receive(MessageTemplate.MatchTopic(agent.getTopic()));
            if (message != null) {
                System.out.println(
                    "Agent " + myAgent.getLocalName() + 
                    ": Message about topic " + topic.getLocalName() + 
                    " received. Content is " + message.getContent()
                );
            }
        }
    }
}
