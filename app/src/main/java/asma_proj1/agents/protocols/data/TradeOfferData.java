package asma_proj1.agents.protocols.data;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import asma_proj1.card.Card;

public class TradeOfferData implements Serializable {
    public Set<Card> wanted;
    public List<Card> offered;

    public TradeOfferData(Set<Card> wanted, List<Card> offered) {
        this.wanted = wanted;
        this.offered = offered;
    }
}
