package src.CorBrawl;

import java.awt.Rectangle;

public class Bullet {
    public int x;
    public int y;
    public int vx;
    public int vy;
    public int size;

    public Bullet(int x, int y, int size) {
        this.x = x;
        this.y = y;
        this.size = size;
    }

    public void setVelocity(int aimScreenX, int aimScreenY) {
        double dx = aimScreenX - GamePanel.WIDTH / 2.0;
        double dy = aimScreenY - GamePanel.HEIGHT / 2.0;
        double angleRads = Math.atan2(dy, dx);
        double angleDegrees = Math.toDegrees(angleRads);
        System.out.println(angleDegrees);
        // TODO: This gives us the angle to where I click. I now need to scale that down for velocity
    }

    public Rectangle getHitbox() {
        return new Rectangle(x, y, size, size);
    }

}
