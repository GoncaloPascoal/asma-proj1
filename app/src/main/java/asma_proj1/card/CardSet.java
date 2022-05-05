package asma_proj1.card;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import asma_proj1.utils.RandomUtils;

public class CardSet implements Serializable {
    public static final int PACK_PRICE = 25;

    private static final Map<Rarity, Integer> RARITY_COUNT = Map.of(
        Rarity.COMMON, 100,
        Rarity.UNCOMMON, 80,
        Rarity.RARE, 70
    );
    private static final double FOIL_CHANCE = 0.2;
    private static final Map<Rarity, Double> FOIL_RARITY_MAP = Map.of(
        Rarity.COMMON, 0.7,
        Rarity.UNCOMMON, 0.2,
        Rarity.RARE, 0.1
    );

    private Card[] cards;
    private Map<Rarity, Card[]> rarityMap;

    public CardSet(CardGenerator generator) {
        final int setSize = RARITY_COUNT.values().stream().mapToInt(n -> n).sum();
        cards = new Card[setSize];
        rarityMap = new HashMap<>();

        int i = 0;
        for (Rarity rarity : RARITY_COUNT.keySet()) {
            int count = RARITY_COUNT.get(rarity);
            rarityMap.put(rarity, new Card[count]);

            for (int j = 0; j < count; ++j, ++i) {
                cards[i] = generator.generateCard(rarity);
                rarityMap.get(rarity)[j] = cards[i];
            }
        }
    }

    public Card[] getCards() {
        return cards;
    }

    private CardInstance randomCard(Rarity rarity, boolean foil) {
        return new CardInstance(RandomUtils.choice(rarityMap.get(rarity)), foil);
    }

    private CardInstance randomCard(Rarity rarity) {
        return randomCard(rarity, false);
    }

    public List<CardInstance> openPack() {
        ArrayList<CardInstance> pack = new ArrayList<>();
        pack.ensureCapacity(14);

        for (int i = 0; i < 9; ++i) {
            pack.set(i, randomCard(Rarity.COMMON));
        }

        // If pack has a foil card, it will replace one of the common cards
        boolean foil = RandomUtils.random.nextDouble() <= FOIL_CHANCE;
        Rarity rarity = Rarity.COMMON;
        if (foil) {
            rarity = RandomUtils.weightedChoice(FOIL_RARITY_MAP);
        }
        pack.set(9, randomCard(rarity, foil));

        for (int i = 10; i < 13; ++i) {
            pack.set(i, randomCard(Rarity.UNCOMMON));
        }

        pack.set(13, randomCard(Rarity.RARE));

        return pack;
    }
}
