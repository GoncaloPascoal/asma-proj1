package asma_proj1.card;

public enum CardSource {
    BOOSTER_PACK("Booster Pack"),
    MARKETPLACE("Marketplace"),
    TRADING("Trading");

    public final String name;

    CardSource(String name) {
        this.name = name;
    }
}
