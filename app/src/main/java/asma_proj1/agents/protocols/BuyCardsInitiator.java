package asma_proj1.agents.protocols;

import java.io.IOException;

import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.proto.SimpleAchieveREInitiator;
import asma_proj1.agents.BaseAgent;
import asma_proj1.agents.CardOwner;
import asma_proj1.agents.Marketplace;
import asma_proj1.agents.protocols.data.Transaction;
import asma_proj1.utils.StringUtils;

public class BuyCardsInitiator extends SimpleAchieveREInitiator {
    private final CardOwner cardOwner;
    private final AID marketplace;
    private final Transaction transaction;

    public BuyCardsInitiator(CardOwner cardOwner, AID marketplace, Transaction transaction) {
        super(cardOwner, new ACLMessage(ACLMessage.REQUEST));
        this.cardOwner = cardOwner;
        this.marketplace = marketplace;
        this.transaction = transaction;
    }

    @Override
    protected ACLMessage prepareRequest(ACLMessage msg) {
        msg.setProtocol(Marketplace.BUY_CARDS_PROTOCOL);
        msg.addReceiver(marketplace);
        try {
            msg.setContentObject(transaction);
        }
        catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        if (!cardOwner.changeCapital(-Marketplace.calculateBuyerPrice(transaction))) {
            StringUtils.logAgentError(cardOwner,
                "Couldn't pay specified maximum price for marketplace cards.");
            return null;
        }

        return msg;
    }

    @Override
    protected void handleInform(ACLMessage msg) {
        try {
            Transaction realTransaction = (Transaction) msg.getContentObject();
            int paidPrice = Marketplace.calculateBuyerPrice(transaction),
                realPrice = Marketplace.calculateBuyerPrice(realTransaction);
            cardOwner.changeCapital(realPrice - paidPrice);

            if (!realTransaction.cards.isEmpty()) {
                cardOwner.addCardsToCollection(realTransaction.cards);

                StringUtils.logAgentMessage(cardOwner, "📉 Bought " +
                    realTransaction.cards.size() + " cards from marketplace: " +
                    BaseAgent.changeCapitalMessage(-realPrice));
            }
        }
        catch (UnreadableException | ClassCastException e) {
            e.printStackTrace();
        }
    }
}
