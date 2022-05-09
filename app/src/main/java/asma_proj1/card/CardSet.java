package asma_proj1.card;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import asma_proj1.utils.RandomUtils;

public class CardSet implements Serializable {
    public static final int PACK_PRICE = 50, SET_SIZE = 125;
    private static final Map<Rarity, Integer> RARITY_COUNT = Map.of(
        Rarity.COMMON, 50,
        Rarity.UNCOMMON, 40,
        Rarity.RARE, 35
    );

    private Card[] cards;
    private Map<Rarity, Card[]> rarityMap;

    public CardSet(CardGenerator generator) {
        cards = new Card[SET_SIZE];
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

    private Card randomCard(Rarity rarity) {
        return RandomUtils.choice(rarityMap.get(rarity));
    }

    public List<Card> openPack() {
        ArrayList<Card> pack = new ArrayList<>();
        pack.addAll(Collections.nCopies(14, null));

        for (int i = 0; i < 10; ++i) {
            pack.set(i, randomCard(Rarity.COMMON));
        }
        for (int i = 10; i < 13; ++i) {
            pack.set(i, randomCard(Rarity.UNCOMMON));
        }
        pack.set(13, randomCard(Rarity.RARE));

        return pack;
    }
}
