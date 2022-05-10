package asma_proj1.agents.protocols;

import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.SubscriptionResponder;

import asma_proj1.agents.Marketplace;

public class MarketplaceSubscriptionResponder extends SubscriptionResponder {
    private final Marketplace marketplace;

    public MarketplaceSubscriptionResponder(Marketplace marketplace) {
        super(marketplace, MessageTemplate.MatchPerformative(ACLMessage.SUBSCRIBE));
        this.marketplace = marketplace;
    }

    @Override
    protected ACLMessage handleSubscription(ACLMessage subMsg) throws NotUnderstoodException, RefuseException {
        ACLMessage agree = subMsg.createReply();
        agree.setPerformative(ACLMessage.AGREE);

        Subscription subscription = createSubscription(subMsg);
        marketplace.addSubscription(subMsg.getSender(), subscription);

        return agree;
    }
}
