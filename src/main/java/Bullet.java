import java.awt.Rectangle;

public class Bullet {
    public double x, y;
    public double vx, vy;
    public int size;
    public boolean fromPlayer;

    private final double speed = 12.0; //Speed of the bullet

    public Bullet(int x, int y, int size, boolean fromPlayer) {
        this.x = x;
        this.y = y;
        this.size = size;
        this.fromPlayer = fromPlayer;
    }

    public void setVelocity(int aimScreenX, int aimScreenY) {
        double dx = aimScreenX - GamePanel.WIDTH / 2.0;
        double dy = aimScreenY - GamePanel.HEIGHT / 2.0;
        
        double length = Math.sqrt(dx * dx + dy * dy);

        if (length == 0) return;

        vx = (dx / length) * speed;
        vy = (dy / length) * speed;
    }

    public void update() {
        x += vx;
        y += vy;
    }

    public Rectangle getHitbox() {
        int left = (int) (x - size / 2.0);
        int top = (int) (y - size / 2.0);
        return new Rectangle(left, top, size, size);
    }

}
