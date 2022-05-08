package asma_proj1.agents.protocols;

import java.io.IOException;
import java.util.Vector;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.proto.ContractNetResponder;

import asma_proj1.agents.CardOwner;


public class TradeOfferResponder extends ContractNetResponder {
    private final CardOwner target;
    private final TradeOfferData data;

    public TradeOfferResponder(CardOwner target, TradeOfferData data) {
        super(target, null);
        this.target = target;
        this.data = data;
    }


    // handle cfp (ontology interaction protocol)
    // &
    // send 'refuse' or 'propose'
    @Override
    protected ACLMessage handleCfp(ACLMessage cfp) {
        System.out.println("Agent " + target.getLocalName() + ": received from " + cfp.getSender().getLocalName());

        ACLMessage propose = cfp.createReply();
        propose.setPerformative(ACLMessage.PROPOSE);

        try {
            propose.setContentObject(this.data); // prob not this?
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return propose;
    }

    // detect if it received 'reject-proposal' or 'accept-proposal'

    // handle 'reject-proposal'
    @Override
    protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
        System.out.println("Agent '" + reject.getSender().getLocalName() + "' Proposal : Rejected");
    }

    // handle 'accept-proposal'
    // &
    // send 'failure' or 'inform-done:inform' or 'inform-result:inform'
    @Override
    protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) {
        ACLMessage inform = accept.createReply();

        inform.setPerformative(ACLMessage.INFORM);

        ACLMessage msg = null; // not sure if this data type
        try {
            msg = (ACLMessage)accept.getContentObject();
        }
        catch (UnreadableException e) {
            e.printStackTrace();
        }
        if (msg != null) {
            // prob something not right here
            inform.setContent("Proposal from " + accept.getSender().getLocalName() + " has been Accepted!");
        }

        return inform;
    }




    // what does registerHandleCfp actually do?
    
}