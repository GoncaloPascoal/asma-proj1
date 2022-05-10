package asma_proj1.agents.protocols.data;

import java.io.Serializable;
import java.util.List;

import asma_proj1.card.Card;
import asma_proj1.utils.StringUtils;

public class TradeOffer implements Serializable {
    public final List<Card> give, receive;

    public TradeOffer(List<Card> give, List<Card> receive) {
        this.give = give;
        this.receive = receive;
    }

    @Override
    public String toString() {
        List<String> giveIds = StringUtils.cardIds(give),
            receiveIds = StringUtils.cardIds(receive);

        return "    • " + StringUtils.colorize("OTHER GIVES:    ", StringUtils.GREEN)
            + StringUtils.colorize(giveIds.toString(), StringUtils.YELLOW)
            + "\n    • " + StringUtils.colorize("OTHER RECEIVES: ", StringUtils.RED)
            + StringUtils.colorize(receiveIds.toString(), StringUtils.YELLOW);
    }
}
