package asma_proj1.agents.protocols;

import java.util.HashMap;

import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.proto.SimpleAchieveREInitiator;

import asma_proj1.agents.CardOwner;
import asma_proj1.agents.Marketplace;
import asma_proj1.agents.protocols.data.Snapshot;
import asma_proj1.card.Card;
import asma_proj1.utils.LogPriority;
import asma_proj1.utils.StringUtils;

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
    @SuppressWarnings("unchecked")
    protected void handleInform(ACLMessage msg) {
        try {
            HashMap<Card, Snapshot> snapshot = (HashMap<Card, Snapshot>) msg.getContentObject();
            cardOwner.setLatestSnapshot(snapshot);
            StringUtils.logAgentMessage(cardOwner, "📸 Obtained latest market snapshot.", LogPriority.LOW);
        }
        catch (UnreadableException | ClassCastException e) {
            e.printStackTrace();
        }
    }
}
