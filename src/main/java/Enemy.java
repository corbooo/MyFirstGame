import java.awt.Rectangle;
import java.util.List;
import java.util.Random;

public class Enemy {
    public double x, y;
    public int hp;
    public int size;

    private long lastShotNanos = 0L;
    private final long SHOT_COOLDOWN_NANOS = 5_000_000_000L;
    private final int bulletSize = 20;

    private double speed = 1.0;
    private boolean isMoving = false;
    private long movingStartNanos;
    private final long MOVING_TIME_NANOS = 1_000_000_000L;
    private enum Direction {UP, DOWN, LEFT, RIGHT}
    private Direction direction;

    private static final Random rng = new Random();

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

    public void update(double playerX, double playerY, double camX, double camY, int WIDTH, int HEIGHT, List<Bullet> bullets) {
        int margin = WIDTH/5;
        double leftWall = camX - margin;
        double topWall = camY - margin;
        double rightWall = camX + WIDTH + margin;
        double bottomWall = camY + HEIGHT + margin;

        if (x < rightWall && x > leftWall && y > topWall && y < bottomWall) {
            fireBullet(playerX, playerY, bullets);
        }

        if (isMoving && System.nanoTime() - movingStartNanos < MOVING_TIME_NANOS) {
            move();
        }
        else if (rng.nextInt(200) == 1) {
            int num = rng.nextInt(4);
            switch (num) {
                case 0 -> direction = Direction.UP;
                case 1 -> direction = Direction.DOWN;
                case 2 -> direction = Direction.LEFT;
                case 3 -> direction = Direction.RIGHT;
            }
            isMoving = true;
            movingStartNanos = System.nanoTime();
        } else isMoving = false;

    }

    private void fireBullet(double playerX, double playerY, List<Bullet> bullets) {
        long now = System.nanoTime();
        if (now - lastShotNanos < SHOT_COOLDOWN_NANOS) return;

        lastShotNanos = now;
        Bullet bullet = new Bullet((int) x, (int) y, bulletSize, false);
        bullet.aimAt(playerX, playerY);
        bullets.add(bullet);
    }

    private void move() {
        switch (direction) {
            case UP -> y -= speed;
            case DOWN -> y += speed;
            case LEFT -> x -= speed;
            case RIGHT -> x += speed;
        }
    }
}
