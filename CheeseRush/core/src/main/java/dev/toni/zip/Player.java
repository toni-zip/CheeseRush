package dev.toni.zip;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Player {
    public Vector2 pos = new Vector2(2, Constants.GROUND_Y);
    public Vector2 vel = new Vector2(0, 0);
    public Rectangle bounds = new Rectangle(pos.x, pos.y, 1, 1);
    public Animation<TextureRegion> anim;
    public float stateTime = 0;

    public void update(float dt) {
        pos.x += vel.x * dt;
        vel.x = Math.max(vel.x - Constants.PLAYER_DECAY * dt, 0);
        bounds.setPosition(pos.x, pos.y);
        stateTime += dt;
    }

    public void accelerate() {
        vel.x = Math.min(vel.x + Constants.PLAYER_ACCEL * 0.5f, Constants.PLAYER_MAX_SPEED);
    }

    public TextureRegion getFrame() {
        return anim.getKeyFrame(stateTime, true);
    }

    public void reset() {
        pos.set(2, Constants.GROUND_Y);
        vel.set(0, 0);
        stateTime = 0;
    }
}
