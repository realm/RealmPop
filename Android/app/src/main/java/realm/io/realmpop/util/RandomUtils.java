package realm.io.realmpop.util;

import java.util.Random;

public class RandomUtils {

    private static Random rand = new Random(System.currentTimeMillis());

    public static int[] generateNumbersArray(int count, int min, int max) {
        int[] numbers = new int[count];
        for(int i = 0; i < count; i++) {
            numbers[i] = generateNumber(min, max);
        }
        return numbers;
    }

    public static int generateNumber(int min, int max) {
        return rand.nextInt((max - min) + 1) + min;
    }


}
