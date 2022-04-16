package asma_proj1.card;

import java.io.Serializable;

public class Card implements Serializable {
    private static int nextId = 0;

    private final int id;
    private final String name;
    private final Rarity rarity;

    public Card(String name, Rarity rarity) {
        this.id = nextId;
        this.name = name;
        this.rarity = rarity;
        ++nextId;
    }

    public int getId() {
        return id;
    }

    public Rarity getRarity() {
        return rarity;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Card [id = " + id + ", name = " + name + ", rarity = " + rarity + "]";
    }    
}
