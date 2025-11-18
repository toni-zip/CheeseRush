package dev.toni.zip;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class GameScreen extends ScreenAdapter {
    private final CheeseRushGame game;
    private OrthographicCamera cam;
    private FitViewport viewport;
    private WorldController controller;
    private WorldRenderer renderer;

    public GameScreen(CheeseRushGame game) {
        this.game = game;
        cam = new OrthographicCamera();
        viewport = new FitViewport(Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT, cam);
        controller = new WorldController(game.assets);
        renderer = new WorldRenderer(game.batch, controller, cam);
    }

    @Override
    public void render(float delta) {
        controller.update(delta);
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        renderer.render();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        renderer.dispose();
    }
}
