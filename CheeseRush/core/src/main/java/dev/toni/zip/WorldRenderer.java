package dev.toni.zip;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public class WorldRenderer {
    private final SpriteBatch batch;
    private final WorldController ctrl;
    private final OrthographicCamera cam;
    private final OrthographicCamera hudCam;
    private final BitmapFont font;

    public WorldRenderer(SpriteBatch batch, WorldController controller, OrthographicCamera cam) {
        this.batch = batch;
        this.ctrl = controller;
        this.cam = cam;

        hudCam = new OrthographicCamera();
        hudCam.setToOrtho(false, Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT);

        font = new BitmapFont();
        font.getData().setScale(0.05f);
    }

    public void render() {
        cam.position.set(ctrl.player.pos.x + 5f, cam.viewportHeight / 2f, 0f);
        cam.update();

        batch.setProjectionMatrix(cam.combined);
        batch.begin();

        renderParallax(ctrl.currentBg, ctrl.player.pos.x * 0.3f);
        renderParallax(ctrl.currentBg, ctrl.player.pos.x * 0.6f);
        renderParallax(ctrl.currentBg, ctrl.player.pos.x * 0.9f);

        // Queijos
        if (ctrl.cheeses != null && ctrl.cheeseTexture != null) {
            for (Rectangle cheese : ctrl.cheeses) {
                batch.draw(ctrl.cheeseTexture, cheese.x, cheese.y, cheese.width, cheese.height);
            }
        }

        // 🔹 Lixeiras
        if (ctrl.trashes != null && ctrl.trashTexture != null) {
            for (Rectangle trash : ctrl.trashes) {
                batch.draw(ctrl.trashTexture, trash.x, trash.y, trash.width, trash.height);
            }
        }

        // Player
        batch.draw(ctrl.player.getFrame(), ctrl.player.pos.x, ctrl.player.pos.y, 1.2f, 1.2f);

        // Gato
        batch.draw(ctrl.cat.getFrame(), ctrl.cat.pos.x, ctrl.cat.pos.y, 1.3f, 1.3f);

        batch.end();

        // HUD
        hudCam.update();
        batch.setProjectionMatrix(hudCam.combined);
        batch.begin();

        float padding = 0.3f;
        float yTop = hudCam.viewportHeight - 0.3f;

        font.draw(batch, "🧀 " + ctrl.cheeseCount, hudCam.viewportWidth - 5f - padding, yTop);

        batch.end();
    }

    private void renderParallax(Texture tex, float offset) {
        if (tex == null) return;
        float w = cam.viewportWidth;
        float h = Constants.WORLD_HEIGHT;
        float x0 = cam.position.x - w / 2f - (offset % w);
        batch.draw(tex, x0, 0, w, h);
        batch.draw(tex, x0 + w, 0, w, h);
    }

    public void resize(int width, int height) {
        cam.setToOrtho(false, Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT);
        hudCam.setToOrtho(false, Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT);
    }

    public void dispose() {
        font.dispose();
    }
}
