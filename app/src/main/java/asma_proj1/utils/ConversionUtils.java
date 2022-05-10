package asma_proj1.utils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public final class ConversionUtils {
    private ConversionUtils() {}

    public static <T> Map<T, Integer> collectionToCountMap(Collection<T> collection) {
        Map<T, Integer> countMap = new HashMap<>();
        for (T elem : collection) {
            countMap.compute(elem, (k, v) -> v == null ? 1 : v + 1);
        }
        return countMap;
    }
}
