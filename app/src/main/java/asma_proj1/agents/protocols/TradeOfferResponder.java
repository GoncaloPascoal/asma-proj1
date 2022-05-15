package asma_proj1.agents.protocols;

import java.io.IOException;

import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.proto.ContractNetResponder;

import asma_proj1.agents.CardOwner;
import asma_proj1.agents.protocols.data.TradeOffer;
import asma_proj1.agents.protocols.data.TradeOfferData;
import asma_proj1.card.CardSource;
import asma_proj1.utils.RandomUtils;

public class TradeOfferResponder extends ContractNetResponder {
    private CardOwner cardOwner;
    private TradeOfferData data;

    public TradeOfferResponder(CardOwner cardOwner) {
        super(cardOwner, MessageTemplate.and(
            MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET),
            MessageTemplate.MatchPerformative(ACLMessage.CFP)
        ));
        this.cardOwner = cardOwner;
    }

    @Override
    protected ACLMessage handleCfp(ACLMessage cfp) throws RefuseException,
            FailureException, NotUnderstoodException {
        if (!RandomUtils.randomOutcome(cardOwner.parameters.probTrade)) {
            throw new RefuseException("Agent does not wish to trade.");
        }

        try {
            data = (TradeOfferData) cfp.getContentObject();
        }
        catch (UnreadableException | ClassCastException e) {
            e.printStackTrace();
            throw new FailureException("Invalid content object.");
        }

        // Filter only wanted cards
        data.offered = cardOwner.selectCardsForTrade(data.offered);

        TradeOffer tradeOffer = cardOwner.generateTradeOffer(data);
        if (tradeOffer == null) {
            throw new RefuseException("No viable trade offer.");
        }

        try {
            ACLMessage propose = cfp.createReply();
            propose.setPerformative(ACLMessage.PROPOSE);
            propose.setContentObject(tradeOffer);
            return propose;
        }
        catch (IOException e) {
            e.printStackTrace();
            throw new FailureException("Error when sending proposal.");
        }
    }

    @Override
    protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) throws FailureException {
        try {
            cardOwner.collectionLock.lock();
            TradeOffer offer = (TradeOffer) propose.getContentObject();
            if (!cardOwner.cardsInCollection(offer.give)) {
                throw new FailureException("Trade is no longer possible.");
            }
            cardOwner.removeCardsFromCollection(offer.give);
            cardOwner.addCardsToCollection(offer.receive, CardSource.TRADING);
        }
        catch (UnreadableException e) {
            e.printStackTrace();
            throw new FailureException("Error when performing trade.");
        }
        finally {
            cardOwner.collectionLock.unlock();
        }

        ACLMessage msg = accept.createReply();
        msg.setPerformative(ACLMessage.INFORM);
        return msg;
    }
}