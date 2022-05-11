package asma_proj1.agents.protocols;

import jade.domain.FIPAAgentManagement.FailureException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.SimpleAchieveREResponder;

import asma_proj1.agents.Marketplace;

public class BuyCardsResponder extends SimpleAchieveREResponder {
    private final Marketplace marketplace;

    public BuyCardsResponder(Marketplace marketplace) {
        super(marketplace, MessageTemplate.and(
            MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
            MessageTemplate.MatchProtocol(Marketplace.BUY_CARDS_PROTOCOL)
        ));
        this.marketplace = marketplace;
    }

    @Override
    protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) throws FailureException {
        // TODO Auto-generated method stub
        return super.prepareResultNotification(request, response);
    }
}
