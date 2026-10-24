import java.io.*;
import java.util.*;

public class ScoreManager {
    private static final String FILE     = "highscores.dat";
    private static final int    MAX_ROWS = 10;

    @SuppressWarnings("unchecked")
    public static List<HighScoreEntry> loadScores() {
        File f = new File(FILE);
        if (!f.exists()) return new ArrayList<>();
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(f))) {
            return (List<HighScoreEntry>) in.readObject();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public static void addScore(HighScoreEntry entry) {
        List<HighScoreEntry> list = loadScores();
        list.add(entry);
        Collections.sort(list);
        if (list.size() > MAX_ROWS) list = list.subList(0, MAX_ROWS);
        persist(new ArrayList<>(list));
    }

    private static void persist(List<HighScoreEntry> list) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(FILE))) {
            out.writeObject(list);
        } catch (IOException ignored) {}
    }
}
