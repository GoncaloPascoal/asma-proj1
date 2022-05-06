package asma_proj1.agents.protocols;

import java.io.IOException;
import java.util.List;
import java.util.Vector;

import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.proto.ContractNetInitiator;

import asma_proj1.agents.CardOwner;

public class TradeOfferInitiator extends ContractNetInitiator {
    private final TradeOfferData data;
    private final List<AID> agents;

    public TradeOfferInitiator(CardOwner owner, TradeOfferData data, List<AID> agents) {
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
}
