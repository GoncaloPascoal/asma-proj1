package asma_proj1.utils;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import jade.core.Agent;

import asma_proj1.card.Card;

public class StringUtils {
    public static final String RED = "\033[1;31m",
        GREEN = "\033[1;32m",
        YELLOW = "\033[1;33m",
        BLUE = "\033[1;34m",
        CYAN = "\033[1;36m";

    private static final String RESET = "\u001B[0m";

    private StringUtils() {}

    public static String colorize(String str, String color) {
        return color + str + RESET;
    }

    public static void logError(String str) {
        System.err.println(colorize(str, RED));
    }

    public static void logAgentMessage(Agent agent, String str) {
        System.out.println(colorize("Agent '" + agent.getLocalName() + "': ", BLUE) + str);
    }

    public static List<String> cardIds(Collection<Card> cards) {
        return cards.stream().map(c -> c.idRarity()).collect(Collectors.toList());
    }
}
