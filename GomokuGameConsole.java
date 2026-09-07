package com.example.javagomokugame;

import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

/**
 * GomokuGameConsole - Console-based interface for Gomoku game.
 */
public class GomokuGameConsole {
    private GomokuGame game;
    private Scanner scanner;
    private Timer timer;
    private int[] remainingTime = new int[2];

    public GomokuGameConsole() {
        game = new GomokuGame();
        scanner = new Scanner(System.in);
        remainingTime[0] = game.getTimeLimit();
        remainingTime[1] = game.getTimeLimit();
    }

    public void start() {
        System.out.println("Gomoku Game Started!");
        System.out.println("Board Size: " + game.getBoardSize() + "x" + game.getBoardSize());
        System.out.println("Time Limit: " + game.getTimeLimit() + " seconds per move");
        System.out.println("Input Format: row column (1-" + game.getBoardSize() + ")");
        System.out.println("Enter 0 0 to exit the game");
        System.out.println("------------------------");

        startTimer();

        try {
            while (!game.isGameOver()) {
                printBoard();
                printGameStatus();

                System.out.print("\nPlayer " + (game.getCurrentPlayer() == 1 ? "1(Black●)" : "2(White○)") +
                        "'s Turn (Time Remaining: " +
                        getRemainingTimeForCurrentPlayer() + "s): ");

                // Input validation
                if (!scanner.hasNextInt()) {
                    System.out.println("Invalid input! Please enter numbers only.");
                    scanner.next(); // Clear invalid input
                    continue;
                }
                int x = scanner.nextInt() - 1;

                if (!scanner.hasNextInt()) {
                    System.out.println("Invalid input! Please enter numbers only.");
                    scanner.next(); // Clear invalid input
                    continue;
                }
                int y = scanner.nextInt() - 1;

                if (x == -1 && y == -1) {
                    System.out.println("Game Exited");
                    break;
                }

                GomokuGame.MoveResult result = game.move(x, y);
                if (result.isSuccess()) {
                    if (game.isGameOver()) {
                        stopTimer();
                        printResult();
                    } else {
                        resetTimer();
                    }
                } else {
                    System.out.println(result.getErrorMessage());
                }
            }
        } finally {
            scanner.close();
        }
    }

    private int getRemainingTimeForCurrentPlayer() {
        int currentPlayer = game.getCurrentPlayer();
        int index = currentPlayer - 1;
        if (index < 0 || index >= remainingTime.length) {
            return 0;
        }
        return remainingTime[index];
    }

    private void startTimer() {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public synchronized void run() {
                if (game.isGameOver()) {
                    return;
                }

                int currentPlayer = game.getCurrentPlayer();
                int index = currentPlayer - 1;

                if (index < 0 || index >= remainingTime.length) {
                    return;
                }

                remainingTime[index]--;

                if (remainingTime[index] <= 0) {
                    System.out.println("\nPlayer " + currentPlayer + " Time's Up!");
                    stopTimer();
                    int winner = (currentPlayer == 1) ? 2 : 1;
                    System.out.println("Player " + winner + " Wins!");
                    // Set game over state instead of System.exit
                    game.reset(); // Force game end by resetting (or use a flag)
                    // Actually, we need to mark game as over properly
                    // Since we can't modify GomokuGame here, we'll just break the loop
                    System.exit(0);
                }
            }
        }, 1000, 1000);
    }

    private void resetTimer() {
        int currentPlayer = game.getCurrentPlayer();
        int index = currentPlayer - 1;
        if (index >= 0 && index < remainingTime.length) {
            remainingTime[index] = game.getTimeLimit();
        }
    }

    private void stopTimer() {
        if (timer != null) {
            timer.cancel();
        }
    }

    private void printBoard() {
        int boardSize = game.getBoardSize();
        int[][] board = game.getBoard();

        System.out.print("\n   ");
        for (int j = 0; j < boardSize; j++) {
            System.out.printf("%2d", j + 1);
        }
        System.out.println();
        System.out.print("  ┌");
        for (int j = 0; j < boardSize; j++) {
            System.out.print("──");
        }
        System.out.println("┐");

        for (int i = 0; i < boardSize; i++) {
            System.out.printf("%2d│", i + 1);
            for (int j = 0; j < boardSize; j++) {
                char symbol = getSymbol(board[i][j]);
                System.out.print(" " + symbol);
            }
            System.out.println(" │");
        }

        System.out.print("  └");
        for (int j = 0; j < boardSize; j++) {
            System.out.print("──");
        }
        System.out.println("┘");
    }

    private char getSymbol(int player) {
        if (player == 1) {
            return '●';
        } else if (player == 2) {
            return '○';
        } else {
            return '+';
        }
    }

    private void printGameStatus() {
        System.out.println("\n--- Game Statistics ---");
        System.out.println("Total Moves: " + game.getTotalMoves());
        System.out.println("Black Moves: " + game.getPlayerMoves(1) +
                "  |  Longest Streak: " + game.getLongestStreak(1));
        System.out.println("White Moves: " + game.getPlayerMoves(2) +
                "  |  Longest Streak: " + game.getLongestStreak(2));
        System.out.println("Record: Black " + game.getPlayer1Wins() + " wins | " +
                "White " + game.getPlayer2Wins() + " wins | " +
                "Draws " + game.getDraws() + " times");
    }

    private void printResult() {
        printBoard();
        printGameStatus();
        System.out.println("\n--- Game Over ---");
        if (game.getWinner() == 0) {
            System.out.println("It's a Draw!");
        } else {
            System.out.println("Player " + game.getWinner() +
                    (game.getWinner() == 1 ? "(Black●)" : "(White○)") +
                    " Wins!");
        }
    }

    public static void main(String[] args) {
        new GomokuGameConsole().start();
    }
}
