import java.awt.Rectangle;
import java.util.List;

public class Enemy {
    public double x, y;
    public double vx, vy;
    public int hp;
    public int size;

    public Enemy(double x, double y, int hp, int size) {
        this.x = x;
        this.y = y;
        this.hp = hp;
        this.size = size;
    }

    public Rectangle getHitbox() {
        int left = (int) (x - size / 2.0);
        int top = (int) (y - size / 2.0);
        return new Rectangle(left, top, size, size);
    }

    public void update(double playerX, double playerY, double camX, double camY, List bullets) {

    }

    private void fireBullet() {

    }

    private void move() {

    }
}
