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
    private boolean isJumping = false;
    private float gravity = -25f;
    private float jumpForce = 10f;

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
            isJumping = false;
        }

        // desaceleração suave (se não estiver pedalando)
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
        // impulso ao pedalar (ou pressionar espaço)
        vel.x += 1.2f;
        if (vel.x > Constants.PLAYER_MAX_SPEED)
            vel.x = Constants.PLAYER_MAX_SPEED;
    }

    public void jump() {
        if (!isJumping) {
            vel.y = jumpForce;
            // 🔹 adiciona impulso horizontal ao pular
            if (vel.x < 2.5f) vel.x += 1.0f;
            isJumping = true;
        }
    }

    public void reset() {
        pos.set(6f, Constants.GROUND_Y);
        vel.set(0f, 0f);
        bounds.set(pos.x, pos.y, 1.1f, 1.1f);
        isJumping = false;
    }
}
