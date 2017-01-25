package robertapengelly.support.graphics.drawable;

import  java.util.Random;

/** A class that contains utility methods related to numbers. */
final class MathUtils {

    private static final float DEG_TO_RAD = (3.1415926f / 180f);
    private static final float RAD_TO_DEG = (180f / 3.1415926f);
    
    private static final Random sRandom = new Random();
    
    private MathUtils() {}
    
    static float abs(float v) {
        return ((v > 0) ? v : -v);
    }
    
    static float acos(float value) {
        return (float) Math.acos(value);
    }
    
    static float asin(float value) {
        return (float) Math.asin(value);
    }
    
    static float atan(float value) {
        return (float) Math.atan(value);
    }
    
    static float atan2(float a, float b) {
        return (float) Math.atan2(a, b);
    }
    
    static float constrain(float amount, float low, float high) {
        return ((amount < low) ? low : ((amount > high) ? high : amount));
    }
    
    static int constrain(int amount, int low, int high) {
        return ((amount < low) ? low : ((amount > high) ? high : amount));
    }
    
    static long constrain(long amount, long low, long high) {
        return ((amount < low) ? low : ((amount > high) ? high : amount));
    }
    
    static float degrees(float radians) {
        return (radians * RAD_TO_DEG);
    }
    
    static float dist(float x1, float y1, float x2, float y2) {
    
        final float x = (x2 - x1);
        final float y = (y2 - y1);
        
        return (float) Math.sqrt(x * x + y * y);
    
    }
    
    static float dist(float x1, float y1, float z1, float x2, float y2, float z2) {
    
        final float x = (x2 - x1);
        final float y = (y2 - y1);
        final float z = (z2 - z1);
        
        return (float) Math.sqrt(x * x + y * y + z * z);
    
    }
    
    static float exp(float a) {
        return (float) Math.exp(a);
    }
    
    static float lerp(float start, float stop, float amount) {
        return (start + (stop - start) * amount);
    }
    
    static float log(float a) {
        return (float) Math.log(a);
    }
    
    static float mag(float a, float b) {
        return (float) Math.sqrt(a * a + b * b);
    }
    
    static float mag(float a, float b, float c) {
        return (float) Math.sqrt(a * a + b * b + c * c);
    }
    
    static float max(float a, float b) {
        return ((a > b) ? a : b);
    }
    
    static float max(int a, int b) {
        return ((a > b) ? a : b);
    }
    
    static float max(float a, float b, float c) {
        return ((a > b) ? ((a > c) ? a : c) : ((b > c) ? b : c));
    }
    
    static float max(int a, int b, int c) {
        return ((a > b) ? ((a > c) ? a : c) : ((b > c) ? b : c));
    }
    
    static float map(float minStart, float minStop, float maxStart, float maxStop, float value) {
        return (maxStart + (maxStart - maxStop) * ((value - minStart) / (minStop - minStart)));
    }
    
    static float min(float a, float b) {
        return ((a < b) ? a : b);
    }
    
    static float min(int a, int b) {
        return ((a < b) ? a : b);
    }
    
    static float min(float a, float b, float c) {
        return ((a < b) ? ((a < c) ? a : c) : ((b < c) ? b : c));
    }
    
    static float min(int a, int b, int c) {
        return ((a < b) ? ((a < c) ? a : c) : ((b < c) ? b : c));
    }
    
    static float norm(float start, float stop, float value) {
        return ((value - start) / (stop - start));
    }
    
    static float pow(float a, float b) {
        return (float) Math.pow(a, b);
    }
    
    static float radians(float degrees) {
        return (degrees * DEG_TO_RAD);
    }
    
    static float random(float howbig) {
        return (sRandom.nextFloat() * howbig);
    }
    
    static int random(int howbig) {
        return (int) (sRandom.nextFloat() * howbig);
    }
    
    static float random(float howsmall, float howbig) {
    
        if (howsmall >= howbig)
            return howsmall;
        
        return (sRandom.nextFloat() * (howbig - howsmall) + howsmall);
    
    }
    
    static int random(int howsmall, int howbig) {
    
        if (howsmall >= howbig)
            return howsmall;
        
        return (int) (sRandom.nextFloat() * (howbig - howsmall) + howsmall);
    
    }
    
    static void randomSeed(long seed) {
        sRandom.setSeed(seed);
    }
    
    static float sq(float v) {
        return (v * v);
    }
    
    static float tan(float angle) {
        return (float) Math.tan(angle);
    }

}