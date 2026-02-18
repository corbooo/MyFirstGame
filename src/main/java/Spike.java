import java.awt.Rectangle;
import java.awt.image.BufferedImage;

public class Spike {
    public double x, y;
    public int size;
    public BufferedImage image;

    public Spike(int x, int y, int size, BufferedImage image) {
        this.x = x;
        this.y = y;
        this.size = size;
        this.image = image;
    }

    public Rectangle getHitbox() {
        int left = (int) (x - size / 2.0);
        int top = (int) (y - size / 2.0);
        return new Rectangle(left, top, size, size);
    }
}
