package asma_proj1.agents;

import jade.core.Agent;

public class BaseAgent extends Agent {
    private int capital = 0;

    protected void setup() {
        System.out.println("Setting up BaseAgent...");
        System.out.println("BaseAgent " + getAID().getName() + " is ready!");
    }

    // Put agent clean-up operations here
    protected void takeDown() {
        System.out.println("Base agent " + getAID().getName() + " terminating.");
    }

    public int getCapital() {
        return capital;
    }

    public boolean changeCapital(int delta) {
        int newCapital = capital + delta;
        
        if (newCapital >= 0) {
            capital = newCapital;
        }

        return false;
    }
}
