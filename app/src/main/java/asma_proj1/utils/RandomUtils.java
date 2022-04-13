package asma_proj1.utils;

import java.util.Random;

public class RandomUtils {
    private static Random random = new Random(System.currentTimeMillis());

    public static double normalDistribution() {
        return normalDistribution(0, 1);
    }

    public static double normalDistribution(double mean, double stDev) {
        return random.nextGaussian() * stDev + mean;
    }
}
