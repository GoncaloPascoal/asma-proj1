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
        Card card = new Card(nextId, generateName(), rarity, RandomUtils.normalDistribution());
        ++nextId;
        return card;
    }

    private enum PartOfSpeech {
        NOUN,
        ADJECTIVE,
        PREPOSITION,
    }
}
