package asma_proj1.card;

import java.util.Map;

import asma_proj1.utils.RandomUtils;

public final class CardGenerator {
    private static final PartOfSpeech[][] nameTemplates = {
        {PartOfSpeech.NOUN, PartOfSpeech.PREPOSITION, PartOfSpeech.NOUN},
        {PartOfSpeech.ADJECTIVE, PartOfSpeech.NOUN}
    };
    private static final Map<PartOfSpeech, String[]> wordMap = Map.of(
        PartOfSpeech.NOUN, new String[] {"Knight", "Cleric", "Rogue", "Barbarian", "Doom", "Light", "Earth", "Fire", "Water"},
        PartOfSpeech.PREPOSITION, new String[] {"of", "from", "in"},
        PartOfSpeech.ADJECTIVE, new String[] {"Brave", "Epic", "Powerful", "Weak", "Horrible", "Holy", "Cruel", "Kind", "Destructive"}
    );
    private static final Map<Rarity, Double> powerMean = Map.of(
        Rarity.COMMON, 0.0,
        Rarity.UNCOMMON, 0.2,
        Rarity.RARE, 0.5
    );
    private static final Map<Rarity, Double> powerStDev = Map.of(
        Rarity.COMMON, 0.8,
        Rarity.UNCOMMON, 1.0,
        Rarity.RARE, 1.5
    );

    private int nextId = 0;

    public String generateName() {
        PartOfSpeech[] template = RandomUtils.choice(nameTemplates);
        String[] words = new String[template.length];

        for (int i = 0; i < template.length; ++i) {
            words[i] = RandomUtils.choice(wordMap.get(template[i]));
        }

        return String.join(" ", words);
    }

    public Card generateCard(Rarity rarity) {
        Card card = new Card(nextId, generateName(), rarity, RandomUtils.normalDistribution(
            powerMean.get(rarity),
            powerStDev.get(rarity)
        ));
        ++nextId;
        return card;
    }

    private enum PartOfSpeech {
        NOUN,
        ADJECTIVE,
        PREPOSITION,
    }
}
