package asma_proj1.agents.protocols;

import java.util.HashMap;

import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.proto.SimpleAchieveREInitiator;

import asma_proj1.agents.CardOwner;
import asma_proj1.agents.Marketplace;
import asma_proj1.card.Card;

public class SnapshotInitiator extends SimpleAchieveREInitiator {
    private final CardOwner cardOwner;
    private final AID marketplace;

    public SnapshotInitiator(CardOwner cardOwner, AID marketplace) {
        super(cardOwner, new ACLMessage(ACLMessage.REQUEST));
        this.cardOwner = cardOwner;
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
        try {
            HashMap<Card, Snapshot> snapshot = (HashMap<Card, Snapshot>) msg.getContentObject();
            cardOwner.setLatestSnapshot(snapshot);
        }
        catch (UnreadableException | ClassCastException e) {
            e.printStackTrace();
        }
    }
}
