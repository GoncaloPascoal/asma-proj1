package asma_proj1.agents.protocol;

import java.util.Vector;

import asma_proj1.agents.BaseAgent;
import asma_proj1.card.Card;
import asma_proj1.utils.StringUtils;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.proto.AchieveREInitiator;

public class CardInfoRequest extends AchieveREInitiator {
    private int cardId;

    public CardInfoRequest(BaseAgent agent, int cardId) {
        super(agent, null);
    }

    @Override
    protected Vector<ACLMessage> prepareRequests(ACLMessage request) {
        Vector<ACLMessage> v = new Vector<>();
        
        request = new ACLMessage(ACLMessage.REQUEST);
        request.setContent(Integer.toString(cardId));

        v.add(request);
        return v;
    }

    @Override
    protected void handleFailure(ACLMessage failure) {
        StringUtils.logError("Card info request failed:" + failure.getContent());
    }

    @Override
    protected void handleInform(ACLMessage inform) {
        try {
            Card card = (Card) inform.getContentObject();
            System.out.println("Got card info: " + card);
        }
        catch (UnreadableException | ClassCastException e) {
            e.printStackTrace();
        }
    }
}
