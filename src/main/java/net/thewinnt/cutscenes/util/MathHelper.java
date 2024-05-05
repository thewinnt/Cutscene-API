package net.thewinnt.cutscenes.util;

/**
 * Contains some helpful(?) math functions.
 */
public class MathHelper {
    private MathHelper() {}

    /**
     * Hashes some numbers into a long hash
     * @param numbers the numbers to hash
     * @return the resulting hash
     */
    public static long hash(long... numbers) {
        long output = 1;
        for (long i : numbers) {
            output = 31 * output + i;
        }
        return output;
    }
}
