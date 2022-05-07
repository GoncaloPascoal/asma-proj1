package asma_proj1.agents.protocols;

import java.io.Serializable;
import java.util.List;

import asma_proj1.card.CardInstance;

public class TradeOffer implements Serializable {
    public final List<CardInstance> give, receive;

    public TradeOffer(List<CardInstance> give, List<CardInstance> receive) {
        this.give = give;
        this.receive = receive;
    }
}
