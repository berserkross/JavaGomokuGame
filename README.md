# Java Gomoku (Five in a Row) — CSC-1004 Course Project

A Gomoku game written in Java with both console and JavaFX GUI modes.
Submitted as part of **CSC-1004: Computational Laboratory Using Java**,
The Chinese University of Hong Kong, Shenzhen (CUHK-SZ).

Course context: the Java game assignment requires preliminary game rules,
Java GUI, direct mouse control, and a time limit. [3](@ref)

## Features

Core engine (`GomokuGame.java`)
- Configurable board size (validated 5–20, default 20×20)
- Two players: Black(1) starts, White(2) follows
- Five-in-a-row win detection in horizontal/vertical/both diagonals
- Draw detection when the board is full
- Move counting and per-player longest-streak statistics
- Per-move time limit with timeout loss (default 30s)
- Cross-round score tracking: black wins / white wins / draws (kept in memory for the current run)
- `synchronized` methods for thread-safe move and timer updates
- Board copy accessor to avoid external mutation

Console mode (`GomokuGameConsole.java`)
- ASCII board rendering
- Turn prompt, remaining-time display, input validation
- `0 0` to exit, automatic timer per move

JavaFX mode (`GomokuGameFX.java`)
- Intersection-based 20×20 board (stones placed on line crossings)
- Mouse click to place stones via transparent click areas
- Real-time status panel: current player, timers, moves, streaks, scores
- New Game / Exit buttons, menu bar, About dialog
- Win dialog and timeout dialog
- Star points and shadows for stones

## Tech Stack

- Java SE OOP (model/service separation)
- JavaFX (Pane, Circle, Line, Rectangle, Timeline, Alert, MenuBar)
- `java.util.Timer` for console countdown

## Project Structure

The source files live at the repository root; they declare
`package com.example.javagomokugame`, so `javac -d out` produces that package
layout under `out/`.

    GomokuGame.java        # Rules, win check, timer, statistics
    GomokuGameConsole.java # Text-based gameplay
    GomokuGameFX.java      # JavaFX GUI gameplay

## Requirements

- JDK 17 or 21 (JavaFX 21 recommended)
- JavaFX SDK included via `--module-path` (no Maven project is included)

## Run console mode

javac *.java -d out
java -cp out com.example.javagomokugame.GomokuGameConsole

Console input format: `row column` using 1-based indices, e.g. `8 8`.
Enter `0 0` to quit.

## Run JavaFX mode (plain JDK + JavaFX SDK)

# Set JAVA_HOME and JAVAFX_LIB to your JavaFX lib folder
javac --module-path $JAVAFX_LIB --add-modules javafx.controls \
  *.java -d out

java --module-path $JAVAFX_LIB --add-modules javafx.controls \
  -cp out com.example.javagomokugame.GomokuGameFX

For JavaFX setup, download the SDK matching your JDK and add
`javafx.controls` (and `javafx.fxml` if used) via `--module-path` /
`--add-modules`.

## How to play

1. Start JavaFX mode (or console mode).
2. Black places a stone on an empty intersection when it is Black’s turn.
3. White does the same on the next turn.
4. First to make five in a row horizontally, vertically, or diagonally wins.
5. If a player’s per-move timer reaches 0, the opponent wins by timeout.
6. Use “New Game” to reset the board and timers.

## Notes for CSC-1004

- GUI + mouse control: JavaFX click areas on intersections.
- Time limit: 30s per move by default, configurable in `GomokuGame`.
- OOP: game model is independent from console/JavaFX views.
- Advanced options you can mention: timeout-by-timer, longest-streak stats,
  cross-game scoreboard, star-point rendering.

Repository: https://github.com/berserkross/JavaGomokuGame
