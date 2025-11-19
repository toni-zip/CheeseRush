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

    private float baseSpeed = 5.5f;   
    private float maxSpeed = 6.8f;    
    private float closeFactor = 0.9f;
    private float farFactor = 1.1f;

    private boolean active = false;

    public Cat() {
        this.bounds = new Rectangle(this.pos.x, this.pos.y, 1.0F, 1.0F);
        this.stateTime = 0.0F;
    }

    public void update(float dt, float playerX) {
        if (!active) return;

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
        baseSpeed = 5.5f;
        active = false;
    }

    public void increaseSpeedOverTime(float delta) {
        baseSpeed += delta;
        if (baseSpeed > maxSpeed) baseSpeed = maxSpeed;
        if (baseSpeed < 0f) baseSpeed = 0f;
    }

    public void activate() {
        active = true;
    }

    public boolean isActive() {
        return active;
    }

    public float getBaseSpeed() {
        return baseSpeed;
    }
}
