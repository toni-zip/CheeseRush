package dev.toni.zip;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class WorldRenderer {
    private final SpriteBatch batch;
    private final WorldController ctrl;
    private final OrthographicCamera cam;

    public WorldRenderer(SpriteBatch batch, WorldController ctrl, OrthographicCamera cam) {
        this.batch = batch;
        this.ctrl = ctrl;
        this.cam = cam;
    }

    public void render() {
        cam.position.set(ctrl.player.pos.x + 5f, cam.viewportHeight / 2f, 0f);
        cam.update();
        batch.setProjectionMatrix(cam.combined);
        batch.begin();
        renderParallax(ctrl.bgDay, ctrl.player.pos.x * 0.3f);
        renderParallax(ctrl.bgDay, ctrl.player.pos.x * 0.6f);
        renderParallax(ctrl.bgDay, ctrl.player.pos.x * 0.9f);
        batch.draw(ctrl.player.getFrame(), ctrl.player.pos.x, Constants.GROUND_Y, 1.2f, 1.2f);
        batch.draw(ctrl.cat.getFrame(), ctrl.cat.pos.x, Constants.GROUND_Y, 1.3f, 1.3f);
        batch.end();
    }

    private void renderParallax(Texture tex, float offset) {
        float w = cam.viewportWidth;
        float h = Constants.WORLD_HEIGHT;
        float x0 = cam.position.x - w / 2f - (offset % w);
        batch.draw(tex, x0, 0, w, h);
        batch.draw(tex, x0 + w, 0, w, h);
    }

    public void dispose() {}
}
