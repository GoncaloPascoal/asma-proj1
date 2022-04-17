package asma_proj1.card;

import java.util.Map;

import asma_proj1.utils.RandomUtils;

public class CardSet {
    private static final double foilChance = 0.2;
    private static final Map<Rarity, Double> foilRarityMap = Map.of(
        Rarity.COMMON, 0.7,
        Rarity.UNCOMMON, 0.2,
        Rarity.RARE, 0.1
    );

    private Card[] cards;
    private Map<Rarity, Card[]> rarityMap;

    private CardInstance randomCard(Rarity rarity, boolean foil) {
        return new CardInstance(RandomUtils.choice(rarityMap.get(rarity)), foil);
    }

    private CardInstance randomCard(Rarity rarity) {
        return randomCard(rarity, false);
    }

    public CardInstance[] openPack() {
        CardInstance[] packCards = new CardInstance[14];

        for (int i = 0; i < 9; ++i) {
            packCards[i] = randomCard(Rarity.COMMON);
        }

        // If pack has a foil card, it will replace one of the common cards
        boolean foil = RandomUtils.random.nextDouble() <= foilChance;
        Rarity rarity = Rarity.COMMON;
        if (foil) {
            rarity = RandomUtils.weightedChoice(foilRarityMap);
        }
        packCards[9] = randomCard(rarity, foil);

        for (int i = 10; i < 13; ++i) {
            packCards[i] = randomCard(Rarity.UNCOMMON);         
        }

        packCards[13] = randomCard(Rarity.RARE);

        return packCards;
    }
}
