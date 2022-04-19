package asma_proj1.utils;

import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Collection;

public class RandomUtils {
    public static final Random random = new Random(System.currentTimeMillis());

    private RandomUtils() {}

    public static double normalDistribution() {
        return normalDistribution(0, 1);
    }

    public static double normalDistribution(double mean, double stDev) {
        return random.nextGaussian() * stDev + mean;
    }

    public static <T> T choice(T[] array) {
        return array[random.nextInt(array.length)];
    }

    public static <T> Optional<T> choice(Collection<T> collection) {
        return collection.stream().skip(random.nextInt(collection.size())).findFirst();
    }

    public static <T> T weightedChoice(Map<T, Double> weightMap) {
        if (weightMap.isEmpty()) return null;

        double sum = weightMap.values().stream().mapToDouble(i -> i).sum(),
            r = random.nextDouble(),
            acc = 0.0;

        for (Map.Entry<T, Double> entry : weightMap.entrySet()) {
            acc += entry.getValue() / sum;
            if (r <= acc) {
                return entry.getKey();
            }
        }

        return weightMap.keySet().iterator().next();
    }
}
