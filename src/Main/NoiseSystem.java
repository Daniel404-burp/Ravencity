package Main;

public final class NoiseSystem {

    private static final int GUNSHOT_RADIUS = 450;
    private static final long NOISE_DURATION = 5000L;

    private static int noiseX = 0;
    private static int noiseY = 0;
    private static long noiseTime = 0L;

    private NoiseSystem() {
    }

    /**
     * Creates one temporary world noise event.
     * Gunshots use this to attract nearby infected to Ethan's firing position.
     */
    public static void createNoise(int x, int y) {
        noiseX = x;
        noiseY = y;
        noiseTime = System.currentTimeMillis();
    }

    public static boolean isActive() {
        return System.currentTimeMillis() - noiseTime < NOISE_DURATION;
    }

    /**
     * True when this zombie is close enough to hear the current noise.
     */
    public static boolean shouldInvestigate(int zombieCenterX, int zombieCenterY) {
        if (!isActive()) {
            return false;
        }

        int dx = noiseX - zombieCenterX;
        int dy = noiseY - zombieCenterY;

        return dx * dx + dy * dy <= GUNSHOT_RADIUS * GUNSHOT_RADIUS;
    }

    public static int getNoiseX() {
        return noiseX;
    }

    public static int getNoiseY() {
        return noiseY;
    }

    public static int getNoiseRadius() {
        return GUNSHOT_RADIUS;
    }

    public static long getRemainingTime() {
        long remaining = NOISE_DURATION
                - (System.currentTimeMillis() - noiseTime);

        return Math.max(0L, remaining);
    }
}
