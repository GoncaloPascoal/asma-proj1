package asma_proj1.agents.protocols;

import java.util.List;

import asma_proj1.card.CardInstance;
import jade.util.leap.Serializable;

public class TradeOfferData implements Serializable {
    public final List<CardInstance> wanted, offered;

    public TradeOfferData(List<CardInstance> wanted, List<CardInstance> offered) {
        this.wanted = wanted;
        this.offered = offered;
    }
}
