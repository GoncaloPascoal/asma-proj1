package asma_proj1.card;

import java.io.Serializable;

public class Card implements Serializable {
    private final int id;
    private final String name;
    private final Rarity rarity;
    private final double power;

    public Card(int id, String name, Rarity rarity, double power) {
        this.id = id;
        this.name = name;
        this.rarity = rarity;
        this.power = power;
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
    
    public double getPower() {
        return power;
    }

    @Override
    public String toString() {
        return "Card [id = " + id + ", name = " + name +
            ", rarity = " + rarity + ", power = " + power + "]";
    }
}
