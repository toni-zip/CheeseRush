package dev.toni.zip;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
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

    public TextureRegion pomboTexture;
    public Animation<TextureRegion> pomboAnim;
    public Animation<TextureRegion> dogAnim;

    public Array<Rectangle> cheeses;
    public Array<Rectangle> trashes;
    public Array<Pigeon> pombos;
    public Array<Dog> dogs;

    public boolean gameOver = false;
    // Vidas do jogador: colisões permitidas antes do game over
    public final int MAX_LIVES = 5;
    public int lives = MAX_LIVES;
    // Texturas de HP (hp1..hp6)
    public Texture[] hpTextures = new Texture[6];
    public float score = 0f;
    public int difficulty = 0;
    public int cheeseCount = 0;

    private boolean isOnGround = true;
    private boolean gameStarted = false;
    private Random random = new Random();

    public boolean fading = false;
    public float fadeTime = 0f;
    private float fadeDuration = 2f;
    public float getFadeDuration() { return fadeDuration; }

    // pausa controlada pelo renderer/menu
    public boolean paused = false;
    private boolean wentAfternoon = false;
    private boolean wentNight = false;

    // Atraso para início do gato
    private boolean catStartPending = false;
    private float catStartTimer = 0f;
    private final float CAT_START_DELAY = 3f; // segundos
    public float bgScroll = 0f;

    private float minCheeseDistance = 8f;
    private float maxCheeseDistance = 15f;
    private float cheeseSpawnAhead = 25f;

    // LIXEIRA (só dia/tarde)
    private float minTrashDistance = 18f;
    private float maxTrashDistance = 40f;
    private float trashSpawnAhead = 20f;
    private float trashSpawnChance = 1f;

    // POMBOS (apenas tarde/noite)
    private float minPomboDistance = 12f;
    private float maxPomboDistance = 25f;
    private float pomboSpawnAhead = 18f;
    private float pomboSpawnChance = 1.2f;

    // CACHORROS 
    private float minDogDistance = 20f;
    private float maxDogDistance = 40f;
    private float dogSpawnAhead = 20f;
    private float dogSpawnChance = 0.25f; 

    private boolean catJumping = false;
    private float catJumpVelocity = 0f;
    private float catGravity = -50f;

    private Music music;

    public boolean isGameStarted() { return gameStarted; }

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

    
        for (int i = 1; i <= 6; i++) {
            hpTextures[i - 1] = new Texture("hp" + i + ".png");
        }

        // POMBO
        Array<TextureRegion> pomboFrames = new Array<>();
        for (int i = 1; i <= 6; i++)
            pomboFrames.add(new TextureRegion(new Texture("pombo" + i + ".png")));
        pomboAnim = new Animation<>(0.1f, pomboFrames, Animation.PlayMode.LOOP);

        // Cachorro 
        Array<TextureRegion> dogFrames = new Array<>();
        for (int i = 1; i <= 6; i++)
            dogFrames.add(new TextureRegion(new Texture("dog" + i + ".png")));
        dogAnim = new Animation<>(0.12f, dogFrames, Animation.PlayMode.LOOP);

        music = Gdx.audio.newMusic(Gdx.files.internal("music.mp3"));
        music.setLooping(true);
        music.setVolume(0.05f);
        music.play();

        cheeses = new Array<>();
        trashes = new Array<>();
    pombos = new Array<>();
    dogs = new Array<>();

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


    public void startGame() {
        if (!gameStarted) {
            gameStarted = true;
            catStartPending = true;
            catStartTimer = 0f;
        }
    }

    public void pauseGame() { paused = true; }
    public void resumeGame() { paused = false; }

    public void update(float dt) {
        if (paused) return;
        if (!gameOver) {
            if (!gameStarted) {
                if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                    gameStarted = true;
                    // inicia contagem regressiva do gato em vez de ativá-lo imediatamente
                    catStartPending = true;
                    catStartTimer = 0f;
                }
                return;
            }

            handleInput(dt);
            player.update(dt);
            if (catStartPending) {
                catStartTimer += dt;
                if (catStartTimer >= CAT_START_DELAY) {
                    catStartPending = false;
                    cat.activate();
                }
            }
            updateCat(dt);
            updateBounds();
            updateCheeses(dt);
            updateTrashes(dt);
            updatePombos(dt);
            updateDogs(dt);

            // atualiza deslocamento contínuo do fundo com base na velocidade do jogador
            bgScroll += player.vel.x * dt * 0.5f;

            score += player.vel.x * dt * 10f;

            if (player.bounds.overlaps(cat.bounds)) {
                handlePlayerCollision();
            }

            for (int i = trashes.size - 1; i >= 0; i--) {
                Rectangle t = trashes.get(i);
                if (player.bounds.overlaps(t)) {
                    trashes.removeIndex(i);
                    handlePlayerCollision();
                }
            }

            for (int i = pombos.size - 1; i >= 0; i--) {
                Pigeon p = pombos.get(i);
                if (player.bounds.overlaps(p.bounds)) {
                    pombos.removeIndex(i);
                    handlePlayerCollision();
                }
            }

            updateBackgroundTransition(dt);

        } else {
            if (Gdx.input.justTouched() || Gdx.input.isKeyJustPressed(Input.Keys.SPACE))
                reset();
        }
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

            if (!wentAfternoon && cheeseCount >= 20 && currentBg != bgAfternoon) {
                nextBg = bgAfternoon;
                startFade(0.2f);
                wentAfternoon = true;
                return;
            }

            if (!wentNight && cheeseCount >= 40 && currentBg != bgNight) {
                nextBg = bgNight;
                startFade(0.3f);
                wentNight = true;
            }
        }
    }

    // Chamado quando o jogador colide com um obstáculo ou com o gato.
    // Decrementa vidas e finaliza o jogo quando anuladas. Se ainda houver vidas,
    // aplica efeito de dano (sem parar o movimento) e mantém o jogador em andamento.
    private void handlePlayerCollision() {
        lives--;
        if (lives <= 0) {
            gameOver = true;
            return;
        }

        // Mantém o jogador em movimento
        player.takeDamage();
        if (player.vel.x < 2.5f) player.vel.x = 2.5f; 
        player.pos.x += 0.5f;
        updateBounds();
    }

    private void startFade(float extraSpeed) {
        fading = true;
        fadeTime = 0f;
    }

    private void handleInput(float dt) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE))
            player.accelerate();

        if (Gdx.input.isKeyJustPressed(Input.Keys.W)) {
            player.jump();
            isOnGround = false;
        }

        if (player.pos.y <= Constants.GROUND_Y)
            isOnGround = true;
    }

    private void updateCat(float dt) {
        if (!cat.isActive()) return;

        Rectangle nearestTrash = null;
        Dog nearestDog = null;
        for (Rectangle t : trashes) {
            if (t.x > cat.pos.x && t.x - cat.pos.x < 2.0f) {
                nearestTrash = t;
                break;
            }
        }

        for (Dog d : dogs) {
            if (d.pos.x > cat.pos.x && d.pos.x - cat.pos.x < 2.0f) {
                nearestDog = d;
                break;
            }
        }

        if ((nearestTrash != null || nearestDog != null) && cat.pos.y <= Constants.GROUND_Y + 0.01f && !catJumping) {
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

    private void updateCheeses(float dt) {
        for (Rectangle c : cheeses)
            c.x -= player.vel.x * dt * 0.8f;

        for (int i = 0; i < cheeses.size; i++) {
            if (player.bounds.overlaps(cheeses.get(i))) {
                cheeses.removeIndex(i);
                cheeseCount++;
                break;
            }
        }

        float farthest = -999f;
        for (Rectangle c : cheeses)
            if (c.x > farthest) farthest = c.x;

        float spawnThreshold = player.pos.x + cheeseSpawnAhead;
        if (farthest < spawnThreshold)
            spawnNewCheese(farthest);

        float removeThreshold = player.pos.x - 15f;
        for (int i = cheeses.size - 1; i >= 0; i--)
            if (cheeses.get(i).x < removeThreshold)
                cheeses.removeIndex(i);
    }

    private void updateTrashes(float dt) {

       
        for (Rectangle t : trashes)
            t.x -= player.vel.x * dt * 0.8f;

        float farthest = -999f;
        for (Rectangle t : trashes)
            if (t.x > farthest) farthest = t.x;

        float spawnThreshold = player.pos.x + trashSpawnAhead;
        if (farthest < spawnThreshold && currentBg != bgNight && random.nextFloat() < trashSpawnChance)
            spawnNewTrash(farthest);

        float removeThreshold = player.pos.x - 20f;
        for (int i = trashes.size - 1; i >= 0; i--)
            if (trashes.get(i).x < removeThreshold)
                trashes.removeIndex(i);
    }

    private void updatePombos(float dt) {

        
        for (Pigeon p : pombos)
            p.update(dt);

        float farthest = -999f;
        for (Pigeon p : pombos)
            if (p.pos.x > farthest) farthest = p.pos.x;

        float spawnThreshold = player.pos.x + pomboSpawnAhead;
        if (farthest < spawnThreshold && currentBg != bgDay && random.nextFloat() < pomboSpawnChance)
            spawnNewPombo(farthest);

        float removeThreshold = player.pos.x - 20f;
        for (int i = pombos.size - 1; i >= 0; i--)
            if (pombos.get(i).pos.x < removeThreshold)
                pombos.removeIndex(i);
    }

    private void updateDogs(float dt) {
    
        for (Dog d : dogs)
            d.update(dt);

        float farthest = -999f;
        for (Dog d : dogs)
            if (d.pos.x > farthest) farthest = d.pos.x;

        float spawnThreshold = player.pos.x + dogSpawnAhead;
        if (farthest < spawnThreshold && currentBg == bgNight && random.nextFloat() < dogSpawnChance)
            spawnNewDog(farthest);

        float removeThreshold = player.pos.x - 20f;
        for (int i = dogs.size - 1; i >= 0; i--)
            if (dogs.get(i).pos.x < removeThreshold)
                dogs.removeIndex(i);

        
        for (int i = dogs.size - 1; i >= 0; i--) {
            Dog d = dogs.get(i);
            if (player.bounds.overlaps(d.bounds)) {
                dogs.removeIndex(i);
                handlePlayerCollision();
            }
        }
    }

    private void spawnNewDog(float lastX) {
        float spacing = minDogDistance + random.nextFloat() * (maxDogDistance - minDogDistance);
        float newX = Math.max(lastX + spacing, player.pos.x + 18f);
        Dog d = new Dog(newX, Constants.GROUND_Y);
        d.activate();
        dogs.add(d);
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

    private void spawnNewPombo(float lastX) {
        float spacing = minPomboDistance + random.nextFloat() * (maxPomboDistance - minPomboDistance);
        float newX = Math.max(lastX + spacing, player.pos.x + 18f);
        float y = Constants.GROUND_Y + 0.9f + random.nextFloat() * 4f;

    Pigeon p = new Pigeon(newX, y);
        p.activate();
    pombos.add(p);
    }

    private void updateBounds() {
        player.bounds.setPosition(player.pos.x, player.pos.y);
        cat.bounds.setPosition(cat.pos.x, cat.pos.y);
    }

    public void reset() {
        player.reset();
        cat.reset();

        gameStarted = false;
        cheeseCount = 0;
        difficulty = 0;

        player.pos.set(6f, Constants.GROUND_Y);
        cat.pos.set(3.5f, Constants.GROUND_Y);

        updateBounds();

        cheeses.clear();
        trashes.clear();
        pombos.clear();

        float startX = 10f;
        for (int i = 0; i < 4; i++) {
            float spacing = minCheeseDistance + random.nextFloat() * (maxCheeseDistance - minCheeseDistance);
            Rectangle c = new Rectangle(startX + spacing * i, Constants.GROUND_Y + 1.0f + random.nextFloat() * 0.8f, 1f, 1f);
            cheeses.add(c);
        }

        trashes.add(new Rectangle(20f, Constants.GROUND_Y, 1.2f, 1.2f));

        currentBg = bgDay;
        nextBg = null;
        fading = false;
        fadeTime = 0f;
        wentAfternoon = false;
        wentNight = false;
        catStartPending = false;
        catStartTimer = 0f;
        paused = false;
        gameOver = false;
        lives = MAX_LIVES;
        music.stop();
        music.setVolume(0.05f);
        music.play();
    }

    public void resetGame() {
        reset();
    }
}
