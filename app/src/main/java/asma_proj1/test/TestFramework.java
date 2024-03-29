package asma_proj1.test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import jade.core.Agent;
import jade.core.Profile;
import jade.core.Runtime;
import jade.core.ProfileImpl;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

import asma_proj1.agents.Marketplace;
import asma_proj1.card.CardSet;
import asma_proj1.card.CardSource;
import asma_proj1.agents.CardDatabase;
import asma_proj1.agents.CardOwner;
import asma_proj1.agents.Collector;
import asma_proj1.agents.CompetitivePlayer;
import asma_proj1.utils.LogPriority;
import asma_proj1.utils.RandomUtils;
import asma_proj1.utils.StringUtils;

public final class TestFramework {
    private static final Runtime runtime = Runtime.instance();
    private static final Profile profile = new ProfileImpl();

    static {
        profile.setParameter(Profile.SERVICES, "jade.core.messaging.TopicManagementService");
    }

    private static <T> void fillArray(T[] arr, Supplier<T> supplier) {
        for (int i = 0; i < arr.length; ++i) {
            arr[i] = supplier.get();
        }
    }

    private static void mainThreadSleep(int seconds) {
        try {
            Thread.sleep(seconds * 1000);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static String displayStatistic(String name, double value, String suffix) {
        return "• " + name + ": " + StringUtils.colorize(
            String.format("%.2f", value),
            StringUtils.CYAN
        ) + suffix + "\n";
    }

    private static String displayCapitalStatistic(String name, double value) {
        return displayStatistic(name, value / 100, " 💵");
    }

    private static void printCardOwnerStatistics(CardOwner[] cardOwners) {
        double estimatedCollectionValue = 0.0,
            marketplaceIncome = 0.0, spentInMarketplace = 0.0, spentInPacks = 0.0,
            remainingCapital = 0.0;
        for (CardOwner cardOwner : cardOwners) {
            estimatedCollectionValue += cardOwner.estimatedCollectionValue();
            marketplaceIncome += cardOwner.marketplaceIncome.get();
            spentInMarketplace += cardOwner.spentInMarketplace.get();
            spentInPacks += cardOwner.packsBought * CardSet.PACK_PRICE;
            remainingCapital += cardOwner.getCapital();
        }
        estimatedCollectionValue /= cardOwners.length;
        marketplaceIncome /= cardOwners.length;
        spentInMarketplace /= cardOwners.length;
        spentInPacks /= cardOwners.length;
        remainingCapital /= cardOwners.length;

        String statistics = displayCapitalStatistic("Average estimated collection value", 
            estimatedCollectionValue) + displayCapitalStatistic("Average marketplace income",
            marketplaceIncome) + displayCapitalStatistic("Average capital spent in marketplace",
            spentInMarketplace) + displayCapitalStatistic("Average capital spent in booster packs",
            spentInPacks) + displayCapitalStatistic("Average remaining capital (to spend)",
            remainingCapital);
        
        System.out.print(statistics);
    }

    private static void printAverageSourceRatio(Map<CardSource, Double> averageSourceRatio) {
        StringBuilder builder = new StringBuilder();
        for (CardSource source : CardSource.values()) {
            builder.append(String.format("    • %-20s ", source.name))
                .append(StringUtils.colorize(
                    String.format("%.2f", 100 * averageSourceRatio.get(source)),
                    StringUtils.CYAN
                ))
                .append("%\n");
        }
        System.out.println(builder.toString());
    }

    private static void printCollectorStatistics(Collector[] collectors) {
        double percentageAcquired = 0.0;
        Map<CardSource, Double> averageSourceRatio = new HashMap<>();
        for (Collector collector : collectors) {
            int desiredOwned = 0;
            Map<CardSource, Double> sourceRatio = new HashMap<>();

            percentageAcquired += collector.percentageAcquired();

            for (CardSource source : CardSource.values()) {
                int n = collector.sourceMap.getOrDefault(source, 0);
                sourceRatio.put(source, (double) n);
                desiredOwned += n;
            }

            for (CardSource source : CardSource.values()) {
                double ratio = sourceRatio.get(source) / desiredOwned;
                averageSourceRatio.compute(source, (k, v) -> v == null ?
                    ratio : v + ratio);
            }
        }

        percentageAcquired /= collectors.length;
        for (CardSource source : CardSource.values()) {
            averageSourceRatio.put(source, averageSourceRatio.get(source) / collectors.length);
        }

        System.out.print(displayStatistic(
            "Average percentage of desired cards acquired",
            percentageAcquired,
            "%"
        ));

        printAverageSourceRatio(averageSourceRatio);
    }

    private static void printCompetitivePlayerStatistics(CompetitivePlayer[] compPlayers) {
        double averageMin = 0.0, averageMax = 0.0;
        Map<CardSource, Double> averageSourceRatio = new HashMap<>();
        for (CompetitivePlayer compPlayer : compPlayers) {
            averageMin += compPlayer.collectionMinPower();
            averageMax += compPlayer.collectionMaxPower();

            int total = 0;
            Map<CardSource, Double> sourceRatio = new HashMap<>();

            for (CardSource source : compPlayer.sourceMap.values()) {
                sourceRatio.compute(source, (k, v) -> v == null ? 1 : v + 1);
                ++total;
            }

            for (CardSource source : CardSource.values()) {
                double ratio = sourceRatio.getOrDefault(source, 0.0) / total;
                averageSourceRatio.compute(source, (k, v) -> v == null ?
                    ratio : v + ratio);
            }
        }
        averageMin /= compPlayers.length;
        averageMax /= compPlayers.length;
        for (CardSource source : CardSource.values()) {
            averageSourceRatio.put(source, averageSourceRatio.get(source) / compPlayers.length);
        }

        System.out.println("• Average collection power range: " +
            StringUtils.colorize(String.format("[%.3f, %.3f]", averageMin, averageMax), StringUtils.CYAN));
        printAverageSourceRatio(averageSourceRatio);
    }

    private static void startAgents(ContainerController container, Agent[] agents, String prefix) throws StaleProxyException {
        AgentController controller;
        for (int i = 0; i < agents.length; ++i) {
            controller = container.acceptNewAgent(prefix + (i + 1), agents[i]);
            controller.start();
        }
    }

    private static final List<Runnable> tests = List.of(
        () -> {
            // Only Collectors
            ContainerController main = runtime.createMainContainer(profile);

            Marketplace marketplace = new Marketplace();
            CardDatabase database = new CardDatabase();
            Collector[] collectors = new Collector[12];
            fillArray(collectors, () -> new Collector());

            AgentController controller;

            try {
                controller = main.acceptNewAgent("marketplace", marketplace);
                controller.start();

                startAgents(main, collectors, "c");

                controller = main.acceptNewAgent("db", database);
                controller.start();

                mainThreadSleep(180);
                main.kill();

                System.out.print("\n\n------------ 📊 TEST STATISTICS 📊 ------------\n\n");
                System.out.println(displayCapitalStatistic(
                    "Marketplace capital (obtained through fees)",
                    marketplace.getCapital()
                ));
                printCollectorStatistics(collectors);
                printCardOwnerStatistics(collectors);
            }
            catch (StaleProxyException e) {
                e.printStackTrace();
            }
        },
        () -> {
            // Only CompetitivePlayers
            ContainerController main = runtime.createMainContainer(profile);

            Marketplace marketplace = new Marketplace();
            CardDatabase database = new CardDatabase();
            CompetitivePlayer[] compPlayers = new CompetitivePlayer[12];
            fillArray(compPlayers, () -> new CompetitivePlayer());

            AgentController controller;

            try {
                controller = main.acceptNewAgent("market", marketplace);
                controller.start();
                
                startAgents(main, compPlayers, "p");
                
                controller = main.acceptNewAgent("db", database);
                controller.start();

                mainThreadSleep(180);
                main.kill();

                System.out.print("\n\n------------ 📊 TEST STATISTICS 📊 ------------\n\n");
                System.out.println(displayCapitalStatistic(
                    "Marketplace capital (obtained through fees)",
                    marketplace.getCapital()
                ));
                printCompetitivePlayerStatistics(compPlayers);
                printCardOwnerStatistics(compPlayers);
            }
            catch (StaleProxyException e) {
                e.printStackTrace();
            }
        },
        () -> {
            // Mix Collectors and CompetitivePlayers
            ContainerController main = runtime.createMainContainer(profile);

            Marketplace marketplace = new Marketplace();
            CardDatabase database = new CardDatabase();
            Collector[] collectors = new Collector[6];
            fillArray(collectors, () -> new Collector());
            CompetitivePlayer[] compPlayers = new CompetitivePlayer[6];
            fillArray(compPlayers, () -> new CompetitivePlayer());

            AgentController controller;
            
            try {
                controller = main.acceptNewAgent("marketplace", marketplace);
                controller.start();

                startAgents(main, collectors, "c");
                startAgents(main, compPlayers, "p");

                controller = main.acceptNewAgent("db", database);
                controller.start();

                mainThreadSleep(180);
                main.kill();

                System.out.print("\n\n------------ 📊 TEST STATISTICS 📊 ------------\n\n");
                System.out.println(displayCapitalStatistic(
                    "Marketplace capital (obtained through fees)",
                    marketplace.getCapital()
                ));

                System.out.println("🖼️ -------- Collectors -------- 🖼️");
                printCollectorStatistics(collectors);
                printCardOwnerStatistics(collectors);

                System.out.println("🎲 ---- CompetitivePlayers ---- 🎲");
                printCompetitivePlayerStatistics(compPlayers);
                printCardOwnerStatistics(compPlayers);
            }
            catch (StaleProxyException e) {
                e.printStackTrace();
            }
        },
        () -> {
            // Groups
            ContainerController main = runtime.createMainContainer(profile);

            Marketplace marketplace = new Marketplace();
            CardDatabase database = new CardDatabase();
            Collector[] group1 = new Collector[4];
            fillArray(group1, () -> {
                Collector c = new Collector();
                c.group = "g1";
                return c;
            });
            Collector[] group2 = new Collector[8];
            fillArray(group2, () -> {
                Collector c = new Collector();
                c.group = "g2";
                return c;
            });

            AgentController controller;
            try {
                controller = main.acceptNewAgent("marketplace", marketplace);
                controller.start();

                startAgents(main, group1, "cs");
                startAgents(main, group2, "cb");

                controller = main.acceptNewAgent("db", database);
                controller.start();

                mainThreadSleep(180);
                main.kill();

                System.out.print("\n\n------------ 📊 TEST STATISTICS 📊 ------------\n\n");
                System.out.println(displayCapitalStatistic(
                    "Marketplace capital (obtained through fees)",
                    marketplace.getCapital()
                ));

                System.out.println("---------- Group 1 ----------");
                printCollectorStatistics(group1);
                printCardOwnerStatistics(group1);

                System.out.println("---------- Group 2 ----------");
                printCollectorStatistics(group2);
                printCardOwnerStatistics(group2);
            }
            catch (StaleProxyException e) {
                e.printStackTrace();
            }
        },
        () -> {
            // Preferences: Collectors
            ContainerController main = runtime.createMainContainer(profile);

            Marketplace marketplace = new Marketplace();
            CardDatabase database = new CardDatabase();
            Collector[] tradingAgents = new Collector[5];
            fillArray(tradingAgents, () -> {
                Collector c = new Collector();
                c.parameters.probBuyMarket = 0.1;
                c.parameters.probSellMarket = 0.1;
                return c;
            });
            Collector[] marketplaceAgents = new Collector[5];
            fillArray(marketplaceAgents, () -> {
                Collector c = new Collector();
                c.parameters.probTrade = 0.1;
                return c;
            });
            Collector[] otherAgents = new Collector[5];
            fillArray(otherAgents, () -> new Collector());

            AgentController controller;
            try {
                controller = main.acceptNewAgent("marketplace", marketplace);
                controller.start();

                startAgents(main, tradingAgents, "ct");
                startAgents(main, marketplaceAgents, "cm");
                startAgents(main, otherAgents, "o");

                controller = main.acceptNewAgent("db", database);
                controller.start();

                mainThreadSleep(180);
                main.kill();

                System.out.print("\n\n------------ 📊 TEST STATISTICS 📊 ------------\n\n");
                System.out.println(displayCapitalStatistic(
                    "Marketplace capital (obtained through fees)",
                    marketplace.getCapital()
                ));

                System.out.println("🤝 ---------- Trading ---------- 🤝");
                printCollectorStatistics(tradingAgents);
                printCardOwnerStatistics(tradingAgents);

                System.out.println("🏦 -------- Marketplace -------- 🏦");
                printCollectorStatistics(marketplaceAgents);
                printCardOwnerStatistics(marketplaceAgents);
            }
            catch (StaleProxyException e) {
                e.printStackTrace();
            }
        },
        () -> {
            // Avoids buying card packs
            ContainerController main = runtime.createMainContainer(profile);

            Marketplace marketplace = new Marketplace();
            CardDatabase database = new CardDatabase();
            Collector[] avoidPackAgents = new Collector[6];
            fillArray(avoidPackAgents, () -> {
                Collector c = new Collector();
                c.parameters.probBuyPack = 0.1;
                return c;
            });
            Collector[] otherAgents = new Collector[6];
            fillArray(otherAgents, () -> new Collector());

            AgentController controller;
            try {
                controller = main.acceptNewAgent("marketplace", marketplace);
                controller.start();

                startAgents(main, avoidPackAgents, "c");
                startAgents(main, otherAgents, "o");

                controller = main.acceptNewAgent("db", database);
                controller.start();

                mainThreadSleep(180);
                main.kill();

                System.out.print("\n\n------------ 📊 TEST STATISTICS 📊 ------------\n\n");
                System.out.println(displayCapitalStatistic(
                    "Marketplace capital (obtained through fees)",
                    marketplace.getCapital()
                ));

                System.out.println("-------- Avoid Packs --------");
                printCollectorStatistics(avoidPackAgents);
                printCardOwnerStatistics(avoidPackAgents);

                System.out.println("----------- Other -----------");
                printCollectorStatistics(otherAgents);
                printCardOwnerStatistics(otherAgents);
            }
            catch (StaleProxyException e) {
                e.printStackTrace();
            }
        }
    );

    private TestFramework() {}

    public static void runTest(int i) {
        if (i < 1 || i > tests.size()) {
            System.out.println(StringUtils.colorize(
                "Test number must be between 1 and " + tests.size(),
                StringUtils.RED
            ));
            return;
        }

        RandomUtils.random.setSeed(i);
        System.out.println("Executing test " +
            StringUtils.colorize(String.valueOf(i), StringUtils.CYAN) + "...");
        tests.get(i - 1).run();
    }

    public static void runDefaultSimulation() {
        StringUtils.MIN_LOG_PRIORITY = LogPriority.LOW;
        ContainerController main = runtime.createMainContainer(profile);

        Marketplace marketplace = new Marketplace();
        CardDatabase database = new CardDatabase();
        Collector[] ca = new Collector[4];
        fillArray(ca, () -> new Collector());
        CompetitivePlayer[] pa = new CompetitivePlayer[4];
        fillArray(pa, () -> new CompetitivePlayer());

        AgentController controller;
        
        try {
            controller = main.acceptNewAgent("marketplace", marketplace);
            controller.start();

            startAgents(main, ca, "c");
            startAgents(main, pa, "p");

            controller = main.acceptNewAgent("db", database);
            controller.start();
        }
        catch (StaleProxyException e) {
            e.printStackTrace();
        }
    }
}
