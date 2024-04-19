package net.thewinnt.cutscenes.util;

public class MathHelper {
    private MathHelper() {}

    public static long hash(long... numbers) {
        long output = 1;
        for (long i : numbers) {
            output = 31 * output + i;
        }
        return output;
    }
}
