package src.dodgegame;

import java.awt.Rectangle;

public class Spike {
    public int x, y; // world coords
    public int size;

    public Spike(int x, int y, int size) {
        this.x = x;
        this.y = y;
        this.size = size;
    }

    public Rectangle getHitBox() {
        return new Rectangle(x, y, size, size);
    }
}
