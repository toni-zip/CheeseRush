package dev.toni.zip;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public class WorldRenderer {
    private final SpriteBatch batch;
    private final WorldController ctrl;
    private final OrthographicCamera cam;
    private final OrthographicCamera hudCam;
    private final BitmapFont font;
    private final BitmapFont titleFont;

    private enum GameState { MENU, PLAYING, PAUSED, GAME_OVER }
    private GameState state = GameState.MENU;

    public WorldRenderer(SpriteBatch batch, WorldController controller, OrthographicCamera cam) {
        this.batch = batch;
        this.ctrl = controller;
        this.cam = cam;

        hudCam = new OrthographicCamera();
        hudCam.setToOrtho(false, Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT);

    font = new BitmapFont();
    font.getData().setScale(0.04f);
    font.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
    font.setUseIntegerPositions(false);

        titleFont = new BitmapFont();
    titleFont.getData().setScale(0.06f);
    titleFont.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
    titleFont.setUseIntegerPositions(false);
    }

    public void render() {
        switch (state) {
            case MENU:
                renderMenu();
                break;
            case PLAYING:
                renderGame();
                break;
            case PAUSED:
                renderPaused();
                break;
            case GAME_OVER:
                renderGameOver();
                break;
        }
    }

    private void renderMenu() {
        cam.update();
        batch.setProjectionMatrix(cam.combined);
        batch.begin();

        renderBackgroundWithFade();

        batch.end();

        hudCam.update();
        batch.setProjectionMatrix(hudCam.combined);
        batch.begin();

        titleFont.setColor(Color.GOLD);
        String title = "CheeseRush";
        GlyphLayout layout = new GlyphLayout(titleFont, title);
        float titleX = hudCam.viewportWidth / 2f - layout.width / 2f;
        titleFont.draw(batch, layout, titleX, hudCam.viewportHeight / 2f + 2f);

        font.setColor(Color.WHITE);
        String hint = "[Pressione SPACE para iniciar]";
        GlyphLayout hintLayout = new GlyphLayout(font, hint);
        float hintX = hudCam.viewportWidth / 2f - hintLayout.width / 2f;
        font.draw(batch, hintLayout, hintX, hudCam.viewportHeight / 2f - 1f);

        batch.end();

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            state = GameState.PLAYING;
            if (!ctrl.isGameStarted())
                ctrl.startGame();
        }
    }

    private void renderPaused() {
        cam.update();
        batch.setProjectionMatrix(cam.combined);
        batch.begin();

        renderBackgroundWithFade();

        batch.end();

        hudCam.update();
        batch.setProjectionMatrix(hudCam.combined);
        batch.begin();

        titleFont.setColor(Color.LIGHT_GRAY);
        String title = "PAUSADO";
        GlyphLayout layout = new GlyphLayout(titleFont, title);
        float titleX = hudCam.viewportWidth / 2f - layout.width / 2f;
        titleFont.draw(batch, layout, titleX, hudCam.viewportHeight / 2f + 2f);

        font.setColor(Color.WHITE);
        String hint1 = "[Pressione ESC ou SPACE para continuar]";
        String hint2 = "[Pressione R para reiniciar | M para menu principal]";
        GlyphLayout h1 = new GlyphLayout(font, hint1);
        GlyphLayout h2 = new GlyphLayout(font, hint2);
        float hx = hudCam.viewportWidth / 2f - h1.width / 2f;
        font.draw(batch, h1, hx, hudCam.viewportHeight / 2f - 1f);
        font.draw(batch, h2, hudCam.viewportWidth / 2f - h2.width / 2f, hudCam.viewportHeight / 2f - 2f);

        batch.end();

        // tratamento de entrada do menu de pausa
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            state = GameState.PLAYING;
            ctrl.resumeGame();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            ctrl.reset();
            ctrl.startGame();
            state = GameState.PLAYING;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.M)) {
            ctrl.reset();
            state = GameState.MENU;
        }
    }

    private void renderGame() {

        cam.position.set(ctrl.player.pos.x + 5f, cam.viewportHeight / 2f, 0f);
        cam.update();

        batch.setProjectionMatrix(cam.combined);
        batch.begin();

        renderBackgroundWithFade();

        if (ctrl.cheeses != null && ctrl.cheeseTexture != null)
            for (Rectangle cheese : ctrl.cheeses)
                batch.draw(ctrl.cheeseTexture, cheese.x, cheese.y, cheese.width, cheese.height);

        if (ctrl.trashes != null && ctrl.trashTexture != null)
            for (Rectangle trash : ctrl.trashes)
                batch.draw(ctrl.trashTexture, trash.x, trash.y, trash.width, trash.height);

    // POMBOS
    for (Pigeon p : ctrl.pombos)
        batch.draw(ctrl.pomboAnim.getKeyFrame(p.stateTime, true),
            p.pos.x, p.pos.y, p.bounds.width, p.bounds.height);

    // CACHORROS 
    for (Dog d : ctrl.dogs)
        batch.draw(ctrl.dogAnim.getKeyFrame(d.stateTime, true), d.pos.x, d.pos.y, d.bounds.width, d.bounds.height);

        batch.draw(ctrl.player.getFrame(), ctrl.player.pos.x, ctrl.player.pos.y, 1.2f, 1.2f);
        batch.draw(ctrl.cat.getFrame(), ctrl.cat.pos.x, ctrl.cat.pos.y, 1.3f, 1.3f);

        batch.end();

        hudCam.update();
        batch.setProjectionMatrix(hudCam.combined);
        batch.begin();

        font.setColor(Color.WHITE);

        // Desenha imagem de HP no canto superior-esquerdo se disponível.
        if (ctrl.hpTextures != null) {
            int index = ctrl.MAX_LIVES - ctrl.lives; 
            if (index < 0) index = 0;
            if (index > 5) index = 5;
            try {
                Texture hpTex = ctrl.hpTextures[index];
                float iconW = 4f; 
                float iconH = 1.1f;
                float iconX = 0.5f;
                float iconY = hudCam.viewportHeight - 1.2f;
                batch.draw(hpTex, iconX, iconY, iconW, iconH);
            } catch (Exception e) {
                font.draw(batch, "Lives: " + ctrl.lives, 0.5f, hudCam.viewportHeight - 0.6f);
            }
        }

        // Contador de queijo
        if (ctrl.cheeseTexture != null) {
            float iconSize = 0.8f; 
            float iconX = hudCam.viewportWidth - 6f;
            float iconY = hudCam.viewportHeight - 1.2f;
            batch.draw(ctrl.cheeseTexture, iconX, iconY, iconSize, iconSize);
            float numberX = iconX + iconSize + 0.2f;
            float numberY = hudCam.viewportHeight - 0.6f;
            font.draw(batch, String.valueOf(ctrl.cheeseCount), numberX, numberY);
        } else {
            font.draw(batch, "Cheese: " + ctrl.cheeseCount, hudCam.viewportWidth - 5f, hudCam.viewportHeight - 0.6f);
        }

        batch.end();

        // abrir menu de pausa
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            state = GameState.PAUSED;
            ctrl.pauseGame();
            return;
        }

        if (ctrl.gameOver)
            state = GameState.GAME_OVER;
    }

    private void renderGameOver() {
        cam.update();
        batch.setProjectionMatrix(cam.combined);
        batch.begin();

        renderBackgroundWithFade();

        batch.end();

        hudCam.update();
        batch.setProjectionMatrix(hudCam.combined);
        batch.begin();

        titleFont.setColor(Color.RED);
        String msg = "Você foi capturado!";
        GlyphLayout msgLayout = new GlyphLayout(titleFont, msg);
        float msgX = hudCam.viewportWidth / 2f - msgLayout.width / 2f;
        float msgY = hudCam.viewportHeight / 2f + msgLayout.height / 2f;
        float margin = 0.2f;
        if (msgY + margin > hudCam.viewportHeight) msgY = hudCam.viewportHeight - margin;
        titleFont.draw(batch, msgLayout, msgX, msgY);

        font.setColor(Color.WHITE);
        String retry = "[Pressione SPACE para tentar novamente]";
        GlyphLayout retryLayout = new GlyphLayout(font, retry);
        float retryX = hudCam.viewportWidth / 2f - retryLayout.width / 2f;
        float retryY = msgY - msgLayout.height - 0.7f;
        if (retryY < retryLayout.height + margin) retryY = retryLayout.height + margin;
        font.draw(batch, retryLayout, retryX, retryY);

        batch.end();

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || Gdx.input.justTouched()) {
            try { ctrl.resetGame(); }
            catch (Exception e) { try { ctrl.reset(); } catch (Exception ignored) {} }
            state = GameState.PLAYING;
        }
    }

    private void renderBackgroundWithFade() {
        float w = cam.viewportWidth;
        float h = Constants.WORLD_HEIGHT;

        float bgOffset = ctrl.bgScroll % w;

        for (int i = -1; i <= 1; i++)
            batch.draw(ctrl.currentBg, cam.position.x - bgOffset + i * w - w / 2f, 0, w, h);

        if (ctrl.fading && ctrl.nextBg != null) {
            float alpha = Math.min(ctrl.fadeTime / ctrl.getFadeDuration(), 1f);
            batch.setColor(1f, 1f, 1f, alpha);

            for (int i = -1; i <= 1; i++)
                batch.draw(ctrl.nextBg, cam.position.x - bgOffset + i * w - w / 2f, 0, w, h);

            batch.setColor(Color.WHITE);
        }
    }

    public void resize(int width, int height) {
        cam.setToOrtho(false, Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT);
        hudCam.setToOrtho(false, Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT);
    }

    public void dispose() {
        font.dispose();
        titleFont.dispose();
    }
}
