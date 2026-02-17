import javax.swing.JFrame;

public class GameFrame extends JFrame {
    
    public GameFrame() {
        setTitle("CorBrawl");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        
        GamePanel panel = new GamePanel();
        add(panel);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
        
        panel.requestFocusInWindow();
    }
}
