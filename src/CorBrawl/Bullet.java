package src.CorBrawl;

import java.awt.Rectangle;

public class Bullet {
    public int x;
    public int y;
    public double vx;
    public double vy;
    public int size;

    private double speed = 8.0; //Speed of the bullet

    public Bullet(int x, int y, int size) {
        this.x = x;
        this.y = y;
        this.size = size;
    }

    public void setVelocity(int aimScreenX, int aimScreenY) {
        double dx = aimScreenX - GamePanel.WIDTH / 2.0;
        double dy = aimScreenY - GamePanel.HEIGHT / 2.0;
        
        double length = Math.sqrt(dx * dx + dy * dy);

        if (length == 0) return;

        vx = (dx / length) * speed;
        vy = (dy / length) * speed;
    }

    public Rectangle getHitbox() {
        return new Rectangle(x, y, size, size);
    }

}
