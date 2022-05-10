package asma_proj1.agents.protocols;

import java.io.IOException;
import java.util.HashMap;

import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.SimpleAchieveREResponder;

import asma_proj1.agents.Marketplace;
import asma_proj1.agents.protocols.data.Snapshot;
import asma_proj1.card.Card;

public class SnapshotResponder extends SimpleAchieveREResponder {
    private final Marketplace marketplace;

    public SnapshotResponder(Marketplace marketplace) {
        super(marketplace, MessageTemplate.and(
            MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
            MessageTemplate.MatchProtocol(Marketplace.SNAPSHOT_PROTOCOL)
        ));
        this.marketplace = marketplace;
    }

    @Override
    protected ACLMessage prepareResponse(ACLMessage request) throws NotUnderstoodException, RefuseException {
        ACLMessage response = request.createReply();
        response.setPerformative(ACLMessage.AGREE);
        return response;
    }

    @Override
    protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) throws FailureException {
        HashMap<Card, Snapshot> snapshots = marketplace.generateSnapshot();

        ACLMessage inform = request.createReply();
        inform.setPerformative(ACLMessage.INFORM);

        try {
            inform.setContentObject(snapshots);
        }
        catch (IOException e) {
            e.printStackTrace();
            throw new FailureException("Error when sending snapshot inform.");
        }

        return inform;
    }
}
