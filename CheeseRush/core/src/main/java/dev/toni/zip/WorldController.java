package dev.toni.zip;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

public class WorldController {
    public Player player;
    public Cat cat;
    public Texture bgDay, bgAfternoon, bgNight;
    public Texture currentBg;
    public boolean gameOver = false;
    public float score = 0f;
    public int difficulty = 0;
    private float catStartDelay = 0f;

    public WorldController(AssetManager assets) {
        player = new Player();
        cat = new Cat();
        player.bounds.setSize(1.1f, 1.1f);
        player.bounds.setPosition(player.pos.x, Constants.GROUND_Y);
        cat.bounds.setSize(1.2f, 1.2f);
        cat.bounds.setPosition(cat.pos.x, Constants.GROUND_Y);
        bgDay = new Texture("Cenario_Dia.jpg");
        bgAfternoon = new Texture("Cenario_Tarde.jpg");
        bgNight = new Texture("Cenario_Noite.jpg");
        currentBg = bgDay;
        Array<TextureRegion> ratFrames = new Array<>();
        for (int i = 1; i <= 4; i++) ratFrames.add(new TextureRegion(new Texture("Rato" + i + ".png")));
        player.anim = new Animation<>(0.1f, ratFrames, Animation.PlayMode.LOOP);
        Array<TextureRegion> catFrames = new Array<>();
        for (int i = 1; i <= 6; i++) catFrames.add(new TextureRegion(new Texture("Gato" + i + ".png")));
        cat.anim = new Animation<>(0.1f, catFrames, Animation.PlayMode.LOOP);
        player.pos.set(6f, Constants.GROUND_Y);
        cat.pos.set(0.5f, Constants.GROUND_Y);
        player.bounds.setPosition(player.pos.x, Constants.GROUND_Y);
        cat.bounds.setPosition(cat.pos.x, Constants.GROUND_Y);
        catStartDelay = 3f;
    }

    public void update(float dt) {
        if (!gameOver) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) player.accelerate();
            player.update(dt);
            if (catStartDelay > 0f) catStartDelay -= dt; else cat.update(dt);
            player.bounds.setPosition(player.pos.x, Constants.GROUND_Y);
            cat.bounds.setPosition(cat.pos.x, Constants.GROUND_Y);
            score += player.vel.x * dt * 10f;
            if (score < 200f) {
                difficulty = 0;
                currentBg = bgDay;
            } else if (score < 600f) {
                difficulty = 1;
                currentBg = bgAfternoon;
            } else {
                difficulty = 2;
                currentBg = bgNight;
            }
            if (player.bounds.overlaps(cat.bounds)) gameOver = true;
        } else {
            if (Gdx.input.justTouched()) reset();
        }
    }

    public void reset() {
        player.reset();
        cat.reset();
        player.pos.set(6f, Constants.GROUND_Y);
        cat.pos.set(0.5f, Constants.GROUND_Y);
        player.bounds.setPosition(player.pos.x, Constants.GROUND_Y);
        cat.bounds.setPosition(cat.pos.x, Constants.GROUND_Y);
        catStartDelay = 3f;
        score = 0f;
        difficulty = 0;
        currentBg = bgDay;
        gameOver = false;
    }
}
