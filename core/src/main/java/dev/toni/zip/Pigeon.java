package dev.toni.zip;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public class Pigeon {

    public float x, y;
    public float speed = 150; // VELOCIDADE DO POMBO
    public Texture texture;
    public Rectangle bounds;

    public Pigeon(float x, float y) {
        this.x = x;
        this.y = y;

        texture = new Texture("pigeon.png"); // coloque sua textura aqui
        bounds = new Rectangle(x, y, texture.getWidth(), texture.getHeight());
    }

    public void update(float delta) {
        // Movimento igual rato/gato: vai para a ESQUERDA
        x -= speed * delta;

        // Atualiza bounding box
        bounds.setPosition(x, y);
    }

    public void render(SpriteBatch batch) {
        batch.draw(texture, x, y);
    }
}
