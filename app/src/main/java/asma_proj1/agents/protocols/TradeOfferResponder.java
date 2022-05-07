package asma_proj1.agents.protocols;

import asma_proj1.agents.CardOwner;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.introspection.ACLMessage;
import jade.proto.ContractNetResponder;
import jade.proto.SSContractNetResponder;

public class TradeOfferResponder extends SSContractNetResponder {
    private final TradeOfferData data;

    public TradeOfferResponder(CardOwner target, TradeOfferData data) {
        super(target, null);
        this.data = data;
    }

    @Override
    protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) throws FailureException {
        ACLMessage msg;

        return msg;
    }
    
}