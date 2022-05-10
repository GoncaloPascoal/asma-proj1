package asma_proj1.agents.protocols;

import asma_proj1.agents.CardOwner;
import asma_proj1.agents.Marketplace;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.proto.SimpleAchieveREInitiator;

public class SnapshotInitiator extends SimpleAchieveREInitiator {
    private final AID marketplace;

    public SnapshotInitiator(CardOwner cardOwner, AID marketplace) {
        super(cardOwner, new ACLMessage(ACLMessage.REQUEST));
        this.marketplace = marketplace;
    }

    @Override
    protected ACLMessage prepareRequest(ACLMessage msg) {
        msg.addReceiver(marketplace);
        msg.setProtocol(Marketplace.SNAPSHOT_PROTOCOL);
        return msg;
    }

    @Override
    protected void handleInform(ACLMessage msg) {
        
    }
}
