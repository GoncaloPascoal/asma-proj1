package asma_proj1.agents.protocols;

import java.io.IOException;

import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.proto.SimpleAchieveREResponder;

import asma_proj1.agents.Marketplace;
import asma_proj1.agents.protocols.data.Transaction;

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
    protected ACLMessage prepareResponse(ACLMessage request) throws NotUnderstoodException, RefuseException {
        ACLMessage response = request.createReply();
        response.setPerformative(ACLMessage.AGREE);
        return response;
    }

    @Override
    protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) throws FailureException {
        ACLMessage inform = request.createReply();
        inform.setPerformative(ACLMessage.INFORM);

        try {
            Transaction transaction = (Transaction) request.getContentObject();
            Transaction actualTransaction = marketplace.attemptPurchase(transaction);
            inform.setContentObject(actualTransaction);
        }
        catch (UnreadableException | ClassCastException e) {
            e.printStackTrace();
            throw new FailureException("Invalid content object.");
        }
        catch (IOException e) {
            e.printStackTrace();
            throw new FailureException("Error when serializing actual transaction.");
        }

        return inform;
    }
}
