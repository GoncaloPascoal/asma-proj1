package asma_proj1.agents.protocols;


import java.util.Vector;

import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.proto.SubscriptionInitiator;

import asma_proj1.agents.CardOwner;
import asma_proj1.utils.StringUtils;

public class MarketplaceSubscriptionInitiator extends SubscriptionInitiator {
    private final AID marketplace;

    public MarketplaceSubscriptionInitiator(CardOwner cardOwner, AID marketplace) {
        super(cardOwner, new ACLMessage(ACLMessage.SUBSCRIBE));
        this.marketplace = marketplace;
    }

    @Override
    protected Vector<?> prepareSubscriptions(ACLMessage subscription) {
        Vector<ACLMessage> vec = new Vector<>();
        subscription.addReceiver(marketplace);
        vec.add(subscription);
        return vec;
    }

    @Override
    protected void handleAgree(ACLMessage agree) {
        StringUtils.logAgentMessage(myAgent, "🏦 Subscribed to updates from marketplace.");
    }
}
