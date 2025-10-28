package dev.toni.zip;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Cat {
    public Vector2 pos = new Vector2(0.5f, Constants.GROUND_Y);
    public Rectangle bounds = new Rectangle(pos.x, pos.y, 1, 1);
    public Animation<TextureRegion> anim;
    public float stateTime = 0;

    public void update(float dt) {
        pos.x += Constants.CAT_SPEED * dt;
        bounds.setPosition(pos.x, pos.y);
        stateTime += dt;
    }

    public TextureRegion getFrame() {
        return anim.getKeyFrame(stateTime, true);
    }

    public void reset() {
        pos.set(0.5f, Constants.GROUND_Y);
        stateTime = 0;
    }
}
