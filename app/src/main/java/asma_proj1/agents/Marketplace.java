package asma_proj1.agents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeSet;
import java.util.stream.Stream;

import jade.core.AID;
import jade.core.behaviours.TickerBehaviour;
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
import asma_proj1.card.Rarity;
import asma_proj1.utils.StringUtils;

public class Marketplace extends BaseAgent {
    public static final String SERVICE_TYPE = "marketplace",
        SNAPSHOT_PROTOCOL = "snapshot",
        BUY_CARDS_PROTOCOL = "buy-cards",
        SELL_CARDS_PROTOCOL = "sell-cards";

    public static final int MIN_SELLER_FEE = 3;
    public static final double SELLER_FEE = 0.05, BUYER_FEE = 0.05;
    private static final double TREND_FACTOR = 0.9;

    private final Map<Card, TreeSet<Listing>> listings = new HashMap<>();
    private final Map<Card, Double> priceTrends = new HashMap<>();
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
        addBehaviour(new LogInformation(this));
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
            priceTrends.compute(card, (k, v) -> v == null ? listing.price :
                TREND_FACTOR * v + (1 - TREND_FACTOR) * listing.price);
        }
    }

    public HashMap<Card, Snapshot> generateSnapshot() {
        HashMap<Card, Snapshot> snapshots = new HashMap<>();

        synchronized (listings) {
            for (Card card : listings.keySet()) {
                TreeSet<Listing> cardListings = listings.get(card);
    
                int count = cardListings.size();
                Integer minPrice = cardListings.isEmpty() ? null : cardListings.first().price;
                double priceTrend = priceTrends.get(card);
    
                snapshots.put(card, new Snapshot(count, minPrice, priceTrend));
            }
        }

        return snapshots;
    }

    public void addSubscription(AID aid, Subscription subscription) {
        subscriptions.put(aid, subscription);
    }

    public Transaction attemptPurchase(Transaction transaction, AID buyer) {
        List<Card> actualCards = new ArrayList<>();
        List<Integer> actualPrices = new ArrayList<>();
        Map<AID, Integer> sellerIncome = new HashMap<>();

        synchronized (listings) {
            for (int i = 0; i < transaction.cards.size(); ++i) {
                Card card = transaction.cards.get(i);
                Integer maxPrice = transaction.prices.get(i);
                Listing listing = attemptCardPurchase(card, maxPrice, buyer);

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

    private Listing attemptCardPurchase(Card card, int maxPrice, AID buyer) {
        if (!listings.containsKey(card)) return null;

        Stream<Listing> validListings = listings.get(card).stream().filter(l -> l.aid != buyer);
        try {
            Listing first = validListings.findFirst().get();

            if (calculateBuyerPrice(first.price) > maxPrice) {
                return null;
            }

            listings.get(card).remove(first);
            return first;
        }
        catch (NoSuchElementException e) {
            return null;
        }
    }

    private String cardInformation(Card card) {
        String info = "• " + card.idRarity() + ":\t";
        if (card.getRarity() == Rarity.RARE) info += " ";

        TreeSet<Listing> cardListings = listings.get(card);
        if (!cardListings.isEmpty()) {
            info += String.format("%d, from %.2f 💵 | ", cardListings.size(),
                (double) cardListings.first().price / 100);
        }
        else {
            info += String.format(" not available  | ");
        }

        info += String.format("Trend: %.2f 💵", priceTrends.get(card) / 100);

        return info;
    }

    private String cardInformation() {
        StringBuilder builder = new StringBuilder();

        synchronized (listings) {
            for (Card card : listings.keySet()) {
                builder.append("\n  ").append(cardInformation(card));
            }
        }

        return builder.toString();
    }

    private class LogInformation extends TickerBehaviour {
        private static final int INTERVAL_SECONDS = 120;

        public LogInformation(Marketplace marketplace) {
            super(marketplace, INTERVAL_SECONDS * 1000);
        }

        @Override
        protected void onTick() {
            String information = String.format("Accumulated capital: %.2f 💵", (double) getCapital() / 100);
            information += cardInformation();
            StringUtils.logAgentMessage(myAgent, information);
        }
    }
}
