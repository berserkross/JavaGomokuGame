package com.example.javagomokugame;

/**
 * GomokuGame - Core game logic for the Gomoku (Five in a Row) game.
 * Handles game rules, board management, win detection, and timing.
 */
public class GomokuGame {
    private int[][] board;          // 0: empty, 1: black stone, 2: white stone
    private int currentPlayer;      // 1: black player, 2: white player
    private boolean gameOver;       // Game completion flag
    private int winner;             // 0: draw, 1: black wins, 2: white wins
    private int boardSize;          // Size of the square board
    private int maxMoves;           // Maximum possible moves (boardSize * boardSize)
    private int totalMoves;         // Total moves made in current game
    private int player1Moves;       // Black player's move count
    private int player2Moves;       // White player's move count
    private int[] longestStreak;    // Longest consecutive stones for each player
    private int timeLimit;          // Time limit per move in seconds
    private int[] remainingTime;    // Remaining time for each player

    // Game statistics across sessions
    private int player1Wins = 0;
    private int player2Wins = 0;
    private int draws = 0;

    /**
     * Constructor with custom board size and time limit.
     * @param boardSize Size of the game board (5-20)
     * @param timeLimit Time limit per move in seconds
     */
    public GomokuGame(int boardSize, int timeLimit) {
        if (boardSize < 5 || boardSize > 20) {
            throw new IllegalArgumentException("Board size should be between 5 and 20.");
        }
        this.boardSize = boardSize;
        this.timeLimit = timeLimit;
        maxMoves = boardSize * boardSize;
        totalMoves = 0;
        player1Moves = 0;
        player2Moves = 0;
        longestStreak = new int[]{0, 0};
        remainingTime = new int[]{timeLimit, timeLimit};

        board = new int[boardSize][boardSize];
        currentPlayer = 1;  // Black starts first
        gameOver = false;
        winner = 0;
    }

    /**
     * Default constructor with standard 20x20 board and 30-second time limit.
     */
    public GomokuGame() {
        this(20, 30);
    }

    /**
     * Resets the game to initial state.
     * Clears board, resets all counters, and resets timers.
     */
    public void reset() {
        board = new int[boardSize][boardSize];
        currentPlayer = 1;
        gameOver = false;
        winner = 0;
        totalMoves = 0;
        player1Moves = 0;
        player2Moves = 0;
        longestStreak = new int[]{0, 0};
        remainingTime = new int[]{timeLimit, timeLimit};
    }

    /**
     * Updates the remaining time for current player.
     * @param seconds Seconds to subtract from current player's time
     * @return true if time still remaining, false if time's up
     */
    public synchronized boolean updateTime(int seconds) {
        if (gameOver) {
            return false;
        }

        int playerIndex = currentPlayer - 1;
        if (playerIndex < 0 || playerIndex >= remainingTime.length) {
            return false;
        }

        remainingTime[playerIndex] -= seconds;

        if (remainingTime[playerIndex] <= 0) {
            // Time's up - opponent wins
            gameOver = true;
            winner = 3 - currentPlayer;
            if (currentPlayer == 1) {
                player2Wins++;
            } else {
                player1Wins++;
            }
            return false;
        }
        return true;
    }

    /**
     * Checks for winning condition from the specified position.
     * Also updates the longest streak record for the player.
     * @param x Row index of the placed stone
     * @param y Column index of the placed stone
     * @return true if the move results in a win, false otherwise
     */
    public boolean checkWin(int x, int y) {
        // Direction vectors: vertical, horizontal, diagonal, anti-diagonal
        int[][] directions = {{0, 1}, {1, 0}, {1, 1}, {1, -1}};

        int player = board[x][y];
        int playerIndex = player - 1;

        if (playerIndex < 0 || playerIndex >= longestStreak.length) {
            return false;
        }

        for (int[] dir : directions) {
            int count = 1;
            int dx = dir[0];
            int dy = dir[1];

            // Check both directions
            for (int step = -1; step <= 1; step += 2) {
                for (int i = 1; i < 5; i++) {
                    int newX = x + i * dx * step;
                    int newY = y + i * dy * step;
                    if (!isValidPosition(newX, newY) || board[newX][newY] != player) {
                        break;
                    }
                    count++;
                }
            }

            // Update longest streak record
            if (count > longestStreak[playerIndex]) {
                longestStreak[playerIndex] = count;
            }

            if (count >= 5) {
                gameOver = true;
                winner = player;
                if (player == 1) {
                    player1Wins++;
                } else {
                    player2Wins++;
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Places a stone at the specified position.
     * Validates move, updates board, checks for win or draw.
     * @param x Row index
     * @param y Column index
     * @return MoveResult containing success status and error message if any
     */
    public synchronized MoveResult move(int x, int y) {
        if (gameOver) {
            return new MoveResult(false, "Game is already over.");
        }

        if (!isValidPosition(x, y)) {
            return new MoveResult(false, "Position is out of bounds.");
        }

        if (board[x][y] != 0) {
            return new MoveResult(false, "This position already has a stone. Please choose an empty intersection.");
        }

        board[x][y] = currentPlayer;
        totalMoves++;

        if (currentPlayer == 1) {
            player1Moves++;
        } else {
            player2Moves++;
        }

        if (totalMoves == maxMoves) {
            gameOver = true;
            winner = 0;  // Draw
            draws++;
            return new MoveResult(true, null);
        }

        if (checkWin(x, y)) {
            return new MoveResult(true, null);
        }

        // Switch player and reset opponent's time
        currentPlayer = (currentPlayer == 1) ? 2 : 1;
        int otherPlayerIndex = currentPlayer - 1;
        remainingTime[otherPlayerIndex] = timeLimit;
        return new MoveResult(true, null);
    }

    /**
     * Inner class to represent the result of a move attempt.
     */
    public static class MoveResult {
        private final boolean success;
        private final String errorMessage;

        public MoveResult(boolean success, String errorMessage) {
            this.success = success;
            this.errorMessage = errorMessage;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }

    /**
     * Gets the number of moves made by a specific player.
     * @param player Player number (1 for black, 2 for white)
     * @return Number of moves made by the player
     */
    public int getPlayerMoves(int player) {
        return player == 1 ? player1Moves : player2Moves;
    }

    /**
     * Gets the longest consecutive stones achieved by a player.
     * @param player Player number (1 for black, 2 for white)
     * @return Longest consecutive stones achieved by the player
     */
    public int getLongestStreak(int player) {
        int index = player - 1;
        if (index < 0 || index >= longestStreak.length) {
            return 0;
        }
        return longestStreak[index];
    }

    /**
     * Gets the remaining time for a player.
     * @param player Player number (1 for black, 2 for white)
     * @return Remaining time in seconds for the player
     */
    public int getRemainingTime(int player) {
        int index = player - 1;
        if (index < 0 || index >= remainingTime.length) {
            return 0;
        }
        return remainingTime[index];
    }

    /**
     * Gets the time limit per move.
     * @return Time limit per move in seconds
     */
    public int getTimeLimit() {
        return timeLimit;
    }

    public int getPlayer1Wins() { return player1Wins; }
    public int getPlayer2Wins() { return player2Wins; }
    public int getDraws() { return draws; }

    /**
     * Checks if a position is within board boundaries.
     * @param x Row index
     * @param y Column index
     * @return true if the position is valid, false otherwise
     */
    public boolean isValidPosition(int x, int y) {
        return x >= 0 && x < boardSize && y >= 0 && y < boardSize;
    }

    public boolean isGameOver() { return gameOver; }
    public int getWinner() { return winner; }
    
    /**
     * Gets a copy of the game board to prevent external modification.
     * @return Deep copy of the board array
     */
    public int[][] getBoard() {
        int[][] boardCopy = new int[boardSize][boardSize];
        for (int i = 0; i < boardSize; i++) {
            System.arraycopy(board[i], 0, boardCopy[i], 0, boardSize);
        }
        return boardCopy;
    }
    
    public int getCurrentPlayer() { return currentPlayer; }
    public int getBoardSize() { return boardSize; }
    public int getTotalMoves() { return totalMoves; }
}
