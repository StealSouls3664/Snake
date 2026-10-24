import javax.swing.*;
import java.awt.*;
import java.io.File;

public class GameFrame extends JFrame {
    public static final String MENU   = "MENU";
    public static final String GAME   = "GAME";
    public static final String SCORES = "SCORES";

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel container = new JPanel(cardLayout);

    private final MenuPanel      menuPanel;
    private final GamePanel      gamePanel;
    private final HighScorePanel highScorePanel;

    public GameFrame() {
        setTitle("Snake — Full Featured Edition");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        menuPanel      = new MenuPanel(this);
        gamePanel      = new GamePanel(this);
        highScorePanel = new HighScorePanel(this);

        container.add(menuPanel,      MENU);
        container.add(gamePanel,      GAME);
        container.add(highScorePanel, SCORES);

        add(container);
        pack();
        setLocationRelativeTo(null);
        showMenu();
    }

    public void showMenu() {
        menuPanel.refresh();
        cardLayout.show(container, MENU);
    }

    public void showGame(boolean loadSave) {
        cardLayout.show(container, GAME);
        if (loadSave) gamePanel.loadGame();
        else          gamePanel.newGame();
        gamePanel.requestFocusInWindow();
    }

    public void showHighScores() {
        highScorePanel.refresh();
        cardLayout.show(container, SCORES);
    }

    public boolean hasSaveFile() {
        return new File("savegame.dat").exists();
    }
}
