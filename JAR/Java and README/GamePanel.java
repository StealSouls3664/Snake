import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class GamePanel extends JPanel implements ActionListener {

    // ── Board constants ────────────────────────────────────────────────────────
    static final int COLS   = 20;
    static final int ROWS   = 20;
    static final int CELL   = 30;
    static final int HEADER = 62;   // pixels above the grid

    // ── Direction constants ────────────────────────────────────────────────────
    static final int UP = 0, RIGHT = 1, DOWN = 2, LEFT = 3;

    // ── Tuning constants ───────────────────────────────────────────────────────
    static final int FOOD_PER_LEVEL      = 5;   // food eaten to advance a level
    static final int BONUS_APPEAR_TICKS  = 28;  // ticks between bonus food spawns
    static final int BONUS_LIFE_TICKS    = 18;  // how long bonus food lives

    // ── Game state ─────────────────────────────────────────────────────────────
    private LinkedList<Point> snake;
    private int direction, nextDir;
    private Point      food;
    private Point      bonusFood;
    private boolean    bonusActive;
    private int        bonusTicks;
    private int        ticksSinceBonus;
    private List<Point> obstacles;

    private int  score, level, foodEaten;
    private boolean running, paused, gameOver;

    // level-up flash overlay
    private boolean levelFlash;
    private int     levelFlashCount;

    private final Timer     timer;
    private final GameFrame frame;
    private final Random    rng = new Random();

    // ── Constructor ────────────────────────────────────────────────────────────
    public GamePanel(GameFrame frame) {
        this.frame = frame;
        setPreferredSize(new Dimension(COLS * CELL, ROWS * CELL + HEADER));
        setBackground(new Color(12, 12, 22));
        setFocusable(true);

        timer = new Timer(150, this);

        addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) { handleKey(e); }
        });
    }

    // ── Speed table ───────────────────────────────────────────────────────────
    private int speed(int lvl) {
        int[] delays = {150, 128, 108, 90, 74, 60, 50};
        return delays[Math.min(lvl - 1, delays.length - 1)];
    }

    // ── Public entry points ────────────────────────────────────────────────────
    public void newGame() {
        initState(1, 0, 0);
    }

    private void initState(int startLevel, int startScore, int startFood) {
        snake = new LinkedList<>();
        int cx = COLS / 2, cy = ROWS / 2;
        snake.add(new Point(cx, cy));
        snake.add(new Point(cx, cy + 1));
        snake.add(new Point(cx, cy + 2));

        direction = UP;
        nextDir   = UP;
        score     = startScore;
        level     = startLevel;
        foodEaten = startFood;

        bonusActive     = false;
        bonusTicks      = 0;
        ticksSinceBonus = 0;
        bonusFood       = null;
        levelFlash      = false;
        levelFlashCount = 0;

        obstacles = buildObstacles(level);
        placeFood(true);

        gameOver = false;
        paused   = false;
        running  = true;

        timer.setDelay(speed(level));
        timer.restart();
        repaint();
    }

    // ── Obstacle generation ────────────────────────────────────────────────────
    private List<Point> buildObstacles(int lvl) {
        List<Point> obs = new ArrayList<>();
        if (lvl < 2) return obs;

        int target   = Math.min((lvl - 1) * 3, 20);
        Set<Point> used = new HashSet<>(snake);
        used.add(food != null ? food : new Point(-1, -1));

        for (int tries = 0; tries < 2000 && obs.size() < target; tries++) {
            int x = rng.nextInt(COLS);
            int y = rng.nextInt(ROWS);
            // Keep a clear zone around the snake's starting position
            if (Math.abs(x - COLS / 2) < 4 && Math.abs(y - ROWS / 2) < 5) continue;
            Point p = new Point(x, y);
            if (used.add(p)) obs.add(p);
        }
        return obs;
    }

    // ── Food placement ─────────────────────────────────────────────────────────
    private void placeFood(boolean regularFood) {
        Set<Point> used = new HashSet<>(snake);
        used.addAll(obstacles);
        if (bonusActive && bonusFood != null) used.add(bonusFood);
        if (!regularFood && food != null) used.add(food);

        Point p;
        do { p = new Point(rng.nextInt(COLS), rng.nextInt(ROWS)); }
        while (used.contains(p));

        if (regularFood) food = p;
        else { bonusFood = p; bonusActive = true; bonusTicks = BONUS_LIFE_TICKS; }
    }

    // ── Game loop ──────────────────────────────────────────────────────────────
    @Override
    public void actionPerformed(ActionEvent e) {
        if (running && !paused && !gameOver) tick();
        repaint();
    }

    private void tick() {
        direction = nextDir;
        Point head = snake.getFirst();
        Point next = new Point(head);

        switch (direction) {
            case UP:    next.y--; break;
            case DOWN:  next.y++; break;
            case LEFT:  next.x--; break;
            case RIGHT: next.x++; break;
        }

        // Wall collision
        if (next.x < 0 || next.x >= COLS || next.y < 0 || next.y >= ROWS) { die(); return; }
        // Self collision (body minus tail that is about to move)
        for (int i = 0; i < snake.size() - 1; i++)
            if (snake.get(i).equals(next)) { die(); return; }
        // Obstacle collision
        for (Point obs : obstacles)
            if (obs.equals(next)) { die(); return; }

        snake.addFirst(next);
        boolean grew = false;

        // Regular food
        if (next.equals(food)) {
            score += 10 * level;
            foodEaten++;
            grew = true;
            placeFood(true);

            if (foodEaten % FOOD_PER_LEVEL == 0) {
                level++;
                timer.setDelay(speed(level));
                List<Point> newObs = buildObstacles(level);
                // Remove any that overlap the snake or food
                newObs.removeIf(p -> snake.contains(p) || p.equals(food));
                obstacles = newObs;
                levelFlash = true;
                levelFlashCount = 8;
            }
        }

        // Bonus food
        if (bonusActive && bonusFood != null && next.equals(bonusFood)) {
            score += 50 * level;
            grew = true;
            bonusActive = false;
            bonusFood   = null;
            ticksSinceBonus = 0;
        }

        if (!grew) snake.removeLast();

        // Bonus food lifecycle
        if (bonusActive) {
            if (--bonusTicks <= 0) {
                bonusActive = false;
                bonusFood   = null;
                ticksSinceBonus = 0;
            }
        } else if (++ticksSinceBonus >= BONUS_APPEAR_TICKS) {
            placeFood(false);
            ticksSinceBonus = 0;
        }

        if (levelFlash && --levelFlashCount <= 0) levelFlash = false;
    }

    private void die() {
        running  = false;
        gameOver = true;
        timer.stop();
        new File("savegame.dat").delete();

        SwingUtilities.invokeLater(() -> {
            String name = JOptionPane.showInputDialog(this,
                    "GAME OVER\n\nScore: " + score + "   Level: " + level
                    + "   Length: " + snake.size()
                    + "\n\nEnter your name for the leaderboard:",
                    "Game Over", JOptionPane.PLAIN_MESSAGE);
            if (name == null || name.isBlank()) name = "Anonymous";
            ScoreManager.addScore(new HighScoreEntry(name.trim(), score, level));
            frame.showHighScores();
        });
    }

    // ── Input ──────────────────────────────────────────────────────────────────
    private void handleKey(KeyEvent e) {
        int k = e.getKeyCode();
        switch (k) {
            case KeyEvent.VK_UP:
            case KeyEvent.VK_W:
                if (direction != DOWN) nextDir = UP;   break;
            case KeyEvent.VK_DOWN:
                if (direction != UP)   nextDir = DOWN; break;
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_A:
                if (direction != RIGHT) nextDir = LEFT; break;
            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_D:
                if (direction != LEFT) nextDir = RIGHT; break;
            case KeyEvent.VK_S:
                if (e.isControlDown()) saveGame();
                else if (direction != UP) nextDir = DOWN;
                break;
            case KeyEvent.VK_P:
            case KeyEvent.VK_ESCAPE:
                togglePause(); break;
            case KeyEvent.VK_F5:
                saveGame(); break;
            case KeyEvent.VK_M:
                returnToMenu(); break;
        }
    }

    private void togglePause() {
        if (gameOver) return;
        paused = !paused;
        if (paused) timer.stop(); else { timer.start(); requestFocusInWindow(); }
        repaint();
    }

    private void returnToMenu() {
        if (gameOver) return;
        int choice = JOptionPane.showConfirmDialog(this,
                "Return to the main menu?\n(Save first with F5 to keep your progress.)",
                "Return to Menu", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            timer.stop(); running = false;
            frame.showMenu();
        }
    }

    // ── Save / Load ────────────────────────────────────────────────────────────
    public void saveGame() {
        if (gameOver) return;
        GameState state = new GameState(snake, food, bonusFood, direction,
                score, level, foodEaten, bonusActive, bonusTicks, obstacles);
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("savegame.dat"))) {
            out.writeObject(state);
            JOptionPane.showMessageDialog(this, "Game saved successfully!", "Saved",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Save failed: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void loadGame() {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream("savegame.dat"))) {
            GameState s = (GameState) in.readObject();
            snake           = s.snake;
            food            = s.food;
            bonusFood       = s.bonusFood;
            direction       = s.direction;
            nextDir         = s.direction;
            score           = s.score;
            level           = s.level;
            foodEaten       = s.foodEaten;
            bonusActive     = s.bonusFoodActive;
            bonusTicks      = s.bonusFoodTicks;
            obstacles       = s.obstacles;
            ticksSinceBonus = 0;
            levelFlash      = false;
            levelFlashCount = 0;
            gameOver = false;
            paused   = false;
            running  = true;
            timer.setDelay(speed(level));
            timer.restart();
            repaint();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Could not load save file.\nStarting a new game.",
                    "Load Error", JOptionPane.WARNING_MESSAGE);
            newGame();
        }
    }

    // ── Painting ───────────────────────────────────────────────────────────────
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        paintHeader(g2);
        paintGrid(g2);
        paintObstacles(g2);
        if (food != null)                   paintRegularFood(g2);
        if (bonusActive && bonusFood != null) paintBonusFood(g2);
        if (snake != null)                  paintSnake(g2);

        if (paused)     paintPauseOverlay(g2);
        if (levelFlash) paintLevelFlash(g2);
    }

    // Header bar ---------------------------------------------------------------
    private void paintHeader(Graphics2D g2) {
        g2.setColor(new Color(18, 18, 32));
        g2.fillRect(0, 0, COLS * CELL, HEADER);

        // Score
        drawLabel(g2, "SCORE",  20, new Color(180, 180, 180));
        drawValue(g2, String.format("%06d", score), 15, new Color(50, 230, 50));

        // Level
        int midX = COLS * CELL / 2;
        drawLabel(g2, "LEVEL", midX - 28, new Color(180, 180, 180));
        drawValue(g2, String.format("%02d", level), midX - 15, new Color(100, 180, 255));

        // Length
        int rX = COLS * CELL - 110;
        drawLabel(g2, "LENGTH", rX, new Color(180, 180, 180));
        drawValue(g2, String.format("%03d", snake == null ? 0 : snake.size()), rX + 10, new Color(255, 200, 60));

        // Hint strip
        g2.setColor(new Color(45, 60, 45));
        g2.setFont(new Font("Monospaced", Font.PLAIN, 10));
        String hint = "P=Pause   F5/Ctrl+S=Save   M=Menu";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(hint, (COLS * CELL - fm.stringWidth(hint)) / 2, HEADER - 5);

        // Separator line
        g2.setColor(new Color(28, 70, 28));
        g2.fillRect(0, HEADER - 2, COLS * CELL, 2);
    }

    private void drawLabel(Graphics2D g2, String text, int x, Color c) {
        g2.setColor(c);
        g2.setFont(new Font("Monospaced", Font.PLAIN, 12));
        g2.drawString(text, x, 20);
    }

    private void drawValue(Graphics2D g2, String text, int x, Color c) {
        g2.setColor(c);
        g2.setFont(new Font("Monospaced", Font.BOLD, 22));
        g2.drawString(text, x, 48);
    }

    // Grid ---------------------------------------------------------------------
    private void paintGrid(Graphics2D g2) {
        for (int x = 0; x < COLS; x++)
            for (int y = 0; y < ROWS; y++) {
                g2.setColor((x + y) % 2 == 0 ? new Color(16, 16, 26) : new Color(19, 19, 29));
                g2.fillRect(x * CELL, y * CELL + HEADER, CELL, CELL);
            }
    }

    // Obstacles ----------------------------------------------------------------
    private void paintObstacles(Graphics2D g2) {
        for (Point obs : obstacles) {
            int px = obs.x * CELL, py = obs.y * CELL + HEADER;
            g2.setColor(new Color(70, 45, 18));
            g2.fillRect(px + 1, py + 1, CELL - 2, CELL - 2);
            g2.setColor(new Color(105, 70, 28));
            g2.fillRect(px + 3, py + 3, CELL - 6, CELL - 6);
            // brick seams
            g2.setColor(new Color(55, 35, 12));
            g2.drawLine(px + 2, py + CELL / 2, px + CELL - 3, py + CELL / 2);
            g2.drawLine(px + CELL / 2, py + 2, px + CELL / 2, py + CELL / 2 - 1);
            g2.drawLine(px + CELL / 4, py + CELL / 2 + 1, px + CELL / 4, py + CELL - 3);
            g2.drawLine(px + 3 * CELL / 4, py + CELL / 2 + 1, px + 3 * CELL / 4, py + CELL - 3);
        }
    }

    // Regular food (red apple) -------------------------------------------------
    private void paintRegularFood(Graphics2D g2) {
        int px = food.x * CELL, py = food.y * CELL + HEADER;
        // soft glow
        g2.setColor(new Color(200, 30, 30, 55));
        g2.fillOval(px - 4, py - 4, CELL + 8, CELL + 8);
        // apple body
        g2.setColor(new Color(220, 48, 48));
        g2.fillOval(px + 3, py + 4, CELL - 6, CELL - 6);
        // highlight
        g2.setColor(new Color(255, 110, 110));
        g2.fillOval(px + 6, py + 7, 8, 7);
        // stem
        g2.setColor(new Color(70, 130, 25));
        g2.setStroke(new BasicStroke(2f));
        g2.drawLine(px + CELL / 2, py + 4, px + CELL / 2 + 4, py + 1);
        g2.setStroke(new BasicStroke(1f));
    }

    // Bonus food (golden star) -------------------------------------------------
    private void paintBonusFood(Graphics2D g2) {
        int px = bonusFood.x * CELL, py = bonusFood.y * CELL + HEADER;
        long t = System.currentTimeMillis();
        float pulse = 0.25f + 0.25f * (float) Math.sin(t / 100.0);
        // pulsing glow
        g2.setColor(new Color(1f, 0.84f, 0f, pulse));
        g2.fillOval(px - 6, py - 6, CELL + 12, CELL + 12);
        // gold body
        g2.setColor(new Color(255, 215, 0));
        g2.fillOval(px + 3, py + 3, CELL - 6, CELL - 6);
        g2.setColor(new Color(255, 240, 120));
        g2.fillOval(px + 7, py + 7, 8, 7);
        // "x5" tag
        g2.setColor(new Color(160, 80, 0));
        g2.setFont(new Font("Monospaced", Font.BOLD, 8));
        g2.drawString("x5", px + CELL - 14, py + CELL - 3);
        // urgency: dim when near expiry
        if (bonusTicks < 6) {
            g2.setColor(new Color(0, 0, 0, 90));
            g2.fillOval(px + 3, py + 3, CELL - 6, CELL - 6);
        }
    }

    // Snake --------------------------------------------------------------------
    private void paintSnake(Graphics2D g2) {
        int size = snake.size();
        // Draw tail → head so head is always on top
        for (int i = size - 1; i >= 0; i--) {
            Point p = snake.get(i);
            int px = p.x * CELL, py = p.y * CELL + HEADER;

            if (i == 0) {
                // Head
                g2.setColor(new Color(55, 225, 55));
                g2.fillRoundRect(px + 1, py + 1, CELL - 2, CELL - 2, 9, 9);
                g2.setColor(new Color(140, 255, 140));
                g2.fillOval(px + 4, py + 4, 10, 9);
                paintEyes(g2, px, py);
            } else {
                // Body gradient: bright near head, dark at tail
                float t = (float) i / Math.max(1, size - 1);
                int green = (int)(195 - 110 * t);
                int r     = (int)(18  +  32 * t);
                g2.setColor(new Color(r, green, r));
                g2.fillRoundRect(px + 2, py + 2, CELL - 4, CELL - 4, 7, 7);
                // segment shine every other piece
                if (i % 2 == 0) {
                    g2.setColor(new Color(Math.min(r + 25, 255),
                                         Math.min(green + 35, 255),
                                         Math.min(r + 25, 255), 110));
                    g2.fillOval(px + 5, py + 5, 8, 7);
                }
            }
        }
    }

    private void paintEyes(Graphics2D g2, int px, int py) {
        int e1x, e1y, e2x, e2y;
        switch (direction) {
            case UP:    e1x=px+7;  e1y=py+7;  e2x=px+17; e2y=py+7;  break;
            case DOWN:  e1x=px+7;  e1y=py+18; e2x=px+17; e2y=py+18; break;
            case LEFT:  e1x=px+6;  e1y=py+7;  e2x=px+6;  e2y=py+17; break;
            case RIGHT: e1x=px+18; e1y=py+7;  e2x=px+18; e2y=py+17; break;
            default:    e1x=px+7;  e1y=py+7;  e2x=px+17; e2y=py+7;
        }
        g2.setColor(Color.WHITE);
        g2.fillOval(e1x - 3, e1y - 3, 7, 7);
        g2.fillOval(e2x - 3, e2y - 3, 7, 7);
        g2.setColor(new Color(20, 20, 20));
        g2.fillOval(e1x - 1, e1y - 1, 4, 4);
        g2.fillOval(e2x - 1, e2y - 1, 4, 4);
    }

    // Pause overlay ------------------------------------------------------------
    private void paintPauseOverlay(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRect(0, HEADER, COLS * CELL, ROWS * CELL);

        g2.setColor(new Color(50, 225, 50));
        g2.setFont(new Font("Monospaced", Font.BOLD, 46));
        String msg = "PAUSED";
        FontMetrics fm = g2.getFontMetrics();
        int tx = (COLS * CELL - fm.stringWidth(msg)) / 2;
        int ty = HEADER + ROWS * CELL / 2;
        g2.drawString(msg, tx, ty);

        g2.setColor(new Color(140, 200, 140));
        g2.setFont(new Font("Monospaced", Font.PLAIN, 14));
        fm = g2.getFontMetrics();
        String sub = "Press P to resume";
        g2.drawString(sub, (COLS * CELL - fm.stringWidth(sub)) / 2, ty + 38);
    }

    // Level-up flash -----------------------------------------------------------
    private void paintLevelFlash(Graphics2D g2) {
        g2.setColor(new Color(80, 190, 255, 70));
        g2.fillRect(0, HEADER, COLS * CELL, ROWS * CELL);

        g2.setColor(new Color(130, 210, 255));
        g2.setFont(new Font("Monospaced", Font.BOLD, 34));
        String msg = "LEVEL " + level + "!";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(msg,
                (COLS * CELL - fm.stringWidth(msg)) / 2,
                HEADER + ROWS * CELL / 2 + 12);
    }
}
