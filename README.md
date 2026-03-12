# 🎮 Pokémon Kanto Adventure

A fan-made Pokémon game in Java featuring all 151 original Pokémon, a top-down tile overworld, turn-based battles, an NBA 2K-style catch minigame, evolution, a Pokémart, and a Pokémon Center — set across the Kanto region.

---

## Requirements

- **Java 17+** — [Download JDK](https://adoptium.net/)
- **Python 3.6+** — only needed once to download sprites
- **Maven** — already bundled in the repo as `apache-maven-3.9.6/`

---

## Quick Start

### 1. Clone the repo

```bash
git clone https://github.com/GauharV/PokemonGame.git
cd PokemonGame
```

### 2. Download sprites (one-time setup, If needed)

Sprites are not committed to the repo. Run this once to download all 151 Pokémon images from PokéAPI:

```bash
python download_sprites.py
```

This saves `.png` files to `src/main/resources/sprites/`. You'll see `✓ #1  ✓ #2 ...` as each downloads. Takes about 1–2 minutes. **You only ever need to do this once.**

> **Windows SSL error?** The script already handles this automatically using a certificate bypass — just make sure Python 3.6+ is installed and retry.

> **Sprites showing as coloured circles?** The game falls back to numbered placeholders if sprites aren't found. Re-run the script and make sure it completes without errors.

### 3. Run the game

**Using the bundled Maven (no install needed):**

```bash
# Windows
apache-maven-3.9.6\bin\mvn.cmd exec:java -Dexec.mainClass=com.pokemon.Main

# macOS / Linux
./apache-maven-3.9.6/bin/mvn exec:java -Dexec.mainClass=com.pokemon.Main
```

**Or if Maven is installed globally:**

```bash
mvn exec:java -Dexec.mainClass=com.pokemon.Main
```

**Or open in VS Code:**
1. Install the [Extension Pack for Java](https://marketplace.visualstudio.com/items?itemName=vscjava.vscode-java-pack)
2. Open the `PokemonGame` folder
3. Open `src/main/java/com/pokemon/Main.java`
4. Click ▶ **Run** or press `F5`

---

## Controls

| Key | Action |
|---|---|
| `Arrow Keys` / `WASD` | Move |
| `ENTER` | Interact with signs |

Pokémon Center and Pokémart activate by **walking through their doors** — no button press needed.

---

## Gameplay

**Starting out** — enter your trainer name and pick a starter: Bulbasaur, Charmander, or Squirtle. You begin in Pallet Town.

**Travelling** — walk to the green exit tiles at map edges to move between locations. Each exit shows a destination label like `↑ Viridian City`.

```
Pallet Town → Route 1 → Viridian City → Mt. Moon → Cerulean City → Route 24 → Lavender Town → Celadon City
```

**Wild battles** — walking through grass triggers random encounters. In battle you can Fight, use the Bag to throw Pokéballs, switch Pokémon, or Run.

**Catching** — an NBA 2K-style timing minigame plays when you throw a ball. Hit the moving bar at the right moment for a better catch rate.

**Pokémon Center** — walk through the red door (cross symbol) to fully heal your party for free.

**Pokémart** — walk through the blue door ($ symbol) to buy Pokéballs, Great Balls, and Ultra Balls. Use `↑↓` to select, `ENTER` to choose quantity, `ESC` to leave.

**HUD buttons (top-right):**

| Button | Action |
|---|---|
| POKÉDEX | View all caught Pokémon |
| PARTY | Check current party HP |
| SAVE | Save your game |
| ≡ | Return to main menu |

---

## Saving & Loading

Click **SAVE** in the HUD at any time. Your save is stored at `~/.pokemon_game/save.dat` and loads automatically from the main menu.

> If you update the game code and the save won't load, delete `~/.pokemon_game/save.dat` and start a new game.

---

## Project Structure

```
PokemonGame/
├── src/main/java/com/pokemon/
│   ├── Main.java                    # Entry point
│   ├── GameState.java               # All player/save data
│   ├── data/
│   │   ├── PokemonDatabase.java     # All 151 Pokémon stats & moves
│   │   ├── MoveDatabase.java        # Move definitions
│   │   ├── TrainerDatabase.java     # Gym leaders & trainers
│   │   └── LocationDatabase.java   # Wild Pokémon per route
│   ├── engine/
│   │   ├── BattleEngine.java        # Turn-based battle logic
│   │   ├── EvolutionEngine.java     # Level-up evolution checks
│   │   └── SaveManager.java         # Serialised save/load
│   ├── model/
│   │   ├── Pokemon.java
│   │   ├── Move.java
│   │   └── Trainer.java
│   └── ui/
│       ├── GameWindow.java          # Main window & screen router
│       ├── MainMenuScreen.java
│       ├── StarterScreen.java
│       ├── WalkingScreen.java       # Overworld (maps, movement, doors)
│       ├── BattleScreen.java        # Battle UI
│       ├── PokedexScreen.java
│       └── CatchMinigame.java       # Timing-bar catch mechanic
├── src/main/resources/
│   ├── sprites/                     # Pokémon PNGs (populated by download_sprites.py)
│   └── battle_theme.mp3
├── download_sprites.py              # One-time sprite downloader
├── pom.xml                          # Maven build config
└── apache-maven-3.9.6/              # Bundled Maven (no install needed)
