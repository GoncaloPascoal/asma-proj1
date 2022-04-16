package asma_proj1.agents.protocol;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREResponder;

import java.io.IOException;

import asma_proj1.agents.CardDatabase;
import asma_proj1.card.Card;

public class CardInfoRespond extends AchieveREResponder {
    private CardDatabase database;

    public CardInfoRespond(CardDatabase database) {
        super(database, MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
        this.database = database;
    }

    public ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) {
        ACLMessage reply = request.createReply();

        try {
            int id = Integer.parseInt(request.getContent());
            Card card = database.getCardById(id);

            if (card != null) {
                reply.setPerformative(ACLMessage.INFORM);
                try {
                    reply.setContentObject(card);
                }
                catch (IOException e) {
                    e.printStackTrace();
                    reply.setPerformative(ACLMessage.FAILURE);
                    reply.setContent("Error when serializing card information");
                }
            }
            else {
                reply.setPerformative(ACLMessage.FAILURE);
                reply.setContent("No card in the database with the specified ID");
            }
        }
        catch (NumberFormatException e) {
            reply.setPerformative(ACLMessage.FAILURE);
            reply.setContent("Invalid card ID");
        }

        return reply;
    }
}
