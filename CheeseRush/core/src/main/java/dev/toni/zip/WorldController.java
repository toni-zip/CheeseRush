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
    public Texture nextBg;
    public Texture cheeseTexture;
    public Texture trashTexture;

    public Array<Rectangle> cheeses;
    public Array<Rectangle> trashes;

    public boolean gameOver = false;
    public float score = 0f;
    public int difficulty = 0;
    public int cheeseCount = 0;

    private boolean isOnGround = true;
    private boolean gameStarted = false;
    private Random random = new Random();

    // fade control
    public boolean fading = false;
    public float fadeTime = 0f;
    private float fadeDuration = 2f; // agora privado
    public float getFadeDuration() { return fadeDuration; } // ✅ getter público

    // Cheeses config
    private float minCheeseDistance = 8f;
    private float maxCheeseDistance = 15f;
    private float cheeseSpawnAhead = 25f;

    // Trash config
    private float minTrashDistance = 18f;
    private float maxTrashDistance = 40f;
    private float trashSpawnAhead = 20f;
    private float trashSpawnChance = 1f;

    // Controle de pulo do gato
    private boolean catJumping = false;
    private float catJumpVelocity = 0f;
    private float catGravity = -50f;

    public boolean isGameStarted() { return gameStarted; } // ✅ usado no menu

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
        trashTexture = new Texture("lixeira.png");

        cheeses = new Array<>();
        trashes = new Array<>();

        float startX = 10f;
        for (int i = 0; i < 4; i++) {
            float spacing = minCheeseDistance + random.nextFloat() * (maxCheeseDistance - minCheeseDistance);
            Rectangle c = new Rectangle(startX + spacing * i, Constants.GROUND_Y + 1.0f + random.nextFloat() * 0.8f, 1f, 1f);
            cheeses.add(c);
        }

        trashes.add(new Rectangle(20f, Constants.GROUND_Y, 1.2f, 1.2f));

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
            if (!gameStarted) {
                // Espera o jogador apertar espaço para começar
                if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                    gameStarted = true;
                    cat.activate();
                }
                return;
            }

            handleInput(dt);
            player.update(dt);
            updateCat(dt);
            updateBounds();
            updateCheeses(dt);
            updateTrashes(dt);

            score += player.vel.x * dt * 10f;

            if (player.bounds.overlaps(cat.bounds)) gameOver = true;

            for (Rectangle t : trashes) {
                if (player.bounds.overlaps(t)) {
                    gameOver = true;
                    break;
                }
            }

            updateBackgroundTransition(dt);

        } else {
            // Reiniciar o jogo quando clicar ou apertar espaço
            if (Gdx.input.justTouched() || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                reset();
            }
        }
    }

    private void handleInput(float dt) {
        if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
            player.accelerate();
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.W)) {
            player.jump();
            isOnGround = false;
        }

        if (player.pos.y <= Constants.GROUND_Y) {
            isOnGround = true;
        }
    }

    private void updateCat(float dt) {
        if (!cat.isActive()) return;

        Rectangle nearestTrash = null;
        for (Rectangle t : trashes) {
            if (t.x > cat.pos.x && t.x - cat.pos.x < 2.0f) {
                nearestTrash = t;
                break;
            }
        }

        if (nearestTrash != null && cat.pos.y <= Constants.GROUND_Y + 0.01f && !catJumping) {
            catJumping = true;
            catJumpVelocity = 18f;
        }

        if (catJumping) {
            cat.pos.y += catJumpVelocity * dt;
            catJumpVelocity += catGravity * dt;
            if (cat.pos.y <= Constants.GROUND_Y) {
                cat.pos.y = Constants.GROUND_Y;
                catJumping = false;
                catJumpVelocity = 0f;
            }
        }

        cat.update(dt, player.pos.x);
    }

    private void updateBackgroundTransition(float dt) {
        if (fading) {
            fadeTime += dt;
            if (fadeTime >= fadeDuration) {
                fading = false;
                currentBg = nextBg;
                nextBg = null;
            }
        }

        if (!fading) {
            if (cheeseCount >= 10 && currentBg != bgNight) {
                nextBg = bgNight;
                startFade(0.7f);
            } else if (cheeseCount >= 5 && currentBg != bgAfternoon) {
                nextBg = bgAfternoon;
                startFade(0.4f);
            }
        }
    }

    private void startFade(float extraSpeed) {
        fading = true;
        fadeTime = 0f;
        cat.increaseSpeedOverTime(extraSpeed);
    }

    private void updateCheeses(float dt) {
        for (Rectangle c : cheeses) c.x -= player.vel.x * dt * 0.8f;

        for (int i = 0; i < cheeses.size; i++) {
            Rectangle c = cheeses.get(i);
            if (player.bounds.overlaps(c)) {
                cheeses.removeIndex(i);
                cheeseCount++;
                break;
            }
        }

        float farthestCheeseX = -999f;
        for (Rectangle c : cheeses)
            if (c.x > farthestCheeseX) farthestCheeseX = c.x;

        float spawnThreshold = player.pos.x + cheeseSpawnAhead;
        if (farthestCheeseX < spawnThreshold)
            spawnNewCheese(farthestCheeseX);

        float removeThreshold = player.pos.x - 15f;
        for (int i = cheeses.size - 1; i >= 0; i--)
            if (cheeses.get(i).x < removeThreshold)
                cheeses.removeIndex(i);
    }

    private void updateTrashes(float dt) {
        for (Rectangle t : trashes) t.x -= player.vel.x * dt * 0.8f;

        float farthestTrashX = -999f;
        for (Rectangle t : trashes)
            if (t.x > farthestTrashX) farthestTrashX = t.x;

        float spawnThreshold = player.pos.x + trashSpawnAhead;
        if (farthestTrashX < spawnThreshold && random.nextFloat() < trashSpawnChance) {
            spawnNewTrash(farthestTrashX);
        }

        float removeThreshold = player.pos.x - 20f;
        for (int i = trashes.size - 1; i >= 0; i--)
            if (trashes.get(i).x < removeThreshold)
                trashes.removeIndex(i);
    }

    private void spawnNewCheese(float lastX) {
        float spacing = minCheeseDistance + random.nextFloat() * (maxCheeseDistance - minCheeseDistance);
        float newX = Math.max(lastX + spacing, player.pos.x + 15f);
        Rectangle newCheese = new Rectangle(newX, Constants.GROUND_Y + 0.9f + random.nextFloat() * 0.8f, 1f, 1f);
        cheeses.add(newCheese);
    }

    private void spawnNewTrash(float lastX) {
        float spacing = minTrashDistance + random.nextFloat() * (maxTrashDistance - minTrashDistance);
        float newX = Math.max(lastX + spacing, player.pos.x + 20f);
        Rectangle newTrash = new Rectangle(newX, Constants.GROUND_Y, 1.2f, 1.2f);
        trashes.add(newTrash);
    }

    private void updateBounds() {
        player.bounds.setPosition(player.pos.x, player.pos.y);
        cat.bounds.setPosition(cat.pos.x, cat.pos.y);
    }

    public void reset() {
        player.reset();
        cat.reset();
        gameStarted = false;

        player.pos.set(6f, Constants.GROUND_Y);
        cat.pos.set(3.5f, Constants.GROUND_Y);
        updateBounds();

        cheeses.clear();
        trashes.clear();

        float startX = 10f;
        for (int i = 0; i < 4; i++) {
            float spacing = minCheeseDistance + random.nextFloat() * (maxCheeseDistance - minCheeseDistance);
            Rectangle c = new Rectangle(startX + spacing * i, Constants.GROUND_Y + 1.0f + random.nextFloat() * 0.8f, 1f, 1f);
            cheeses.add(c);
        }

        trashes.add(new Rectangle(20f, Constants.GROUND_Y, 1.2f, 1.2f));

        cheeseCount = 0;
        difficulty = 0;
        currentBg = bgDay;
        fading = false;
        nextBg = null;
        fadeTime = 0f;
        gameOver = false;
    }

    // ✅ método adicional pedido
    public void resetGame() {
        cheeseCount = 0;
        player.reset();
        cat.reset();
        currentBg = bgDay;
        nextBg = null;
        fading = false;
    }
}
