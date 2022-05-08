package asma_proj1.agents.protocols;

import java.io.Serializable;
import java.util.List;

import asma_proj1.card.Card;
import asma_proj1.card.CardInstance;

public class TradeOfferData implements Serializable {
    public List<Card> wanted;
    public List<CardInstance> offered;

    public TradeOfferData(List<Card> wanted, List<CardInstance> offered) {
        this.wanted = wanted;
        this.offered = offered;
    }
}
