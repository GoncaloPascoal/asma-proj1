package asma_proj1.agents.protocols;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import asma_proj1.card.Card;
import asma_proj1.card.CardInstance;

public class TradeOfferData implements Serializable {
    public Set<Card> wanted;
    public List<CardInstance> offered;

    public TradeOfferData(Set<Card> wanted, List<CardInstance> offered) {
        this.wanted = wanted;
        this.offered = offered;
    }
}
