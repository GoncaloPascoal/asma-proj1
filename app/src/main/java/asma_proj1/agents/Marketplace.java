package asma_proj1.agents;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import jade.core.AID;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import asma_proj1.agents.protocols.SnapshotResponder;
import asma_proj1.card.Card;

public class Marketplace extends BaseAgent {
    public static final String SERVICE_TYPE = "marketplace",
        SNAPSHOT_PROTOCOL = "snapshot";
    private final Map<Card, TreeSet<Listing>> listings = new HashMap<>();

    @Override
    protected void setup() {
        super.setup();

        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());

        ServiceDescription sd = new ServiceDescription();
        sd.setType(SERVICE_TYPE);
        sd.setName(getName());
        dfd.addServices(sd);

        try {
            DFService.register(this, dfd);
        }
        catch (FIPAException e) {
            e.printStackTrace();
        }

        addBehaviour(new SnapshotResponder(this));
    }

    public void addListing(Card card, Listing listing) {
        listings.putIfAbsent(card, new TreeSet<>());
        listings.get(card).add(listing);
    }

    public class Listing implements Comparable<Listing> {
        public final AID aid;
        public final int price;

        public Listing(AID aid, int price) {
            this.aid = aid;
            this.price = price;
        }

        @Override
        public int compareTo(Listing other) {
            return Integer.compare(price, other.price);
        }
    }
}
