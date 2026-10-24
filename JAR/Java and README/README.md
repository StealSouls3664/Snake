# Snake — Full Featured Edition
### A Java Swing Game

## Requirements
- Java 8 or later (JDK)

## Compile
```
cd snake/
javac *.java
```

## Run
```
java Main
```

---

## Features

| Feature | Details |
|---------|---------|
| **Levels** | 7 speed tiers; every 5 apples = level up |
| **Obstacles** | Brick walls appear from Level 2 (more each level) |
| **Bonus Food** | Golden ×5 multiplier food spawns periodically |
| **Score** | 10 × level per apple; 50 × level per bonus |
| **Save / Load** | F5 or Ctrl+S saves; "Load Game" on menu restores |
| **High Scores** | Top 10 scores saved to `highscores.dat` |
| **Pause** | P or Escape |

## Controls

| Key | Action |
|-----|--------|
| Arrow Keys / WASD | Move |
| P / Escape | Pause / Resume |
| F5 or Ctrl+S | Save game |
| M | Return to Menu |

## Saved Files
- `savegame.dat` — current save slot (one at a time)
- `highscores.dat` — top 10 leaderboard (persistent)

Both files are written in the directory where you run `java Main`.
