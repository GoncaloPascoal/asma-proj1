package asma_proj1.agents.protocols;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.stream.Collectors;

import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.proto.ContractNetInitiator;

import asma_proj1.agents.CardOwner;
import asma_proj1.card.CardInstance;
import asma_proj1.utils.StringUtils;

public class TradeOfferInitiator extends ContractNetInitiator {
    private final CardOwner cardOwner;
    private final TradeOfferData data;
    private final Collection<AID> agents;
    private TradeOffer bestOffer = null;

    public TradeOfferInitiator(CardOwner cardOwner, TradeOfferData data, Collection<AID> agents) {
        super(cardOwner, null);
        this.cardOwner = cardOwner;
        this.data = data;
        this.agents = agents;
    }

    @Override
    protected Vector<ACLMessage> prepareCfps(ACLMessage cfp) {
        Vector<ACLMessage> msgs = new Vector<>();

        for (AID agent : agents) {
            try {
                cfp = new ACLMessage(ACLMessage.CFP);
                cfp.setContentObject(data);
                cfp.addReceiver(agent);
                msgs.add(cfp);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }

        return msgs;
    }

    @Override
    protected void handleAllResponses(Vector responses, Vector acceptances) {
        ACLMessage bestMessage = null;
        int numOffers = 0;

        for (Object obj : responses) {
            ACLMessage msg = (ACLMessage) obj;

            if (msg.getPerformative() == ACLMessage.PROPOSE) {
                ++numOffers;

                ACLMessage reply = msg.createReply();
                reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
                acceptances.add(reply);

                try {
                    TradeOffer offer = (TradeOffer) msg.getContentObject();
                    if (bestOffer == null || cardOwner.evaluateTradeOffer(offer) > cardOwner.evaluateTradeOffer(bestOffer)) {
                        bestMessage = reply;
                        bestOffer = offer;
                    }
                }
                catch (UnreadableException e) {
                    e.printStackTrace();
                }
            }
        }

        if (numOffers > 0) {
            StringUtils.logAgentMessage(myAgent, "🔄 Received " + numOffers + " trade offers.");

            Map<CardInstance, Integer> toRemove = new HashMap<>();
            for (CardInstance inst : bestOffer.receive) {
                toRemove.compute(inst, (k, v) -> v == null ? 1 : v + 1);
            }

            cardOwner.collectionLock.lock();

            // Check if the cards in the offer still exist in our collection
            Map<CardInstance, Integer> collection = cardOwner.getCollection();
            for (CardInstance inst : toRemove.keySet()) {
                if (collection.getOrDefault(inst, 0) < toRemove.get(inst)) {
                    cardOwner.collectionLock.unlock();
                    return;
                }
            }

            cardOwner.removeCardsFromCollection(bestOffer.receive);
            bestMessage.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
            cardOwner.collectionLock.unlock();

            StringUtils.logAgentMessage(myAgent, "✅ Accepting best trade offer:\n" + bestOffer);
        }
    }

    @Override
    protected void handleInform(ACLMessage inform) {
        cardOwner.collectionLock.lock();
        cardOwner.addCardsToCollection(bestOffer.give);
        cardOwner.collectionLock.unlock();
    }
}
