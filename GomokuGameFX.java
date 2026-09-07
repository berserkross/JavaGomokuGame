package com.example.javagomokugame;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * GomokuGameFX - JavaFX GUI implementation of Gomoku game with intersection-based board.
 * Stones are placed on intersections (grid line crossings) rather than in grid cells.
 */
public class GomokuGameFX extends Application {
    private static final int BOARD_SIZE = 20;
    private static final int GRID_SPACING = 30;
    private static final int BOARD_MARGIN = 30;
    private static final int GRID_AREA_SIZE = GRID_SPACING * (BOARD_SIZE - 1);
    private static final int BOARD_TOTAL_SIZE = GRID_AREA_SIZE + 2 * BOARD_MARGIN;
    private static final double GRID_AREA_X = BOARD_MARGIN;
    private static final double GRID_AREA_Y = BOARD_MARGIN;

    private GomokuGame game;
    private Pane boardPane;
    private Label currentPlayerLabel;
    private Label blackTimeLabel;
    private Label whiteTimeLabel;
    private Label moveCountLabel;
    private Label blackStreakLabel;
    private Label whiteStreakLabel;
    private Label blackScoreLabel;
    private Label whiteScoreLabel;
    private Label drawCountLabel;

    private Map<String, Circle> stoneMap;
    private List<Circle> shadowList;
    private Rectangle[][] clickAreas;

    private Timeline timer;

    private final Color BOARD_COLOR = Color.rgb(222, 184, 135);
    private final Color GRID_LINE_COLOR = Color.rgb(101, 67, 33);
    private final Color INTERSECTION_COLOR = Color.rgb(60, 40, 20);
    private final Color BLACK_STONE = Color.BLACK;
    private final Color WHITE_STONE = Color.WHITE;

    @Override
    public void start(Stage primaryStage) {
        game = new GomokuGame(BOARD_SIZE, 30);
        stoneMap = new HashMap<>();
        shadowList = new ArrayList<>();
        clickAreas = new Rectangle[BOARD_SIZE][BOARD_SIZE];

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));
        root.setStyle("-fx-background-color: #f0f0f0;");

        MenuBar menuBar = createMenuBar(primaryStage);
        VBox statusPanel = createStatusPanel();
        Pane boardContainer = createBoard();
        HBox controlPanel = createControlPanel();

        root.setTop(menuBar);
        root.setLeft(statusPanel);
        root.setCenter(boardContainer);
        root.setBottom(controlPanel);

        initializeTimer();

        Scene scene = new Scene(root, BOARD_TOTAL_SIZE + 350, BOARD_TOTAL_SIZE + 120);

        primaryStage.setTitle("Gomoku Game");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();

        updateStatus();
    }

    private MenuBar createMenuBar(Stage primaryStage) {
        MenuBar menuBar = new MenuBar();
        menuBar.setStyle("-fx-background-color: #e8e8e8;");

        Menu gameMenu = new Menu("Game");
        MenuItem newGameItem = new MenuItem("New Game");
        MenuItem exitItem = new MenuItem("Exit");

        newGameItem.setOnAction(e -> resetGame());
        exitItem.setOnAction(e -> primaryStage.close());

        gameMenu.getItems().addAll(newGameItem, new SeparatorMenuItem(), exitItem);

        Menu helpMenu = new Menu("Help");
        MenuItem aboutItem = new MenuItem("About");
        aboutItem.setOnAction(e -> showAboutDialog());
        helpMenu.getItems().add(aboutItem);

        menuBar.getMenus().addAll(gameMenu, helpMenu);
        return menuBar;
    }

    private VBox createStatusPanel() {
        VBox statusPanel = new VBox(15);
        statusPanel.setPadding(new Insets(20));
        statusPanel.setPrefWidth(280);
        statusPanel.setStyle("-fx-background-color: #f8f8f8; -fx-border-color: #ccc; -fx-border-width: 1; " +
                "-fx-border-radius: 5; -fx-background-radius: 5;");

        Label statusTitle = new Label("Game Status");
        statusTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        statusTitle.setTextFill(Color.rgb(60, 60, 60));

        currentPlayerLabel = new Label("Current: Black ●");
        currentPlayerLabel.setFont(Font.font(16));
        currentPlayerLabel.setTextFill(Color.rgb(40, 40, 40));

        Label timeTitle = new Label("Time Remaining");
        timeTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        timeTitle.setTextFill(Color.rgb(60, 60, 60));

        blackTimeLabel = new Label("● Black: 30s");
        whiteTimeLabel = new Label("○ White: 30s");
        blackTimeLabel.setFont(Font.font(15));
        whiteTimeLabel.setFont(Font.font(15));

        Label statsTitle = new Label("Game Statistics");
        statsTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        statsTitle.setTextFill(Color.rgb(60, 60, 60));

        moveCountLabel = new Label("Total Moves: 0");
        blackStreakLabel = new Label("Black Streak: 0");
        whiteStreakLabel = new Label("White Streak: 0");
        blackScoreLabel = new Label("Black Wins: 0");
        whiteScoreLabel = new Label("White Wins: 0");
        drawCountLabel = new Label("Draws: 0");

        Font statFont = Font.font(14);
        moveCountLabel.setFont(statFont);
        blackStreakLabel.setFont(statFont);
        whiteStreakLabel.setFont(statFont);
        blackScoreLabel.setFont(statFont);
        whiteScoreLabel.setFont(statFont);
        drawCountLabel.setFont(statFont);

        statusPanel.getChildren().addAll(
                statusTitle, currentPlayerLabel,
                new Separator(),
                timeTitle, blackTimeLabel, whiteTimeLabel,
                new Separator(),
                statsTitle, moveCountLabel, blackStreakLabel, whiteStreakLabel,
                blackScoreLabel, whiteScoreLabel, drawCountLabel
        );

        return statusPanel;
    }

    private Pane createBoard() {
        boardPane = new Pane();
        boardPane.setPrefSize(BOARD_TOTAL_SIZE, BOARD_TOTAL_SIZE);
        boardPane.setMinSize(BOARD_TOTAL_SIZE, BOARD_TOTAL_SIZE);
        boardPane.setMaxSize(BOARD_TOTAL_SIZE, BOARD_TOTAL_SIZE);

        drawBoardBackground();
        drawGridLines();
        drawIntersections();
        drawClickAreas();

        return boardPane;
    }

    private void drawGridLines() {
        double endX = GRID_AREA_X + GRID_AREA_SIZE;
        double endY = GRID_AREA_Y + GRID_AREA_SIZE;

        for (int i = 0; i < BOARD_SIZE; i++) {
            double x = GRID_AREA_X + i * GRID_SPACING;
            Line verticalLine = new Line(x, GRID_AREA_Y, x, endY);
            verticalLine.setStroke(GRID_LINE_COLOR);
            verticalLine.setStrokeWidth(1.5);
            boardPane.getChildren().add(verticalLine);
        }

        for (int i = 0; i < BOARD_SIZE; i++) {
            double y = GRID_AREA_Y + i * GRID_SPACING;
            Line horizontalLine = new Line(GRID_AREA_X, y, endX, y);
            horizontalLine.setStroke(GRID_LINE_COLOR);
            horizontalLine.setStrokeWidth(1.5);
            boardPane.getChildren().add(horizontalLine);
        }
    }

    private void drawIntersections() {
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                double centerX = GRID_AREA_X + col * GRID_SPACING;
                double centerY = GRID_AREA_Y + row * GRID_SPACING;

                Circle intersection = new Circle(centerX, centerY, 1.5, INTERSECTION_COLOR);
                boardPane.getChildren().add(intersection);
            }
        }

        int[] starPositions = {3, 9, 15};
        for (int rowPos : starPositions) {
            for (int colPos : starPositions) {
                double centerX = GRID_AREA_X + colPos * GRID_SPACING;
                double centerY = GRID_AREA_Y + rowPos * GRID_SPACING;
                Circle starPoint = new Circle(centerX, centerY, 4, INTERSECTION_COLOR);
                boardPane.getChildren().add(starPoint);
            }
        }
    }

    private void drawClickAreas() {
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                double centerX = GRID_AREA_X + col * GRID_SPACING;
                double centerY = GRID_AREA_Y + row * GRID_SPACING;

                Rectangle clickArea = new Rectangle(
                        centerX - GRID_SPACING / 2.0,
                        centerY - GRID_SPACING / 2.0,
                        GRID_SPACING,
                        GRID_SPACING
                );

                clickArea.setFill(Color.TRANSPARENT);
                clickArea.setStroke(Color.TRANSPARENT);
                clickArea.setCursor(javafx.scene.Cursor.HAND);

                final int r = row;
                final int c = col;
                clickArea.setOnMouseClicked(e -> handleIntersectionClick(r, c));

                clickArea.setOnMouseEntered(e -> {
                    if (!game.isGameOver() && game.getBoard()[r][c] == 0) {
                        clickArea.setStroke(Color.rgb(0, 255, 0, 0.6));
                        clickArea.setStrokeWidth(2);
                    } else if (!game.isGameOver()) {
                        clickArea.setStroke(Color.rgb(255, 0, 0, 0.8));
                        clickArea.setStrokeWidth(3);
                    }
                });

                clickArea.setOnMouseExited(e -> {
                    clickArea.setStroke(Color.TRANSPARENT);
                });

                clickAreas[row][col] = clickArea;
                boardPane.getChildren().add(clickArea);
            }
        }
    }

    private HBox createControlPanel() {
        HBox controlPanel = new HBox(20);
        controlPanel.setPadding(new Insets(15));
        controlPanel.setAlignment(Pos.CENTER);
        controlPanel.setStyle("-fx-background-color: #e8e8e8; -fx-border-color: #ccc; -fx-border-width: 1; " +
                "-fx-border-radius: 5; -fx-background-radius: 5;");

        Button resetBtn = new Button("New Game");
        resetBtn.setPrefSize(120, 40);
        resetBtn.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        resetBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 5;");
        resetBtn.setOnAction(e -> resetGame());

        Button exitBtn = new Button("Exit Game");
        exitBtn.setPrefSize(120, 40);
        exitBtn.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        exitBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-background-radius: 5;");
        exitBtn.setOnAction(e -> Platform.exit());

        controlPanel.getChildren().addAll(resetBtn, exitBtn);
        return controlPanel;
    }

    private void initializeTimer() {
        timer = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateTimer()));
        timer.setCycleCount(Animation.INDEFINITE);
        timer.play();
    }

    private void updateTimer() {
        if (!game.isGameOver()) {
            int currentPlayer = game.getCurrentPlayer();
            if (game.updateTime(1)) {
                updateTimeLabels();
            } else {
                // Time's up - game over logic already executed in GomokuGame.updateTime()
                if (timer != null) {
                    timer.stop();
                }
                // Update UI to reflect final game state
                updateStatus();
                // Show timeout alert with winner information
                showTimeOutAlert(currentPlayer);
            }
        }
    }

    private void updateTimeLabels() {
        Platform.runLater(() -> {
            blackTimeLabel.setText("● Black: " + game.getRemainingTime(1) + "s");
            whiteTimeLabel.setText("○ White: " + game.getRemainingTime(2) + "s");
        });
    }

    private void handleIntersectionClick(int row, int col) {
        // Ensure execution on JavaFX Application Thread
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(() -> handleIntersectionClick(row, col));
            return;
        }

        // Boundary check for defensive programming
        if (row < 0 || row >= BOARD_SIZE || col < 0 || col >= BOARD_SIZE) {
            return;
        }

        if (game.isGameOver()) {
            showWarning("Game Over", "The game has ended. Please start a new game.");
            return;
        }

        int[][] board = game.getBoard();
        if (board[row][col] != 0) {
            showOccupiedWarning();
            return;
        }

        int currentPlayer = game.getCurrentPlayer();
        GomokuGame.MoveResult result = game.move(row, col);

        if (result.isSuccess()) {
            drawStone(row, col, currentPlayer);
            updateStatus();

            if (game.isGameOver()) {
                if (timer != null) {
                    timer.stop();
                }
                showGameResult();
            } else {
                updateTimeLabels();
            }
        }
    }

    private void drawStone(int row, int col, int player) {
        double centerX = GRID_AREA_X + col * GRID_SPACING;
        double centerY = GRID_AREA_Y + row * GRID_SPACING;
        double stoneRadius = GRID_SPACING * 0.4;

        Circle shadow = new Circle(centerX + 2, centerY + 2, stoneRadius, Color.rgb(0, 0, 0, 0.25));
        shadow.setMouseTransparent(true);
        
        Circle stone = new Circle(centerX, centerY, stoneRadius);
        stone.setFill(player == 1 ? BLACK_STONE : WHITE_STONE);
        stone.setStroke(Color.BLACK);
        stone.setStrokeWidth(1.5);
        stone.setMouseTransparent(true);

        String key = row + "," + col;
        stoneMap.put(key, stone);
        shadowList.add(shadow);

        boardPane.getChildren().addAll(shadow, stone);
    }

    private void updateStatus() {
        Platform.runLater(() -> {
            int currentPlayer = game.getCurrentPlayer();
            String playerText = (currentPlayer == 1 ? "Black ●" : "White ○");
            currentPlayerLabel.setText("Current: " + playerText);

            if (currentPlayer == 1) {
                blackTimeLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16;");
                whiteTimeLabel.setStyle("-fx-font-weight: normal; -fx-font-size: 15;");
            } else {
                blackTimeLabel.setStyle("-fx-font-weight: normal; -fx-font-size: 15;");
                whiteTimeLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16;");
            }

            moveCountLabel.setText("Total Moves: " + game.getTotalMoves());
            blackStreakLabel.setText("Black Streak: " + game.getLongestStreak(1));
            whiteStreakLabel.setText("White Streak: " + game.getLongestStreak(2));
            blackScoreLabel.setText("Black Wins: " + game.getPlayer1Wins());
            whiteScoreLabel.setText("White Wins: " + game.getPlayer2Wins());
            drawCountLabel.setText("Draws: " + game.getDraws());
        });
    }

    private void resetGame() {
        if (boardPane == null) {
            return;
        }

        if (timer != null) {
            timer.stop();
        }

        game.reset();
        stoneMap.clear();
        shadowList.clear();

        boardPane.getChildren().clear();
        drawBoardBackground();
        drawGridLines();
        drawIntersections();
        drawClickAreas();

        initializeTimer();
        updateStatus();
        updateTimeLabels();
    }

    private void drawBoardBackground() {
        Rectangle background = new Rectangle(0, 0, BOARD_TOTAL_SIZE, BOARD_TOTAL_SIZE);
        background.setFill(BOARD_COLOR);
        background.setArcWidth(10);
        background.setArcHeight(10);
        boardPane.getChildren().add(background);

        Rectangle borderRect = new Rectangle(0, 0, BOARD_TOTAL_SIZE, BOARD_TOTAL_SIZE);
        borderRect.setFill(Color.TRANSPARENT);
        borderRect.setStroke(Color.rgb(139, 69, 19));
        borderRect.setStrokeWidth(6);
        borderRect.setArcWidth(10);
        borderRect.setArcHeight(10);
        boardPane.getChildren().add(borderRect);

        Rectangle gridArea = new Rectangle(GRID_AREA_X, GRID_AREA_Y, GRID_AREA_SIZE, GRID_AREA_SIZE);
        gridArea.setFill(Color.rgb(222, 184, 135));
        boardPane.getChildren().add(gridArea);
    }

    private void showOccupiedWarning() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Invalid Move");
        alert.setHeaderText(null);
        alert.setContentText("This position already has a stone.\nPlease choose an empty intersection.");
        alert.showAndWait();
    }

    private void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showGameResult() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Game Over");
        alert.setHeaderText(null);

        if (game.getWinner() == 0) {
            alert.setContentText("The game ended in a draw!\nThe board is full.");
        } else {
            String winner = game.getWinner() == 1 ? "Black ●" : "White ○";
            alert.setContentText(winner + " wins!\nCongratulations!");
        }

        alert.showAndWait();
    }

    /**
     * Shows time-out alert when player's time expires.
     */
    private void showTimeOutAlert(int timeoutPlayer) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Time's Up");
            alert.setHeaderText("Player " + timeoutPlayer + " ran out of time!");

            String winner = timeoutPlayer == 1 ? "White ○" : "Black ●";
            alert.setContentText(winner + " wins by timeout!");
            alert.showAndWait();
        });
    }

    private void showAboutDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About Gomoku");
        alert.setHeaderText("Gomoku (Five in a Row)");
        alert.setContentText("Version: 2.0\n\n" +
                "A traditional strategy game where players\n" +
                "alternate placing stones on intersections\n" +
                "to get five in a row.\n\n" +
                "Board: 20×20 intersections\n" +
                "Time limit: 30 seconds per move");
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
