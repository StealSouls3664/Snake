import java.awt.Point;
import java.io.Serializable;
import java.util.*;

public class GameState implements Serializable {
    private static final long serialVersionUID = 3L;

    public LinkedList<Point> snake;
    public Point food;
    public Point bonusFood;
    public int   direction;
    public int   score;
    public int   level;
    public int   foodEaten;
    public boolean bonusFoodActive;
    public int   bonusFoodTicks;
    public List<Point> obstacles;

    public GameState(LinkedList<Point> snake, Point food, Point bonusFood,
                     int direction, int score, int level, int foodEaten,
                     boolean bonusFoodActive, int bonusFoodTicks,
                     List<Point> obstacles) {
        this.snake          = new LinkedList<>(snake);
        this.food           = food;
        this.bonusFood      = bonusFood;
        this.direction      = direction;
        this.score          = score;
        this.level          = level;
        this.foodEaten      = foodEaten;
        this.bonusFoodActive = bonusFoodActive;
        this.bonusFoodTicks = bonusFoodTicks;
        this.obstacles      = new ArrayList<>(obstacles);
    }
}
