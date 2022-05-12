package asma_proj1.agents.protocols.data;

import java.io.Serializable;

public class Snapshot implements Serializable {
    public final int count, minPrice;
    public final double averagePrice;

    public Snapshot(int count, int minPrice, double averagePrice) {
        this.count = count;
        this.minPrice = minPrice;
        this.averagePrice = averagePrice;
    }
}
