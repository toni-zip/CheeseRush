package dev.toni.zip;

public final class Constants {
    private Constants() {}

    public static final float WORLD_WIDTH = 16f;
    public static final float WORLD_HEIGHT = 12f;
    public static final float GRAVITY = -40f;

    // Movimento do player
    public static final float PLAYER_ACCEL = 12f;
    public static final float PLAYER_MAX_SPEED = 6.5f;
    public static final float PLAYER_DECAY = 6f;

    public static final float GROUND_Y = 0.2f;

    // Velocidade base do gato (reduzida para equilibrar perseguição)
    public static final float CAT_SPEED = 3.5f;
}
