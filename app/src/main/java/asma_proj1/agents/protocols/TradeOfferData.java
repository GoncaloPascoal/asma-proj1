package asma_proj1.agents.protocols;

import java.util.List;

import asma_proj1.card.Card;
import asma_proj1.card.CardInstance;
import jade.util.leap.Serializable;

public class TradeOfferData implements Serializable {
    public List<Card> wanted;
    public List<CardInstance> offered;

    public TradeOfferData(List<Card> wanted, List<CardInstance> offered) {
        this.wanted = wanted;
        this.offered = offered;
    }
}
