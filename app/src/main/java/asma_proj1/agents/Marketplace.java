package asma_proj1.agents;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import jade.core.AID;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.proto.SubscriptionResponder.Subscription;

import asma_proj1.agents.protocols.MarketplaceSubscriptionResponder;
import asma_proj1.agents.protocols.SnapshotResponder;
import asma_proj1.agents.protocols.data.Snapshot;
import asma_proj1.card.Card;

public class Marketplace extends BaseAgent {
    public static final String SERVICE_TYPE = "marketplace",
        SNAPSHOT_PROTOCOL = "snapshot",
        SELL_CARDS_PROTOCOL = "sell-cards";
    private final Map<Card, TreeSet<Listing>> listings = new HashMap<>();
    private final Map<AID, Subscription> subscriptions = new HashMap<>();

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

        addBehaviour(new MarketplaceSubscriptionResponder(this));
        addBehaviour(new SnapshotResponder(this));
    }

    public void addListing(Card card, Listing listing) {
        listings.putIfAbsent(card, new TreeSet<>());
        listings.get(card).add(listing);
    }

    public HashMap<Card, Snapshot> generateSnapshot() {
        HashMap<Card, Snapshot> snapshots = new HashMap<>();

        for (Card card : listings.keySet()) {
            TreeSet<Listing> cardListings = listings.get(card);
            
            int count = cardListings.size();
            int minPrice = cardListings.first().price;
            int averagePrice = cardListings.stream().mapToInt(l -> l.price).sum();

            snapshots.put(card, new Snapshot(count, minPrice, averagePrice));
        }

        return snapshots;
    }

    public void addSubscription(AID aid, Subscription subscription) {
        subscriptions.put(aid, subscription);
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
