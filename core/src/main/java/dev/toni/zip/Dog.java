package dev.toni.zip;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Dog {

    public Vector2 pos = new Vector2();
    public Rectangle bounds;
    public float stateTime = 0f;

    private float baseSpeed = 5.0f;
    private float maxSpeed = 7.0f;

    private boolean active = false;

    public Dog(float x, float y) {
        this.pos.set(x, Constants.GROUND_Y);
        this.bounds = new Rectangle(x, Constants.GROUND_Y, 1.8f, 1.2f);
    }

    public void update(float dt) {
        if (!active) return;
        pos.x -= baseSpeed * dt;
        bounds.setPosition(pos.x, pos.y);
        stateTime += dt;
    }

    public void reset(float x, float y) {
        pos.set(x, Constants.GROUND_Y);
        bounds.setPosition(x, Constants.GROUND_Y);
        stateTime = 0f;
        baseSpeed = 5.0f;
        active = false;
    }

    public void activate() { active = true; }

    public boolean isActive() { return active; }

    public void increaseSpeedOverTime(float delta) {
        baseSpeed += delta;
        if (baseSpeed > maxSpeed) baseSpeed = maxSpeed;
        if (baseSpeed < 0f) baseSpeed = 0f;
    }

}
