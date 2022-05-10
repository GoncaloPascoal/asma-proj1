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
import asma_proj1.agents.protocols.data.Transaction;
import asma_proj1.card.Card;

public class Marketplace extends BaseAgent {
    public static final String SERVICE_TYPE = "marketplace",
        SNAPSHOT_PROTOCOL = "snapshot",
        SELL_CARDS_PROTOCOL = "sell-cards";

    public static final int MIN_SELLER_FEE = 3;
    public static final double SELLER_FEE = 0.05, BUYER_FEE = 0.05;

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

    public static int calculateSellerFee(int price) {
        return Math.max(MIN_SELLER_FEE, (int) (SELLER_FEE * price));
    }

    public static int calculateSellerFee(Transaction transaction) {
        int totalFee = 0;
        for (Card card : transaction.cards) {
            totalFee += transaction.priceMap.get(card);
        }
        return totalFee;
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
}
