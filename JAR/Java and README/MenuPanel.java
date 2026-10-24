import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class MenuPanel extends JPanel {
    private final GameFrame frame;
    private final JButton loadBtn;

    public MenuPanel(GameFrame frame) {
        this.frame = frame;
        setPreferredSize(new Dimension(600, 660));
        setBackground(new Color(12, 12, 22));
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = GridBagConstraints.RELATIVE;
        gbc.fill  = GridBagConstraints.HORIZONTAL;

        // ── Title ──────────────────────────────────────────
        JLabel title = new JLabel("SNAKE", SwingConstants.CENTER);
        title.setFont(new Font("Monospaced", Font.BOLD, 64));
        title.setForeground(new Color(40, 220, 40));
        gbc.insets = new Insets(0, 0, 4, 0);
        add(title, gbc);

        JLabel sub = new JLabel("Full Featured Edition", SwingConstants.CENTER);
        sub.setFont(new Font("Monospaced", Font.ITALIC, 15));
        sub.setForeground(new Color(70, 160, 70));
        gbc.insets = new Insets(0, 0, 44, 0);
        add(sub, gbc);

        // ── Buttons ────────────────────────────────────────
        gbc.insets = new Insets(7, 70, 7, 70);

        JButton newBtn = btn("▶   NEW GAME", new Color(25, 110, 25));
        newBtn.addActionListener(e -> frame.showGame(false));
        add(newBtn, gbc);

        loadBtn = btn("📂  LOAD GAME", new Color(25, 80, 110));
        loadBtn.addActionListener(e -> frame.showGame(true));
        add(loadBtn, gbc);

        JButton scoresBtn = btn("🏆  HIGH SCORES", new Color(110, 90, 10));
        scoresBtn.addActionListener(e -> frame.showHighScores());
        add(scoresBtn, gbc);

        JButton quitBtn = btn("✖   QUIT", new Color(110, 25, 25));
        quitBtn.addActionListener(e -> System.exit(0));
        add(quitBtn, gbc);

        // ── Controls hint ──────────────────────────────────
        JLabel hint = new JLabel(
            "<html><center>Arrow Keys / WASD to move &nbsp;|&nbsp; P = Pause"
            + " &nbsp;|&nbsp; F5 / Ctrl+S = Save &nbsp;|&nbsp; M = Menu</center></html>",
            SwingConstants.CENTER);
        hint.setFont(new Font("Monospaced", Font.PLAIN, 11));
        hint.setForeground(new Color(55, 90, 55));
        gbc.insets = new Insets(40, 10, 0, 10);
        add(hint, gbc);
    }

    private JButton btn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setFont(new Font("Monospaced", Font.BOLD, 16));
        b.setForeground(Color.WHITE);
        b.setBackground(bg);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(320, 52));
        b.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { b.setBackground(bg.brighter()); }
            @Override public void mouseExited (MouseEvent e) { b.setBackground(bg); }
        });
        return b;
    }

    public void refresh() {
        boolean hasSave = frame.hasSaveFile();
        loadBtn.setEnabled(hasSave);
        loadBtn.setForeground(hasSave ? Color.WHITE : new Color(80, 80, 80));
    }
}
