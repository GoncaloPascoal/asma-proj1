
package asma_proj1;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

public class App {
    public static void main(String[] args) {
        Runtime runtime = Runtime.instance();
        Profile profile = new ProfileImpl();
        
        profile.setParameter(Profile.SERVICES, "jade.core.messaging.TopicManagementService");

        ContainerController mainContainer = runtime.createMainContainer(profile);

        try {
            AgentController rma = mainContainer.createNewAgent("rma", "jade.tools.rma.rma", null);
            rma.start();

            AgentController marketplace = mainContainer.createNewAgent("marketplace", "asma_proj1.agents.Marketplace", null);
            marketplace.start();

            AgentController collector = mainContainer.createNewAgent("c1", "asma_proj1.agents.Collector", null);
            collector.start();

            AgentController collector2 = mainContainer.createNewAgent("c2", "asma_proj1.agents.Collector", null);
            collector2.start();

            AgentController collector3 = mainContainer.createNewAgent("c3", "asma_proj1.agents.Collector", null);
            collector3.start();

            AgentController database = mainContainer.createNewAgent("db", "asma_proj1.agents.CardDatabase", null);
            database.start();
        }
        catch (StaleProxyException e) {
            e.printStackTrace();
        }
    }
}
