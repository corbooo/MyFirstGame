package src.CorBrawl;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GamePanel extends JPanel{

    public static final int WIDTH = 800;
    public static final int HEIGHT = 600;

    private boolean alive = true;
    private final int playerSize = 30;
    
    //Player world position (double = smooth movement)
    private double px = 0;
    private double py = 0;

    //Movement keys
    private boolean up, down, left, right;

    //Camera world position (top-left of screen in world coords)
    private double camX, camY;

    //Where the user last clicked (screen coords)
    private int aimScreenX = WIDTH / 2;
    private int aimScreenY = HEIGHT / 2;

    //Hazard generation
    private final List<Spike> spikes = new ArrayList<>();
    private final List<Enemy> enemies = new ArrayList<>();
    private final List<Bullet> bullets = new ArrayList<>();

    private final Random rng = new Random();

    private final int spikeSize = 40;
    private final int targetSpikes = 60;
    private final int enemySize = 20;
    private final int enemyHp = 3;
    private final int bulletSize = 10;

    private final int baseEnemyCap = 20;
    private final int maxEnemyCap = 60;
    private final int secondsPerCapIncrease = 10; // +1 enemy every 10s

    private long gameStartMs = System.currentTimeMillis();

    private final double rangeX = WIDTH * 2.5;
    private final double rangeY = HEIGHT * 2.5;
    private double safeRadius = 150;


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
            addRandomSpikeNearPLayer();
        }

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                aimScreenX = e.getX();
                aimScreenY = e.getY();

                fireBullet(aimScreenX, aimScreenY);
            }
        });

        //Time "game-loop" (60 FPS-ish)
        new Timer(16, e -> {
            updateGame();
            repaint();
        }).start();
    }

    private Rectangle getPlayerHitbox() {
        int left = (int)(px - playerSize / 2.0);
        int top = (int)(py - playerSize / 2.0);
        return new Rectangle(left, top, playerSize, playerSize);
    }

    private void addRandomSpikeNearPLayer() {
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
        // Remove spikes too far away
        spikes.removeIf(s ->
            Math.abs(s.x - px) > rangeX || Math.abs(s.y - py) > rangeY
        );

        // Add until back to 60 spikes
        while (spikes.size() < targetSpikes) {
            addRandomSpikeNearPLayer();
        }
    }
    private void addRandomEnemyNearPLayer() {
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
            addRandomEnemyNearPLayer();
        }
    }
    private void fireBullet(int aimScreenX, int aimScreenY) {
        Bullet bullet = new Bullet((int) px, (int) py, bulletSize);
        bullet.setVelocity(aimScreenX, aimScreenY);
        bullets.add(bullet);
    }
    private void maintainBullets() {
        
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

        // Move player in WORLD coordinates
        double speed = 4.0;
        if (up) py -= speed;
        if (down) py += speed;
        if (left) px -= speed;
        if (right) px += speed;

        // Camera follows player: player stays centered
        camX = px - (WIDTH / 2.0);
        camY = py - (HEIGHT / 2.0);

        maintainSpikes();
        maintainEnemies();
        maintainBullets();

        if (alive) {
            var playerHitbox = getPlayerHitbox();
            for (Spike s : spikes) {
                if (playerHitbox.intersects((s.getHitBox()))) {
                    alive = false;
                    break;
                }
            }
        }
    }

    private void resetGame() {
        alive = true;
        px = 0;
        py = 0;
        aimScreenX = WIDTH / 2;
        aimScreenY = HEIGHT / 2;
        gameStartMs = System.currentTimeMillis();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.setColor(Color.BLACK);
        for (Spike s : spikes) {
            int sx = (int) (s.x - camX);
            int sy = (int) (s.y - camY);
            g.fillRect(sx, sy, s.size, s.size);
        }
        g.setColor(Color.RED);
        for (Enemy e : enemies) {
            int ex = (int) (e.x - camX);
            int ey = (int) (e.y - camY);
            g.fillRect(ex, ey, e.size, e.size);
        }
        g.setColor(Color.WHITE);
        for (Bullet b : bullets) {
            int bx = (int) (b.x - camX) - bulletSize/2;
            int by = (int) (b.y - camY) - bulletSize/2;
            g.fillRect(bx, by, b.size, b.size);
        }
        
        // Draw player ALWAYS at center of screen
        int playerSize = 30;
        int playerScreenX = WIDTH / 2 - playerSize / 2;
        int playerScreenY = HEIGHT / 2 - playerSize / 2;
        
        g.setColor(Color.BLUE);
        g.fillRect(playerScreenX, playerScreenY, playerSize, playerSize);

        // Debug text
        g.setColor(Color.BLACK);
        g.drawString("Player world: (" + (int)px + ", " + (int)py + ")", 10, 20);
        g.drawString("Enemy cap: " + currentEnemyCap(), 10, 40);
        g.drawString("CamX: " + camX, 10, 60);
        g.drawString("CamY: " + camY, 10, 80);
        g.drawString("AimScreenX: " + aimScreenX, 10, 100);
        g.drawString("AimScreenY: " + aimScreenY, 10, 120);


        if (!alive) {
            g.setColor(Color.BLACK);
            g.drawString("GAME OVER", WIDTH / 2 - 40, HEIGHT / 2 - 50);
        }
    }
}