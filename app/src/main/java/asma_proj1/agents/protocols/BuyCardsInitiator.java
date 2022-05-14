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
import asma_proj1.card.CardSource;
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
        return msg;
    }

    @Override
    protected void handleInform(ACLMessage msg) {
        try {
            Transaction realTransaction = (Transaction) msg.getContentObject();
            int paidPrice = transaction.totalPrice(), realPrice = realTransaction.totalPrice();
            cardOwner.changeCapital(paidPrice - realPrice);

            if (!realTransaction.cards.isEmpty()) {
                cardOwner.addCardsToCollection(realTransaction.cards, CardSource.MARKETPLACE);

                StringUtils.logAgentMessage(cardOwner, "🛒 Bought " +
                    realTransaction.cards.size() + " cards from marketplace: " +
                    BaseAgent.changeCapitalMessage(-realPrice));
            }
        }
        catch (UnreadableException | ClassCastException e) {
            e.printStackTrace();
        }
    }
}
