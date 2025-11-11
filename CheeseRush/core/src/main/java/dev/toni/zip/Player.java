package dev.toni.zip;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class Player {
    public Vector2 pos = new Vector2();
    public Vector2 vel = new Vector2();
    public Rectangle bounds = new Rectangle();
    public Animation<TextureRegion> anim;
    private float stateTime = 0f;
    private float gravity = -25f;
    private float jumpForce = 10f;

    private int jumpCount = 0;        // 🔹 controla pulos
    private final int maxJumps = 2;   // 🔹 permite 2 pulos (duplo pulo)

    public Player() {
        reset();
        Array<TextureRegion> ratFrames = new Array<>();
        for (int i = 1; i <= 4; i++) {
            ratFrames.add(new TextureRegion(new Texture("Rato" + i + ".png")));
        }
        anim = new Animation<>(0.1f, ratFrames, Animation.PlayMode.LOOP);
    }

    public void update(float dt) {
        stateTime += dt;

        // aplica gravidade
        vel.y += gravity * dt;
        pos.add(vel.x * dt, vel.y * dt);

        // limitar o chão
        if (pos.y < Constants.GROUND_Y) {
            pos.y = Constants.GROUND_Y;
            vel.y = 0;
            jumpCount = 0; // 🔹 reseta contagem de pulos ao tocar o chão
        }

        // desaceleração suave
        if (vel.x > 0f) {
            vel.x -= Constants.PLAYER_DECAY * dt;
            if (vel.x < 0f) vel.x = 0f;
        }

        bounds.set(pos.x, pos.y, 1.1f, 1.1f);
    }

    public TextureRegion getFrame() {
        return anim.getKeyFrame(stateTime);
    }

    public void accelerate() {
        vel.x += 1.0f; // 🔹 leve redução na aceleração
        if (vel.x > Constants.PLAYER_MAX_SPEED)
            vel.x = Constants.PLAYER_MAX_SPEED;
    }

    public void jump() {
        if (jumpCount < maxJumps) { // 🔹 permite pular novamente no ar
            vel.y = jumpForce;
            if (vel.x < 2.5f) vel.x += 0.8f;
            jumpCount++;
        }
    }

    public void reset() {
        pos.set(6f, Constants.GROUND_Y);
        vel.set(0f, 0f);
        bounds.set(pos.x, pos.y, 1.1f, 1.1f);
        jumpCount = 0;
    }
}
