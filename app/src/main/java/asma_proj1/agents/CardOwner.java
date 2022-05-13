package asma_proj1.agents;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import jade.core.AID;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.Property;
import jade.domain.FIPAAgentManagement.ServiceDescription;

import asma_proj1.agents.protocols.BuyCardsInitiator;
import asma_proj1.agents.protocols.MarketplaceSubscriptionInitiator;
import asma_proj1.agents.protocols.SellCardsInitiator;
import asma_proj1.agents.protocols.SnapshotInitiator;
import asma_proj1.agents.protocols.TradeOfferInitiator;
import asma_proj1.agents.protocols.TradeOfferResponder;
import asma_proj1.agents.protocols.data.Snapshot;
import asma_proj1.agents.protocols.data.TradeOffer;
import asma_proj1.agents.protocols.data.TradeOfferData;
import asma_proj1.agents.protocols.data.Transaction;
import asma_proj1.card.Card;
import asma_proj1.card.CardSet;
import asma_proj1.card.Rarity;
import asma_proj1.utils.ConversionUtils;
import asma_proj1.utils.RandomUtils;
import asma_proj1.utils.StringUtils;

public abstract class CardOwner extends BaseAgent {
    public static final String DF_HAVE_TYPE = "have";
    protected static final Map<Rarity, Integer> basePrice = Map.of(
        Rarity.COMMON, 15,
        Rarity.UNCOMMON, 50,
        Rarity.RARE, 200
    );

    protected final Map<Card, Integer> collection = new HashMap<>();
    private final Map<Integer, Integer> cardsForTrade = new HashMap<>();
    public final Lock collectionLock = new ReentrantLock();

    protected final DFAgentDescription dfd = new DFAgentDescription();
    protected AID marketplace = null;
    protected Map<Card, Snapshot> latestSnapshot = new HashMap<>();

    @Override
    protected void setup() {
        super.setup();

        // Trading (yellow pages) setup
        dfd.setName(getAID());
        try {
            DFService.register(this, dfd);
        }
        catch (FIPAException e) {
            e.printStackTrace();
        }
        addBehaviour(new TradeOfferResponder(this));

        receiveCapital();
        addBehaviour(new ReceiveCapital(this));

        findMarketplace();
        addBehaviour(new CardOwnerBehaviour(this));
    }

    protected abstract CardSet selectSet();
    protected abstract void handleNewCards(List<Card> cards);

    protected abstract LinkedHashSet<Card> wantedCards();
    protected abstract ArrayList<Card> unwantedCards();

    protected abstract Set<AID> selectAgentsForTrade();
    public abstract ArrayList<Card> selectCardsForTrade(ArrayList<Card> offered);
    public abstract double evaluateTradeOffer(TradeOffer offer);

    public Map<Card, Integer> getCollection() {
        return Collections.unmodifiableMap(collection);
    }

    public void setLatestSnapshot(Map<Card, Snapshot> latestSnapshot) {
        this.latestSnapshot = latestSnapshot;
    }

    public boolean cardsInCollection(Collection<Card> cards) {
        Map<Card, Integer> countMap = ConversionUtils.collectionToCountMap(cards);

        for (Map.Entry<Card, Integer> entry : countMap.entrySet()) {
            if (collection.getOrDefault(entry.getKey(), 0) < entry.getValue()) {
                return false;
            }
        }

        return true;
    }

    protected Set<AID> selectAgentsWithCards(Set<Card> cards) {
        Set<AID> agents = new HashSet<>();

        for (Card card : cards) {
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType(CardOwner.DF_HAVE_TYPE);
            sd.setName(String.valueOf(card.getId()));
            template.addServices(sd);

            try {
                DFAgentDescription[] results = DFService.search(this, template);
                for (DFAgentDescription result : results) {
                    if (result.getName() != getAID()) {
                        agents.add(result.getName());
                    }
                }
            }
            catch (FIPAException e) {
                e.printStackTrace();
            }
        }

        return agents;
    }

    protected void receiveCapital() {
        int capital = RandomUtils.intRangeInclusive(400, 900);
        changeCapital(capital);
        StringUtils.logAgentMessage(this, "Received periodic capital: " +
            changeCapitalMessage(capital));
    }

    protected void purchasePack(CardSet set) {
        if (changeCapital(-CardSet.PACK_PRICE)) {
            StringUtils.logAgentMessage(this, "Purchased a card pack: " + changeCapitalMessage(-CardSet.PACK_PRICE));

            List<Card> pack = set.openPack();
            addCardsToCollection(pack);
        }
    }

    public void addCardsToCollection(List<Card> cards) {
        for (Card card : cards) {
            collection.compute(card, (k, v) -> v == null ? 1 : v + 1);
        }
        handleNewCards(cards);
    }

    public void removeCardsFromCollection(List<Card> cards) {
        for (Card card : cards) {
            collection.compute(card, (k, v) -> v == null || v == 1 ? null : v - 1);
        }
        unlistCards(cards);
    }

    protected void updateDfd() {
        try {
            DFService.modify(this, dfd);
        }
        catch (FIPAException e) {
            e.printStackTrace();
        }
    }

    protected void listCards(List<Card> cards) {
        Map<Integer, Integer> countMap = new HashMap<>();
        for (Card card : cards) {
            countMap.compute(card.getId(), (k, v) -> v == null ? 1 : v + 1);
        }

        for (Map.Entry<Integer, Integer> entry : countMap.entrySet()) {
            cardsForTrade.compute(entry.getKey(), (k, v) -> v == null ? entry.getValue() : v + entry.getValue());
        }

        Iterator<?> it = dfd.getAllServices();
        while (it.hasNext()) {
            ServiceDescription sd = (ServiceDescription) it.next();

            if (sd.getType() == DF_HAVE_TYPE) {
                int id = Integer.valueOf(sd.getName());

                if (countMap.containsKey(id)) {
                    sd.removeProperties(new Property("count", null));
                    sd.addProperties(new Property("count", cardsForTrade.get(id)));
                    countMap.remove(id);
                }
            }
        }

        for (Map.Entry<Integer, Integer> entry : countMap.entrySet()) {
            int id = entry.getKey();
            ServiceDescription sd = new ServiceDescription();
            sd.setType(DF_HAVE_TYPE);
            sd.setName(String.valueOf(id));
            sd.addProperties(new Property("count", cardsForTrade.get(id)));
            dfd.addServices(sd);
        }

        updateDfd();
    }

    protected void unlistCards(List<Card> cards) {
        for (Card card : cards) {
            cardsForTrade.compute(card.getId(), (k, v) -> v == null || v == 1 ? null : v - 1);
        }

        Iterator<?> it = dfd.getAllServices();
        while (it.hasNext()) {
            ServiceDescription sd = (ServiceDescription) it.next();

            if (sd.getType() == DF_HAVE_TYPE) {
                int id = Integer.valueOf(sd.getName());

                if (cardsForTrade.containsKey(id)) {
                    sd.removeProperties(new Property("count", null));
                    sd.addProperties(new Property("count", cardsForTrade.get(id)));
                }
                else {
                    it.remove();
                }
            } 
        }

        updateDfd();
    }

    protected void findMarketplace() {
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType(Marketplace.SERVICE_TYPE);

        try {
            DFAgentDescription[] results = DFService.search(this, template);
            if (results.length > 0) {
                marketplace = results[0].getName();
                addBehaviour(new MarketplaceSubscriptionInitiator(this, marketplace));
            }
        }
        catch (FIPAException e) {
            e.printStackTrace();
        }
    }

    protected Transaction selectCardsToBuy() {
        List<Card> cards = new ArrayList<>();
        List<Integer> prices = new ArrayList<>();
        int totalPrice = 0;

        for (Card card : wantedCards()) {
            // TODO: Create new constants for probability / ratios
            if (RandomUtils.random.nextDouble() <= 0.25) {
                int maxPrice = evaluateMaxBuyPrice(card);
                if (totalPrice + maxPrice <= getCapital()) {
                    totalPrice += maxPrice;
                    cards.add(card);
                    prices.add(maxPrice);

                    if (RandomUtils.random.nextDouble() > 1 / Math.exp(0.16 * cards.size())) {
                        break;
                    }
                }
            }
        }

        return new Transaction(cards, prices);
    }

    protected Transaction selectCardsToSell() {
        Transaction transaction = null;
        List<Card> cards = unwantedCards();
        int numMarketplace = Math.min(
            cards.size(),
            RandomUtils.intRangeInclusive(0, 8)
        );

        if (numMarketplace > 0) {
            Collections.shuffle(cards);
            cards = new ArrayList<>(cards.subList(0, numMarketplace));

            List<Integer> prices = cards.stream().map(
                c -> evaluateSellPrice(c)
            ).collect(Collectors.toList());

            transaction = new Transaction(cards, prices);
        }

        return transaction;
    }

    protected int evaluateSellPrice(Card card) {
        if (latestSnapshot.containsKey(card)) {
            Snapshot snapshot = latestSnapshot.get(card);

            // TODO: custom parameters depending on rarity
            double multiplier = 1.05 / Math.min(0.85, Math.exp(0.0035 * snapshot.count));
            multiplier *= RandomUtils.doubleRangeInclusive(0.95, 1.05);

            return (int) (snapshot.averagePrice * multiplier);
        }

        return (int) ((double) basePrice.get(card.getRarity()) *
            RandomUtils.doubleRangeInclusive(0.9, 1.1));
    }

    protected int evaluateMaxBuyPrice(Card card) {
        if (latestSnapshot.containsKey(card)) {
            Snapshot snapshot = latestSnapshot.get(card);
            return (int) (snapshot.minPrice *
                RandomUtils.doubleRangeInclusive(1.0, 1.4));
        }

        return (int) (basePrice.get(card.getRarity()) *
            RandomUtils.doubleRangeInclusive(1.0, 1.2));
    }

    public TradeOffer generateTradeOffer(TradeOfferData data) {
        if (data.offered.isEmpty()) return null;
        List<Card> give = new ArrayList<>(), receive = new ArrayList<>();
        List<Card> canGive = unwantedCards();

        Map<Card, Integer> priorityMap = new HashMap<>();
        int i = 1;
        for (Card card : data.wanted) {
            priorityMap.put(card, i);
            ++i;
        }

        Comparator<Card> comparator = Comparator.comparingDouble(c -> priorityMap.getOrDefault(c, 0));
        Map<Rarity, PriorityQueue<Card>> rarityPriorityMap = new HashMap<>();
        for (Rarity rarity : Rarity.values()) {
            // PriorityQueue is a min-heap by default
            rarityPriorityMap.put(rarity, new PriorityQueue<>(comparator.reversed()));
        }
        for (Card card : canGive) {
            Rarity rarity = card.getRarity();
            rarityPriorityMap.get(rarity).add(card);
        }

        while (!data.offered.isEmpty() && !canGive.isEmpty()) {
            Card rCard = data.offered.remove(data.offered.size() - 1);
            Rarity rarity = rCard.getRarity();

            if (rarityPriorityMap.get(rarity).size() > 0) {
                Card gCard = rarityPriorityMap.get(rarity).poll();

                receive.add(rCard);
                give.add(gCard);
            }
        }

        if (give.isEmpty() || receive.isEmpty()) {
            return null;
        }

        return new TradeOffer(give, receive);
    }

    private class ReceiveCapital extends TickerBehaviour {
        private static final int INTERVAL_SECONDS = 25;

        public ReceiveCapital(CardOwner cardOwner) {
            super(cardOwner, INTERVAL_SECONDS * 1000);
        }

        @Override
        protected void onTick() {
            receiveCapital();
        }
    }

    private class CardOwnerBehaviour extends TickerBehaviour {
        private static final int INTERVAL_SECONDS = 15;
        private final CardOwner cardOwner;

        public CardOwnerBehaviour(CardOwner cardOwner) {
            super(cardOwner, INTERVAL_SECONDS * 1000);
            this.cardOwner = cardOwner;
        }

        @Override
        protected void onTick() {
            if (marketplace == null) {
                findMarketplace();
            }
            else {
                addBehaviour(new SnapshotInitiator(cardOwner, marketplace));
            }

            block(500);

            // Buying in marketplace
            if (marketplace != null) {
                Transaction transaction = selectCardsToBuy();
                if (transaction != null && !transaction.isEmpty()) {
                    if (cardOwner.changeCapital(-transaction.totalPrice())) {
                        addBehaviour(new BuyCardsInitiator(cardOwner, marketplace, transaction));
                        block(500);
                    }
                    else {
                        StringUtils.logAgentError(cardOwner,
                            "Couldn't pay specified maximum price for marketplace cards.");
                    }
                }
            }

            if (!collection.isEmpty()) {
                // Selling in marketplace
                if (marketplace != null) {
                    Transaction transaction = selectCardsToSell();
                    if (transaction != null && !transaction.isEmpty()) {
                        if (cardOwner.changeCapital(-Marketplace.calculateSellerFee(transaction))) {
                            cardOwner.collectionLock.lock();
                            cardOwner.removeCardsFromCollection(transaction.cards);
                            cardOwner.collectionLock.unlock();
                            addBehaviour(new SellCardsInitiator(cardOwner, marketplace, transaction));
                            block(500);
                        }
                    }
                }

                // Trading
                Set<AID> agents = selectAgentsForTrade();
                if (!agents.isEmpty()) {
                    ArrayList<Card> offered = unwantedCards();
    
                    if (!offered.isEmpty()) {
                        StringUtils.logAgentMessage(myAgent, "📢 Found " + agents.size() +
                        " possible agents to trade with.");
    
                        addBehaviour(new TradeOfferInitiator(cardOwner,
                            new TradeOfferData(wantedCards(), offered), agents));
                    }
                }
            }

            if (!cardSets.isEmpty()) {
                CardSet set = selectSet();
                if (set != null) {
                    purchasePack(set);
                }
            }
        }
    }
}
