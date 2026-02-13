package src.CorBrawl;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GamePanel extends JPanel{

    public static final int WIDTH = 1024;
    public static final int HEIGHT = 768;

    private boolean alive = true;
    private int score = 0;
    private final int playerSize = 30;
    private double playerSpeed = 3.0;
    
    //Player world position (double = smooth movement)
    private double px = 0;
    private double py = 0;

    //Movement keys
    private boolean up, down, left, right;

    //Camera world position (top-left of screen in world coords)
    private double camX, camY;

    //Hazard generation
    private final List<Spike> spikes = new ArrayList<>();
    private final List<Enemy> enemies = new ArrayList<>();
    private final List<Bullet> bullets = new ArrayList<>();

    private final Random rng = new Random();

    private final int spikeSize = 50;
    private final int targetSpikes = 60;
    private final int enemySize = 40;
    private final int enemyHp = 3;
    private final int bulletSize = 10;

    private final int baseEnemyCap = 20;
    private final int maxEnemyCap = 60;
    private final int secondsPerCapIncrease = 10; // +1 enemy every 10s

    private long gameStartMs = System.currentTimeMillis();
    private long lastShotNanos = 0L;
    private static final long SHOT_COOLDOWN_NANOS = 400_000_000L; // 400ms

    private final double rangeX = WIDTH * 2.5;
    private final double rangeY = HEIGHT * 2.5;
    private final double safeRadius = 150;


    public GamePanel() {
        setPreferredSize(new Dimension(WIDTH,HEIGHT));
        setBackground(Color.LIGHT_GRAY);
        setOpaque(true);
        setFocusable(true);
        
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_W -> up = true;
                    case KeyEvent.VK_S -> down = true;
                    case KeyEvent.VK_A -> left = true;
                    case KeyEvent.VK_D -> right = true;
                    case KeyEvent.VK_R -> { if (!alive) resetGame(); }
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_W -> up = false;
                    case KeyEvent.VK_S -> down = false;
                    case KeyEvent.VK_A -> left = false;
                    case KeyEvent.VK_D -> right = false;
                }
            }
        });

        while (spikes.size() < targetSpikes) {
            addRandomSpikeNearPlayer();
        }

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    tryShoot(e.getX(), e.getY());
                }
            }
        });

        //Time "game-loop" (60 FPS-ish)
        new Timer(16, e -> {
            updateGame();
            repaint();
        }).start();
    }

    private void tryShoot(int aimScreenX, int aimScreenY) {
        long now = System.nanoTime();
        if (now - lastShotNanos < SHOT_COOLDOWN_NANOS) return;

        lastShotNanos = now;
        fireBullet(aimScreenX, aimScreenY);
    }

    private Rectangle getPlayerHitbox() {
        int left = (int)(px - playerSize / 2.0);
        int top = (int)(py - playerSize / 2.0);
        return new Rectangle(left, top, playerSize, playerSize);
    }

    private void addRandomSpikeNearPlayer() {
        while (true) {
            int x = (int) (px + (rng.nextDouble() * 2 - 1) * rangeX);
            int y = (int) (py + (rng.nextDouble() * 2 - 1) * rangeY);

            double dx = x - px;
            double dy = y - py;

            if (dx * dx + dy * dy < safeRadius * safeRadius) continue; // too close to player
            if (isInsideView(x, y)) continue; // inside current viewing window

            spikes.add(new Spike(x, y, spikeSize));
            return;
        }
    }
    private void maintainSpikes() {
        spikes.removeIf(s ->
            Math.abs(s.x - px) > rangeX || Math.abs(s.y - py) > rangeY
        );
        while (spikes.size() < targetSpikes) {
            addRandomSpikeNearPlayer();
        }
    }
    private void addRandomEnemyNearPlayer() {
        while (true) {
            int x = (int) (px + (rng.nextDouble() * 2 -1) * rangeX);
            int y = (int) (py + (rng.nextDouble() * 2 -1) * rangeY);

            double dx = x - px;
            double dy = y - py;

            if (dx * dx + dy * dy < safeRadius * safeRadius) continue; // too close to player
            if (isInsideView(x, y)) continue; // inside current viewing window

            enemies.add(new Enemy(x, y, enemyHp, enemySize));
            return;
        }
    }
    private void maintainEnemies() {
        enemies.removeIf(e ->
            Math.abs(e.x - px) > rangeX || Math.abs(e.y - py) > rangeY
        );
        int cap = currentEnemyCap();
        while (enemies.size() < cap) {
            addRandomEnemyNearPlayer();
        }
    }
    private void fireBullet(int aimScreenX, int aimScreenY) {
        Bullet bullet = new Bullet((int) px, (int) py, bulletSize, true);
        bullet.setVelocity(aimScreenX, aimScreenY);
        bullets.add(bullet);
    }
    private void maintainBullets() {
        bullets.removeIf(b ->
            Math.abs(b.x - px) > rangeX || Math.abs(b.y - py) > rangeY
        );
    }
    private void bulletCollisions() {
        Rectangle playerHitbox = getPlayerHitbox();
        for (var bIt = bullets.iterator(); bIt.hasNext();) {
            Bullet b = bIt.next();
            Rectangle bBox = b.getHitbox();

            // Player bullet -> Enemy
            if (b.fromPlayer) {
                boolean hitSomething = false;
                for (var eIt = enemies.iterator(); eIt.hasNext();) {
                    Enemy e = eIt.next();
                    if (bBox.intersects(e.getHitbox())) {
                        eIt.remove();
                        bIt.remove();
                        score += 50;
                        hitSomething = true;
                        break;
                    }
                }
                if (hitSomething) continue;
                for (var sIt = spikes.iterator(); sIt.hasNext();) {
                    Spike s = sIt.next();
                    if (bBox.intersects(s.getHitbox())) {
                        bIt.remove();
                        hitSomething = true;
                        break;
                    }
                }
            }
            // Enemy bullet -> Player
            else {
                if (bBox.intersects(playerHitbox)) {
                    bIt.remove();
                    alive = false;
                    return;
                }
            }
        }
    }

    private boolean isInsideView(double x, double y) {
        return x >= camX && x <= camX + WIDTH &&
            y >= camY && y <= camY + HEIGHT;
    }

    private int currentEnemyCap() {
        long elapsedMs = System.currentTimeMillis() - gameStartMs;
        long elapsedSeconds = elapsedMs / 1000;

        int cap = baseEnemyCap + (int)(elapsedSeconds / secondsPerCapIncrease);
        return Math.min(cap, maxEnemyCap);
    }

    private void updateGame() {
        if (!alive) return;

        if (up) py -= playerSpeed;
        if (down) py += playerSpeed;
        if (left) px -= playerSpeed;
        if (right) px += playerSpeed;

        camX = px - (WIDTH / 2.0);
        camY = py - (HEIGHT / 2.0);

        maintainSpikes();
        maintainEnemies();
        maintainBullets();

        if (alive) {
            var playerHitbox = getPlayerHitbox();
            for (Spike s : spikes) {
                if (playerHitbox.intersects(s.getHitbox())) {
                    alive = false;
                    break;
                }
            }
        }
        for (Bullet b : bullets) {
            b.update();
        }
        bulletCollisions();
    }

    private void resetGame() {
        alive = true;
        score = 0;
        px = 0;
        py = 0;
        enemies.clear();
        spikes.clear();
        bullets.clear();
        gameStartMs = System.currentTimeMillis();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.setColor(Color.BLACK);
        for (Spike s : spikes) {
            int sx = (int) (s.x - camX) - s.size / 2;
            int sy = (int) (s.y - camY) - s.size / 2;
            g.fillRect(sx, sy, s.size, s.size);
        }
        g.setColor(Color.RED);
        for (Enemy e : enemies) {
            int ex = (int) (e.x - camX) - e.size / 2;
            int ey = (int) (e.y - camY) - e.size / 2;
            g.fillRect(ex, ey, e.size, e.size);
        }
        g.setColor(Color.WHITE);
        for (Bullet b : bullets) {
            int bx = (int) (b.x - camX) - bulletSize/2;
            int by = (int) (b.y - camY) - bulletSize/2;
            g.fillRect(bx, by, b.size, b.size);
        }
        
        int playerScreenX = WIDTH / 2 - playerSize / 2;
        int playerScreenY = HEIGHT / 2 - playerSize / 2;
        g.setColor(Color.BLUE);
        g.fillRect(playerScreenX, playerScreenY, playerSize, playerSize);

        // Debug text
        g.setColor(Color.BLACK);
        g.drawString("Score: " + score, 10, 20);
        g.drawString("Player world: (" + (int)px + ", " + (int)py + ")", 10, 40);
        g.drawString("Enemy cap: " + currentEnemyCap(), 10, 60);

        if (!alive) {
            g.setColor(Color.BLACK);
            g.drawString("GAME OVER", WIDTH / 2 - 40, HEIGHT / 2 - 50);
        }
    }
}