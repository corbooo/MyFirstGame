package src.CorBrawl;

import java.awt.Rectangle;

public class Spike {
    public double x, y; // world coords
    public int size;

    public Spike(int x, int y, int size) {
        this.x = x;
        this.y = y;
        this.size = size;
    }

    public Rectangle getHitbox() {
        int left = (int) (x - size / 2.0);
        int top = (int) (y - size / 2.0);
        return new Rectangle(left, top, size, size);
    }
}
