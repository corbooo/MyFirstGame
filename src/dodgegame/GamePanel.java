package src.dodgegame;

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

    //Spike generation
    private final List<Spike> spikes = new ArrayList<>();
    private final Random rng = new Random();

    private final int spikeSize = 40;
    private final int targetSpikes = 60;

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
            addRandomSpikeNEarPLayer();
        }

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                aimScreenX = e.getX();
                aimScreenY = e.getY();
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

    private void addRandomSpikeNEarPLayer() {
        while (true) {
            int x = (int) (px + (rng.nextDouble() * 2 -1) * rangeX);
            int y = (int) (py + (rng.nextDouble() * 2 -1) * rangeY);

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
            addRandomSpikeNEarPLayer();
        }
    }

    private boolean isInsideView(double x, double y) {
        return x >= camX && x <= camX + WIDTH &&
            y >= camY && y <= camY + HEIGHT;
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
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.setColor(Color.RED);
        for (Spike s : spikes) {
            int sx = (int) (s.x - camX);
            int sy = (int) (s.y - camY);
            g.fillRect(sx, sy, s.size, s.size);
        }
        
        // Draw player ALWAYS at center of screen
        int playerSize = 30;
        int playerScreenX = WIDTH / 2 - playerSize / 2;
        int playerScreenY = HEIGHT / 2 - playerSize / 2;
        
        g.setColor(Color.BLUE);
        g.fillRect(playerScreenX, playerScreenY, playerSize, playerSize);
        
        // Draw "aim line" from player center to last click (for future shooting)
        g.setColor(Color.BLACK);
        g.drawLine(WIDTH / 2, HEIGHT / 2, aimScreenX, aimScreenY);
        
        // Debug text: show world position
        g.drawString("Player world: (" + (int)px + ", " + (int)py + ")", 10, 20);

        if (!alive) {
            g.setColor(Color.BLACK);
            g.drawString("GAME OVER", WIDTH / 2 - 40, HEIGHT / 2 - 50);
        }
    }
}