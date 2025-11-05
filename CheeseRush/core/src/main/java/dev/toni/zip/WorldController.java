package dev.toni.zip;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import java.util.Random;

public class WorldController {
    public Player player;
    public Cat cat;
    public Texture bgDay, bgAfternoon, bgNight;
    public Texture currentBg;
    public Texture cheeseTexture;
    public Array<Rectangle> cheeses;

    public boolean gameOver = false;
    public float score = 0f;
    public int difficulty = 0;
    public int cheeseCount = 0; // 🧀 contador de queijos coletados

    private float gravity = -25f;
    private float jumpVelocity = 8f;
    private boolean isOnGround = true;
    private Random random = new Random();

    public WorldController(AssetManager assets) {
        player = new Player();
        cat = new Cat();

        player.bounds.setSize(1.1f, 1.1f);
        cat.bounds.setSize(1.2f, 1.2f);

        bgDay = new Texture("Cenario_Dia.jpg");
        bgAfternoon = new Texture("Cenario_Tarde.jpg");
        bgNight = new Texture("Cenario_Noite.jpg");
        currentBg = bgDay;

        cheeseTexture = new Texture("cheese.png");
        cheeses = new Array<>();

        // 🧀 menos queijos iniciais
        for (int i = 0; i < 3; i++) {
            Rectangle c = new Rectangle(10f + i * 6f, Constants.GROUND_Y + 1.0f + random.nextFloat() * 0.8f, 1f, 1f);
            cheeses.add(c);
        }

        Array<TextureRegion> catFrames = new Array<>();
        for (int i = 1; i <= 6; i++)
            catFrames.add(new TextureRegion(new Texture("Gato" + i + ".png")));
        cat.anim = new Animation<>(0.1f, catFrames, Animation.PlayMode.LOOP);

        player.pos.set(6f, Constants.GROUND_Y);
        cat.pos.set(0.5f, Constants.GROUND_Y);

        updateBounds();
    }

    public void update(float dt) {
        if (!gameOver) {
            handleInput(dt);
            player.update(dt);
            cat.update(dt, player.pos.x);

            updateBounds();
            updateCheeses(dt);

            score += player.vel.x * dt * 10f;

            if (player.bounds.overlaps(cat.bounds)) gameOver = true;
        } else {
            if (Gdx.input.justTouched()) reset();
        }
    }

    private void handleInput(float dt) {
        // espaço = pedalar
        if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
            player.accelerate();
        }

        // W = pular
        if (Gdx.input.isKeyJustPressed(Input.Keys.W) && isOnGround) {
            player.vel.y = jumpVelocity;
            isOnGround = false;
        }

        // checa se está no chão
        if (player.pos.y <= Constants.GROUND_Y) {
            isOnGround = true;
        }
    }

    private void updateCheeses(float dt) {
        for (Rectangle c : cheeses) c.x -= player.vel.x * dt * 0.8f;

        for (int i = 0; i < cheeses.size; i++) {
            Rectangle c = cheeses.get(i);
            if (player.bounds.overlaps(c)) {
                cheeses.removeIndex(i);
                cheeseCount++; // 🧀 incrementa contador
                score += 50f;

                // Novo queijo — posição aleatória e mais baixa
                Rectangle newCheese = new Rectangle();
                newCheese.setWidth(1f);
                newCheese.setHeight(1f);
                newCheese.setX(player.pos.x + 20f + random.nextFloat() * 10f);
                newCheese.setY(Constants.GROUND_Y + 0.9f + random.nextFloat() * 0.8f); // mais baixo
                cheeses.add(newCheese);
                break;
            }
        }
    }

    private void updateBounds() {
        player.bounds.setPosition(player.pos.x, player.pos.y);
        cat.bounds.setPosition(cat.pos.x, cat.pos.y);
    }

    public void reset() {
        player.reset();
        cat.reset();

        player.pos.set(6f, Constants.GROUND_Y);
        cat.pos.set(3.5f, Constants.GROUND_Y);
        updateBounds();

        cheeses.clear();
        for (int i = 0; i < 3; i++) {
            Rectangle c = new Rectangle(10f + i * 6f, Constants.GROUND_Y + 1.0f + random.nextFloat() * 0.8f, 1f, 1f);
            cheeses.add(c);
        }

        score = 0f;
        cheeseCount = 0; // reseta o contador
        difficulty = 0;
        currentBg = bgDay;
        gameOver = false;
    }
}
