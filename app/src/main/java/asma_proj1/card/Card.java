package asma_proj1.card;

import java.io.Serializable;

public class Card implements Serializable {
    private int id;
    private String name;
    private Rarity rarity;

    public int getId() {
        return id;
    }

    public Rarity getRarity() {
        return rarity;
    }

    public String getName() {
        return name;
    }
}
