
package asma_proj1;

import asma_proj1.test.TestFramework;

public class App {
    public static void main(String[] args) {
        if (args.length > 0) {
            try {
                int i = Integer.parseInt(args[0]);
                TestFramework.runTest(i);
                System.exit(0);
            }
            catch (NumberFormatException e) {
                System.err.println("Test number must be an integer.");
            }
        }
        else {
            TestFramework.runDefaultSimulation();
        }
    }
}
