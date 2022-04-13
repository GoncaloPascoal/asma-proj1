package asma_proj1.agents.protocol;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREResponder;

import asma_proj1.agents.CardDatabase;

public class CardInfoRespond extends AchieveREResponder {
    private CardDatabase database;

    public CardInfoRespond(CardDatabase database) {
        super(database, MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
        this.database = database;
    }

    public ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) {
        ACLMessage reply = request.createReply();
        // TODO
        return reply;
    }
}
