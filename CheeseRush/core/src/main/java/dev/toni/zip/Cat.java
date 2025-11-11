package dev.toni.zip;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Cat {
    public Vector2 pos = new Vector2(1.7F, 0.2F);
    public Rectangle bounds;
    public Animation<TextureRegion> anim;
    public float stateTime;

    private float baseSpeed = 6.0f;   // 🔹 leve aumento (antes 5.5f)
    private float maxSpeed = 7.5f;    // 🔹 leve aumento (antes 7.0f)
    private float closeFactor = 0.9f;
    private float farFactor = 1.1f;

    private boolean active = false;   // 🔹 gato só começa após 1º espaço

    public Cat() {
        this.bounds = new Rectangle(this.pos.x, this.pos.y, 1.0F, 1.0F);
        this.stateTime = 0.0F;
    }

    public void update(float dt, float playerX) {
        if (!active) return; // 🔹 não se move até o jogo começar

        float distance = playerX - pos.x;
        float factor = (distance > 8f) ? farFactor : (distance < 2f ? closeFactor : 1.0f);
        float displacement = baseSpeed * factor * dt;
        pos.x += displacement;

        bounds.setPosition(pos.x, pos.y);
        stateTime += dt;
    }

    public TextureRegion getFrame() {
        return anim.getKeyFrame(stateTime, true);
    }

    public void reset() {
        pos.set(1.7F, 0.2F);
        stateTime = 0.0F;
        baseSpeed = 6.0f;
        active = false; // 🔹 reseta estado (fica parado até apertar espaço)
    }

    public void increaseSpeedOverTime(float delta) {
        baseSpeed += delta;
        if (baseSpeed > maxSpeed) baseSpeed = maxSpeed;
        if (baseSpeed < 0f) baseSpeed = 0f;
    }

    public void activate() { // 🔹 método para ativar o gato
        active = true;
    }

    public boolean isActive() {
        return active;
    }

    public float getBaseSpeed() {
        return baseSpeed;
    }
}
