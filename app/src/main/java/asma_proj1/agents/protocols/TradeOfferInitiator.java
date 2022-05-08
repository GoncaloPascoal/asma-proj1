package asma_proj1.agents.protocols;

import java.io.IOException;
import java.util.Collection;
import java.util.Vector;

import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.proto.ContractNetInitiator;

import asma_proj1.agents.CardOwner;
import asma_proj1.utils.StringUtils;

public class TradeOfferInitiator extends ContractNetInitiator {
    private final TradeOfferData data;
    private final Collection<AID> agents;

    public TradeOfferInitiator(CardOwner owner, TradeOfferData data, Collection<AID> agents) {
        super(owner, null);
        this.data = data;
        this.agents = agents;
    }

    @Override
    protected Vector<ACLMessage> prepareCfps(ACLMessage cfp) {
        Vector<ACLMessage> msgs = new Vector<>();

        for (AID agent : agents) {
            try {
                cfp = new ACLMessage(ACLMessage.CFP);
                cfp.setContentObject(data);
                cfp.addReceiver(agent);
                msgs.add(cfp);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }

        return msgs;
    }

    @Override
    protected void handleAllResponses(Vector responses, Vector acceptances) {
        int offers = 0;

        for (Object e : responses) {
            ACLMessage msg = (ACLMessage) e;
            if (msg.getPerformative() == ACLMessage.PROPOSE) {
                offers += 1;
            }
        }

        StringUtils.logAgentMessage(myAgent, "Received " + offers + " trade offers.");
    }
}
