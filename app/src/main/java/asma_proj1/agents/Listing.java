package asma_proj1.agents;

import jade.core.AID;

public class Listing implements Comparable<Listing> {
    public final AID aid;
    public final int price;

    public Listing(AID aid, int price) {
        this.aid = aid;
        this.price = price;
    }

    @Override
    public int compareTo(Listing other) {
        return Integer.compare(price, other.price);
    }
}
