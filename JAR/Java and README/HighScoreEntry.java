import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class HighScoreEntry implements Serializable, Comparable<HighScoreEntry> {
    private static final long serialVersionUID = 1L;

    public final String name;
    public final int    score;
    public final int    level;
    public final String date;

    public HighScoreEntry(String name, int score, int level) {
        this.name  = name.length() > 14 ? name.substring(0, 14) : name;
        this.score = score;
        this.level = level;
        this.date  = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
    }

    @Override
    public int compareTo(HighScoreEntry o) {
        return Integer.compare(o.score, this.score); // descending
    }
}
