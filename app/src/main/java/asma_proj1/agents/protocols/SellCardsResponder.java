package asma_proj1.agents.protocols;

import jade.core.AID;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.proto.SimpleAchieveREResponder;

import asma_proj1.agents.Listing;
import asma_proj1.agents.Marketplace;
import asma_proj1.agents.protocols.data.Transaction;
import asma_proj1.card.Card;

public class SellCardsResponder extends SimpleAchieveREResponder {
    private final Marketplace marketplace;

    public SellCardsResponder(Marketplace marketplace) {
        super(marketplace, MessageTemplate.and(
            MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
            MessageTemplate.MatchProtocol(Marketplace.SELL_CARDS_PROTOCOL)
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
        AID seller = request.getSender();
        ACLMessage inform = request.createReply();
        inform.setPerformative(ACLMessage.INFORM);

        try {
            Transaction transaction = (Transaction) request.getContentObject();

            for (int i = 0; i < transaction.cards.size(); ++i) {
                Card card = transaction.cards.get(i);
                Integer price = transaction.prices.get(i);
                marketplace.addListing(card, new Listing(seller, price));
            }

            int totalFee = Marketplace.calculateSellerFee(transaction);
            marketplace.changeCapital(totalFee);
        }
        catch (UnreadableException | ClassCastException e) {
            e.printStackTrace();
            throw new FailureException("Invalid content object.");
        }

        return inform;
    }
}
