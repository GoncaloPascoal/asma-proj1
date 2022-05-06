package asma_proj1.card;

public enum Rarity {
    COMMON("⚫"), UNCOMMON("⚪"), RARE("🟡");

    public final String symbol;

    Rarity(String symbol) {
        this.symbol = symbol;
    }
}
