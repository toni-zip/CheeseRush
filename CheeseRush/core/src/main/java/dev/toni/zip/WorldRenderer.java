package dev.toni.zip;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
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
    private final BitmapFont titleFont;

    private enum GameState { MENU, PLAYING, GAME_OVER }
    private GameState state = GameState.MENU;

    public WorldRenderer(SpriteBatch batch, WorldController controller, OrthographicCamera cam) {
        this.batch = batch;
        this.ctrl = controller;
        this.cam = cam;

        hudCam = new OrthographicCamera();
        hudCam.setToOrtho(false, Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT);

        font = new BitmapFont();
        font.getData().setScale(0.05f);

        titleFont = new BitmapFont();
        titleFont.getData().setScale(0.12f);
    }

    public void render() {
        // switch tradicional para compatibilidade com Java 8
        switch (state) {
            case MENU:
                renderMenu();
                break;
            case PLAYING:
                renderGame();
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

        // Título centralizado aproximado
        titleFont.setColor(Color.GOLD);
        String title = "CheeseRush";
        float titleX = hudCam.viewportWidth / 2f - (title.length() * 0.16f);
        titleFont.draw(batch, title, titleX, hudCam.viewportHeight / 2f + 2f);

        // Botão de iniciar (use SPACE para compatibilizar com WorldController)
        font.setColor(Color.WHITE);
        String hint = "[Pressione SPACE para iniciar]";
        float hintX = hudCam.viewportWidth / 2f - (hint.length() * 0.06f);
        font.draw(batch, hint, hintX, hudCam.viewportHeight / 2f - 1f);

        batch.end();

        // Iniciar o jogo ao apertar SPACE (consistente com WorldController)
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            state = GameState.PLAYING;
            // também ativamos o jogador/gato aqui para garantir sincronia
            if (!ctrl.isGameStarted()) {
                // o WorldController começa o jogo via espaço normalmente,
                // mas garantimos ativar o gato caso seja necessário.
                ctrl.cat.activate();
            }
        }
    }

    private void renderGame() {
        cam.position.set(ctrl.player.pos.x + 5f, cam.viewportHeight / 2f, 0f);
        cam.update();

        batch.setProjectionMatrix(cam.combined);
        batch.begin();

        renderBackgroundWithFade();

        // Queijos
        if (ctrl.cheeses != null && ctrl.cheeseTexture != null) {
            for (Rectangle cheese : ctrl.cheeses) {
                batch.draw(ctrl.cheeseTexture, cheese.x, cheese.y, cheese.width, cheese.height);
            }
        }

        // Lixeiras
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

        font.setColor(Color.WHITE);
        font.draw(batch, "🧀 " + ctrl.cheeseCount, hudCam.viewportWidth - 5f, hudCam.viewportHeight - 0.6f);

        batch.end();

        // Se morrer (usa o gameOver do WorldController)
        if (ctrl.gameOver) {
            state = GameState.GAME_OVER;
        }
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
        float msgX = hudCam.viewportWidth / 2f - (msg.length() * 0.15f);
        titleFont.draw(batch, msg, msgX, hudCam.viewportHeight / 2f + 1.5f);

        font.setColor(Color.WHITE);
        String retry = "[Pressione SPACE para tentar novamente]";
        float retryX = hudCam.viewportWidth / 2f - (retry.length() * 0.06f);
        font.draw(batch, retry, retryX, hudCam.viewportHeight / 2f - 1f);

        batch.end();

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || Gdx.input.justTouched()) {
            // usa o método que você já adicionou no WorldController
            try {
                ctrl.resetGame();
            } catch (Exception e) {
                // se por acaso você só tiver reset() em vez de resetGame(), chama reset()
                try {
                    ctrl.reset();
                } catch (Exception ignored) {}
            }
            state = GameState.PLAYING;
        }
    }

    private void renderBackgroundWithFade() {
        float w = cam.viewportWidth;
        float h = Constants.WORLD_HEIGHT;

        // Movimento do fundo (scroll)
        float bgSpeed = 0.5f; // velocidade do fundo em relação ao jogador
        float bgOffset = (ctrl.player.pos.x * bgSpeed) % w;

        // Fundo atual (scroll infinito)
        batch.setColor(Color.WHITE);
        for (int i = -1; i <= 1; i++) {
            batch.draw(ctrl.currentBg, cam.position.x - bgOffset + i * w - w / 2f, 0, w, h);
        }

        // Fundo novo (transição suave)
        if (ctrl.fading && ctrl.nextBg != null) {
            float alpha = Math.min(ctrl.fadeTime / ctrl.getFadeDuration(), 1f);
            batch.setColor(1f, 1f, 1f, alpha);

            for (int i = -1; i <= 1; i++) {
                batch.draw(ctrl.nextBg, cam.position.x - bgOffset + i * w - w / 2f, 0, w, h);
            }

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
