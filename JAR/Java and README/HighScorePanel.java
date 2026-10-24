import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class HighScorePanel extends JPanel {
    private final GameFrame frame;
    private final JPanel    rows = new JPanel();

    public HighScorePanel(GameFrame frame) {
        this.frame = frame;
        setPreferredSize(new Dimension(600, 660));
        setBackground(new Color(12, 12, 22));
        setLayout(new BorderLayout(0, 0));

        // Title
        JLabel title = new JLabel("🏆  HIGH SCORES", SwingConstants.CENTER);
        title.setFont(new Font("Monospaced", Font.BOLD, 28));
        title.setForeground(new Color(255, 215, 0));
        title.setBorder(new EmptyBorder(30, 0, 20, 0));
        add(title, BorderLayout.NORTH);

        // Table
        rows.setBackground(new Color(12, 12, 22));
        rows.setLayout(new BoxLayout(rows, BoxLayout.Y_AXIS));
        JScrollPane scroll = new JScrollPane(rows,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(new Color(12, 12, 22));
        add(scroll, BorderLayout.CENTER);

        // Back button
        JButton back = new JButton("← BACK TO MENU");
        back.setFont(new Font("Monospaced", Font.BOLD, 14));
        back.setForeground(Color.WHITE);
        back.setBackground(new Color(30, 85, 30));
        back.setFocusPainted(false);
        back.setBorderPainted(false);
        back.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        back.setPreferredSize(new Dimension(200, 42));
        back.addActionListener(e -> frame.showMenu());

        JPanel south = new JPanel();
        south.setBackground(new Color(12, 12, 22));
        south.setBorder(new EmptyBorder(12, 0, 20, 0));
        south.add(back);
        add(south, BorderLayout.SOUTH);
    }

    public void refresh() {
        rows.removeAll();

        // Header row
        rows.add(buildRow("#", "NAME", "SCORE", "LVL", "DATE", true));
        rows.add(Box.createVerticalStrut(4));

        List<HighScoreEntry> scores = ScoreManager.loadScores();

        if (scores.isEmpty()) {
            JLabel empty = new JLabel("No scores yet — go play!", SwingConstants.CENTER);
            empty.setFont(new Font("Monospaced", Font.PLAIN, 15));
            empty.setForeground(new Color(80, 80, 80));
            empty.setAlignmentX(Component.CENTER_ALIGNMENT);
            empty.setBorder(new EmptyBorder(50, 0, 0, 0));
            rows.add(empty);
        }

        String[] medals = {"🥇", "🥈", "🥉"};
        for (int i = 0; i < scores.size(); i++) {
            HighScoreEntry e = scores.get(i);
            String rank = i < 3 ? medals[i] : String.valueOf(i + 1);
            JPanel row = buildRow(rank, e.name,
                    String.format("%,d", e.score), "L" + e.level, e.date, false);
            if (i == 0)       row.setBackground(new Color(45, 38, 8));
            else if (i == 1)  row.setBackground(new Color(30, 30, 35));
            else if (i == 2)  row.setBackground(new Color(35, 25, 20));
            else              row.setBackground(i % 2 == 0 ? new Color(20, 20, 30) : new Color(16, 16, 26));
            rows.add(row);
        }

        rows.revalidate();
        rows.repaint();
    }

    private JPanel buildRow(String rank, String name, String score,
                             String level, String date, boolean header) {
        JPanel p = new JPanel(new GridLayout(1, 5));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        p.setBackground(header ? new Color(22, 22, 40) : new Color(18, 18, 28));
        p.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(32, 32, 52)));

        Font font = header
                ? new Font("Monospaced", Font.BOLD, 13)
                : new Font("Monospaced", Font.PLAIN, 14);
        Color fg = header ? new Color(140, 140, 200) : Color.WHITE;

        for (String val : new String[]{rank, name, score, level, date}) {
            JLabel lbl = new JLabel(val, SwingConstants.CENTER);
            lbl.setFont(font);
            lbl.setForeground(fg);
            p.add(lbl);
        }
        return p;
    }
}
