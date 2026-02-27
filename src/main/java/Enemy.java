import java.awt.Rectangle;
import java.util.List;
import java.util.Random;

public class Enemy {
    public double x, y;
    public double vx, vy;
    public int hp;
    public int size;

    private long lastShotNanos = 0L;
    private final long SHOT_COOLDOWN_NANOS = 3_000_000_000L;
    private final int bulletSize = 20;

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

    public void update(double playerX, double playerY, int WIDTH, int HEIGHT, List bullets) {
        if (Math.abs(x - playerX) < (WIDTH/2 + WIDTH/6) && Math.abs(y - playerY) < (HEIGHT/2 + HEIGHT/6)) {
            fireBullet(playerX, playerY, bullets);
        }
        move();
    }

    private void fireBullet(double playerX, double playerY, List bullets) {
        long now = System.nanoTime();
        if (now - lastShotNanos < SHOT_COOLDOWN_NANOS) return;

        lastShotNanos = now;
        Bullet bullet = new Bullet((int) x, (int) y, bulletSize, false);
        bullet.aimAt(playerX, playerY);
        bullets.add(bullet);
    }

    private void move() {

    }
}
