package realm.io.realmpop.util;

import java.util.Random;

public class RandomNumberUtils {

    private static Random rand = new Random(System.currentTimeMillis());

    public static int[] generateNumbersArray(int count, int min, int max) {

        int eachNumberRange = Math.round(((float) max) / ((float) count));
        int prevNum = min;
        int maxNum = eachNumberRange;

        int[] numbers = new int[count];
        for(int i = 0; i < count; i++) {
            numbers[i] = generateNumber(prevNum + 1, maxNum);
            prevNum = numbers[i];
            maxNum = prevNum + eachNumberRange;
        }
        return numbers;
    }

    public static int generateNumber(int min, int max) {
        return rand.nextInt((max - min) + 1) + min;
    }


}
