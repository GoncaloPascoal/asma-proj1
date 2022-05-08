package asma_proj1.card;

import java.io.Serializable;
import java.util.Objects;

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

    public String idRarity() {
        return id + " " + rarity.symbol;
    }

    @Override
    public String toString() {
        return "Card [id = " + id + ", name = " + name +
            ", rarity = " + rarity + ", power = " + power + "]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass()) return false;
        Card other = (Card) obj;
        return id == other.id;
    }
}
