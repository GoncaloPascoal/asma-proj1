package asma_proj1.agents.protocols;

import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.proto.SimpleAchieveREInitiator;

import java.io.IOException;

import asma_proj1.agents.CardOwner;
import asma_proj1.agents.Marketplace;
import asma_proj1.agents.protocols.data.Transaction;

public class SellCardsInitiator extends SimpleAchieveREInitiator {
    private final CardOwner cardOwner;
    private final AID marketplace;
    private final Transaction transaction;

    public SellCardsInitiator(CardOwner cardOwner, AID marketplace, Transaction transaction) {
        super(cardOwner, new ACLMessage(ACLMessage.REQUEST));
        this.cardOwner = cardOwner;
        this.marketplace = marketplace;
        this.transaction = transaction;
    }

    @Override
    protected ACLMessage prepareRequest(ACLMessage msg) {
        msg.setProtocol(Marketplace.SELL_CARDS_PROTOCOL);

        cardOwner.collectionLock.lock();
        try {
            msg.addReceiver(marketplace);
            msg.setContentObject(transaction);
            cardOwner.removeCardsFromCollection(transaction.cards);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            cardOwner.collectionLock.unlock();
        }

        return msg;
    }
}
