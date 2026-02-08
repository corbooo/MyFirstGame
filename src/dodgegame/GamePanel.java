package src.dodgegame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class GamePanel extends JPanel{

    public static final int WIDTH = 800;
    public static final int HEIGHT = 600;

    private boolean alive = true;
    private final int playerSize = 30;
    private final Spike spike = new Spike(200, -150, 40);

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

    //A single spike at a fixed world coordinate (proof of scrolling)
    private final int spikeWorldX = 200;
    private final int spikeWorldY = -150;
    private final int spikeSize = 40;


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

        if (alive && getPlayerHitbox().intersects(spike.getHitBox())) {
            alive = false;
        }
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (!alive) {
            g.setColor(Color.BLACK);
            g.drawString("GAME OVER", WIDTH / 2 - 40, HEIGHT / 2 - 50);
        }

        // Convert world -> screen helper
        int spikeScreenX = (int) (spikeWorldX - camX);
        int spikeScreenY = (int) (spikeWorldY - camY);
        g.setColor(Color.RED);
        g.fillRect(spikeScreenX, spikeScreenY, spike.size, spike.size);

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
    }
}