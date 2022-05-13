package asma_proj1.agents.protocols.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashSet;

import asma_proj1.card.Card;

public class TradeOfferData implements Serializable {
    public LinkedHashSet<Card> wanted;
    public ArrayList<Card> offered;

    public TradeOfferData(LinkedHashSet<Card> wanted, ArrayList<Card> offered) {
        this.wanted = wanted;
        this.offered = offered;
    }
}
