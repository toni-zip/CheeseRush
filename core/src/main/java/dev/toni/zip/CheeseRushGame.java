package dev.toni.zip;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class CheeseRushGame extends Game {
    public SpriteBatch batch;
    public AssetManager assets;

    @Override
    public void create() {
        batch = new SpriteBatch();
        assets = new AssetManager();
        setScreen(new GameScreen(this));
    }

    @Override
    public void dispose() {
        if (getScreen() != null) getScreen().dispose();
        batch.dispose();
        assets.dispose();
    }
}
