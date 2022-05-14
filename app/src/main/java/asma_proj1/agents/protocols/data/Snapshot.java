package asma_proj1.agents.protocols.data;

import java.io.Serializable;

public class Snapshot implements Serializable {
    public final int count;
    public final Integer minPrice;
    public final double priceTrend;

    public Snapshot(int count, Integer minPrice, double priceTrend) {
        this.count = count;
        this.minPrice = minPrice;
        this.priceTrend = priceTrend;
    }
}
