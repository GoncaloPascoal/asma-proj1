package asma_proj1.test;

import java.util.List;

import jade.core.Profile;
import jade.core.Runtime;
import jade.core.ProfileImpl;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

import asma_proj1.agents.Marketplace;
import asma_proj1.agents.CardDatabase;
import asma_proj1.agents.Collector;
import asma_proj1.agents.CompetitivePlayer;
import asma_proj1.utils.LogPriority;
import asma_proj1.utils.RandomUtils;
import asma_proj1.utils.StringUtils;

public final class TestFramework {
    private static final Runtime runtime = Runtime.instance();
    private static final Profile profile = new ProfileImpl();

    static {
        runtime.setCloseVM(true);
        profile.setParameter(Profile.SERVICES, "jade.core.messaging.TopicManagementService");
    }

    private static void mainThreadSleep(int seconds) {
        try {
            Thread.sleep(seconds * 1000);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static final List<Runnable> tests = List.of(
        () -> {
            // 10 Collectors
            RandomUtils.random.setSeed(1);
            StringUtils.MIN_LOG_PRIORITY = LogPriority.MEDIUM;

            ContainerController main = runtime.createMainContainer(profile);

            Marketplace marketplace = new Marketplace();
            CardDatabase database = new CardDatabase();
            Collector[] collectors = new Collector[10];
            for (int i = 0; i < collectors.length; ++i) {
                collectors[i] = new Collector();
            }

            AgentController controller;

            try {
                controller = main.acceptNewAgent("marketplace", marketplace);
                controller.start();

                for (int i = 0; i < collectors.length; ++i) {
                    controller = main.acceptNewAgent("c" + (i + 1), collectors[i]);
                    controller.start();
                }

                controller = main.acceptNewAgent("db", database);
                controller.start();

                mainThreadSleep(300);

                main.kill();
                System.out.println(marketplace.getCapital());
            }
            catch (StaleProxyException e) {
                e.printStackTrace();
            }
        },
        () -> {
            // 10 CompetitivePlayers
            RandomUtils.random.setSeed(1);

            ContainerController main = runtime.createMainContainer(profile);

            Marketplace marketplace = new Marketplace();
            CardDatabase database = new CardDatabase();
            CompetitivePlayer[] compPlayers = new CompetitivePlayer[10];
            for (int i = 0; i < compPlayers.length; ++i) {
                compPlayers[i] = new CompetitivePlayer();
            }

            AgentController controller;
            
            try {
                controller = main.acceptNewAgent("market", marketplace);
                controller.start();
                
                for (int i = 0; i < compPlayers.length; ++i) {
                    controller = main.acceptNewAgent("p" + (i + 1), compPlayers[i]);
                    controller.start();
                }
                
                controller = main.acceptNewAgent("db", database);
                controller.start();
            }
            catch (StaleProxyException e) {
                e.printStackTrace();
            }
        },
        () -> {
            // Mix collectors and competitives
            RandomUtils.random.setSeed(1);

            // ContainerController main = runtime.createMainContainer(profile);

            // Marketplace marketplace = new Marketplace();
            // CardDatabase database = new CardDatabase();
            // Collector[] collectors = new Collector[5];
            // CompetitivePlayer[] competitives = new CompetitivePlayer[5];
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

        System.out.println("Executing test " +
            StringUtils.colorize(String.valueOf(i), StringUtils.CYAN) + "...");
        tests.get(i - 1).run();
    }

    public static void runDefaultSimulation() {
        // ContainerController main = runtime.createMainContainer(profile);
    }
}
