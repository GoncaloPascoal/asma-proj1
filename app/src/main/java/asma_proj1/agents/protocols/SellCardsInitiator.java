package asma_proj1.agents.protocols;

import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.proto.SimpleAchieveREInitiator;

import java.io.IOException;

import asma_proj1.agents.BaseAgent;
import asma_proj1.agents.CardOwner;
import asma_proj1.agents.Marketplace;
import asma_proj1.agents.protocols.data.Transaction;
import asma_proj1.utils.StringUtils;

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
        msg.addReceiver(marketplace);
        try {
            msg.setContentObject(transaction);

            StringUtils.logAgentMessage(cardOwner, "🧾 Listed " + transaction.cards.size() +
                " cards in marketplace: " + BaseAgent.changeCapitalMessage(-Marketplace.calculateSellerFee(transaction)));

            return msg;
        }
        catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
