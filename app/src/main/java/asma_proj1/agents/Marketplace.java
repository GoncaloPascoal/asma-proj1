package asma_proj1.agents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeSet;
import java.util.stream.Stream;

import jade.core.AID;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

import jade.proto.SubscriptionResponder.Subscription;
import asma_proj1.agents.protocols.BuyCardsResponder;
import asma_proj1.agents.protocols.MarketplaceSubscriptionResponder;
import asma_proj1.agents.protocols.SellCardsResponder;
import asma_proj1.agents.protocols.SnapshotResponder;
import asma_proj1.agents.protocols.data.Snapshot;
import asma_proj1.agents.protocols.data.Transaction;
import asma_proj1.card.Card;

public class Marketplace extends BaseAgent {
    public static final String SERVICE_TYPE = "marketplace",
        SNAPSHOT_PROTOCOL = "snapshot",
        BUY_CARDS_PROTOCOL = "buy-cards",
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
        addBehaviour(new BuyCardsResponder(this));
        addBehaviour(new SellCardsResponder(this));
    }

    public static int calculateSellerFee(int price) {
        return Math.max(MIN_SELLER_FEE, (int) (SELLER_FEE * price));
    }

    public static int calculateSellerFee(Transaction transaction) {
        int totalFee = 0;
        for (Integer price : transaction.prices) {
            totalFee += calculateSellerFee(price);
        }
        return totalFee;
    }

    public static int calculateBuyerPrice(int price) {
        return (int) ((double) price * (1.0 + BUYER_FEE));
    }

    public static int calculateBuyerPrice(Transaction transaction) {
        int totalPrice = 0;
        for (Integer price : transaction.prices) {
            totalPrice += calculateBuyerPrice(price);
        }
        return totalPrice;
    }

    public void addListing(Card card, Listing listing) {
        synchronized (listings) {
            listings.putIfAbsent(card, new TreeSet<>());
            listings.get(card).add(listing);
        }
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

    public Transaction attemptPurchase(Transaction transaction) {
        List<Card> actualCards = new ArrayList<>();
        List<Integer> actualPrices = new ArrayList<>();
        Map<AID, Integer> sellerIncome = new HashMap<>();

        synchronized (listings) {
            for (int i = 0; i < transaction.cards.size(); ++i) {
                Card card = transaction.cards.get(i);
                Integer maxPrice = transaction.prices.get(i);
                Listing listing = attemptCardPurchase(card, maxPrice);

                if (listing != null) {
                    actualCards.add(card);
                    actualPrices.add(calculateBuyerPrice(listing.price));
                    sellerIncome.compute(listing.aid, (k, v) -> v == null ? listing.price : v + listing.price);
                }
            }
        }

        int marketplaceIncome = actualPrices.stream().mapToInt(v -> v).sum() -
            sellerIncome.values().stream().mapToInt(v -> v).sum();
        changeCapital(marketplaceIncome);

        for (Map.Entry<AID, Integer> entry : sellerIncome.entrySet()) {
            Subscription subscription = subscriptions.get(entry.getKey());

            if (subscription != null) {
                ACLMessage incomeMsg = new ACLMessage(ACLMessage.INFORM);
                incomeMsg.addReceiver(entry.getKey());
                incomeMsg.setContent(entry.getValue().toString());
                subscription.notify(incomeMsg);
            }
        }

        return new Transaction(actualCards, actualPrices);
    }

    private Listing attemptCardPurchase(Card card, int maxPrice) {
        if (!listings.containsKey(card)) return null;

        Stream<Listing> validListings = listings.get(card).stream().filter(l -> l.aid != null);
        try {
            Listing first = validListings.findFirst().get();

            if (calculateBuyerPrice(first.price) > maxPrice) {
                return null;
            }

            listings.get(card).remove(first);
            if (listings.get(card).isEmpty()) {
                listings.remove(card);
            }

            return first;
        }
        catch (NoSuchElementException e) {
            return null;
        }
    }
}
