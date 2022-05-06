package asma_proj1.card;

public class CardInstance {
    private Card card;
    private boolean foil;

    public CardInstance(Card card, boolean foil) {
        this.card = card;
        this.foil = foil;
    }

    public Card getCard() {
        return card;
    }

    public boolean isFoil() {
        return foil;
    }

    @Override
    public String toString() {
        return "CardInstance [name = " + card.getName() + ", foil = " + foil + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass()) return false;
        CardInstance other = (CardInstance) obj;
        return card == other.card;
    }
}
