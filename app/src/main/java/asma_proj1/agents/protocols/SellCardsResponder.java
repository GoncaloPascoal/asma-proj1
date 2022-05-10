package asma_proj1.agents.protocols;

import jade.core.AID;
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
        AID seller = request.getSender();
        ACLMessage response = request.createReply();
        response.setPerformative(ACLMessage.AGREE);

        try {
            Transaction transaction = (Transaction) request.getContentObject();

            for (Card card : transaction.cards) {
                marketplace.addListing(card,
                    new Listing(seller, transaction.priceMap.get(card)));
            }

            int totalFee = Marketplace.calculateSellerFee(transaction);
            marketplace.changeCapital(totalFee);
        }
        catch (UnreadableException | ClassCastException e) {
            throw new NotUnderstoodException("Invalid content object.");
        }

        return response;
    }
}
