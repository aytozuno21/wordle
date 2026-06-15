package com.wordle;

import atlantafx.base.theme.PrimerDark;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.stage.Stage;

import java.util.Locale;

/**
 * Main JavaFX application for the Wordle game.
 *
 * This class builds a simple desktop interface:
 * - a 6 by 5 grid for guesses
 * - a text box and button for entering guesses
 * - a keyboard-style display that colors used letters
 * - a new game button
 */
public class MainApp extends Application {
    private static final int TILE_SIZE = 64;
    private static final int KEY_WIDTH = 44;
    private static final int KEY_HEIGHT = 48;
    private static final int CONTROL_HEIGHT = 52;
    private static final double ICON_SCALE = 0.78;

    // icon paths -> makes the buttons look nicer
    private static final String LUCIDE_ENTER_1 = "M20 4v7a4 4 0 0 1-4 4H4";
    private static final String LUCIDE_ENTER_2 = "m9 10-5 5 5 5";
    private static final String LUCIDE_DELETE_1 = "M10 5a2 2 0 0 0-1.344.519l-6.328 5.74a1 1 0 0 0 0 1.481l6.328 5.741A2 2 0 0 0 10 19h10a2 2 0 0 0 2-2V7a2 2 0 0 0-2-2z";
    private static final String LUCIDE_DELETE_2 = "m12 9 6 6";
    private static final String LUCIDE_DELETE_3 = "m18 9-6 6";
    private static final String LUCIDE_ROTATE_1 = "M3 12a9 9 0 1 0 9-9 9.75 9.75 0 0 0-6.74 2.74L3 8";
    private static final String LUCIDE_ROTATE_2 = "M3 3v5h5";

    // colors -> pulled from tailwind so the ui stays consistent
    private static final String SLATE_950 = "#020617";
    private static final String SLATE_900 = "#0f172a";
    private static final String SLATE_800 = "#1e293b";
    private static final String SLATE_700 = "#334155";
    private static final String SLATE_500 = "#64748b";
    private static final String SLATE_200 = "#e2e8f0";
    private static final String SLATE_50 = "#f8fafc";
    private static final String GREEN_600 = "#16a34a";
    private static final String AMBER_500 = "#f59e0b";

    private final WordleGame game = new WordleGame();
    private final Label[][] tiles = new Label[WordleGame.MAX_GUESSES][WordleGame.WORD_LENGTH];

    private Stage stage;
    private Label statusLabel;
    private TextField guessField;
    private Button guessButton;
    private Button keyboardEnterButton;
    private VBox keyboard;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());

        stage = primaryStage;
        stage.setTitle("Wordle");
        stage.setMinWidth(620);
        stage.setMinHeight(760);
        stage.setScene(buildScene());
        stage.show();
    }

    private Scene buildScene() {
        // top section -> title, instructions, and status
        Label title = new Label("Wordle");
        title.setStyle("-fx-font-size: 38px; -fx-font-weight: bold; -fx-text-fill: " + SLATE_50 + ";");

        Label subtitle = new Label("Guess the hidden 5-letter word in 6 tries.");
        subtitle.setStyle("-fx-text-fill: " + SLATE_200 + ";");

        statusLabel = new Label("Type a 5-letter word to begin.");
        statusLabel.setStyle("-fx-font-size: 15px; -fx-text-fill: " + SLATE_200 + ";");

        // game area -> tiles plus clickable keyboard
        GridPane board = buildBoard();
        keyboard = buildKeyboard();

        guessField = new TextField();
        guessField.setPromptText("Enter guess");
        guessField.setMaxWidth(240);
        guessField.setMinHeight(CONTROL_HEIGHT);
        guessField.setPrefHeight(CONTROL_HEIGHT);
        guessField.setMaxHeight(CONTROL_HEIGHT);
        guessField.setStyle("-fx-font-size: 18px;"
                + "-fx-background-color: " + SLATE_900 + ";"
                + "-fx-text-fill: " + SLATE_50 + ";"
                + "-fx-border-color: " + SLATE_700 + ";"
                + "-fx-border-radius: 6;"
                + "-fx-background-radius: 6;");
        guessField.textProperty().addListener((obs, oldText, newText) -> keepGuessFieldWordleShaped(newText));
        // enter key -> submits from the textbox
        guessField.setOnAction(event -> submitGuess());

        guessButton = new Button("Guess");
        guessButton.setMinHeight(CONTROL_HEIGHT);
        guessButton.setPrefHeight(CONTROL_HEIGHT);
        guessButton.setMaxHeight(CONTROL_HEIGHT);
        guessButton.setStyle(actionButtonStyle(GREEN_600));
        guessButton.setOnAction(event -> submitGuess());

        Button newGameButton = new Button("New Game");
        newGameButton.setMinHeight(CONTROL_HEIGHT);
        newGameButton.setPrefHeight(CONTROL_HEIGHT);
        newGameButton.setMaxHeight(CONTROL_HEIGHT);
        newGameButton.setGraphic(lucideIcon(LUCIDE_ROTATE_1, LUCIDE_ROTATE_2));
        newGameButton.setGraphicTextGap(8);
        newGameButton.setStyle(actionButtonStyle(SLATE_700));
        newGameButton.setOnAction(event -> startNewRound());

        HBox controls = new HBox(10, guessField, guessButton, newGameButton);
        controls.setAlignment(Pos.CENTER);
        HBox.setHgrow(guessField, Priority.NEVER);

        VBox center = new VBox(18, title, subtitle, statusLabel, board, controls, keyboard);
        center.setAlignment(Pos.CENTER);
        center.setPadding(new Insets(26));

        BorderPane root = new BorderPane(center);
        root.setStyle("-fx-background-color: " + SLATE_950 + ";");
        return new Scene(root);
    }

    private GridPane buildBoard() {
        // 6 rows x 5 columns like the real game
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(8);
        grid.setVgap(8);

        for (int row = 0; row < WordleGame.MAX_GUESSES; row++) {
            for (int col = 0; col < WordleGame.WORD_LENGTH; col++) {
                Label tile = new Label("");
                tile.setMinSize(TILE_SIZE, TILE_SIZE);
                tile.setAlignment(Pos.CENTER);
                tile.setStyle(tileStyle(SLATE_900, SLATE_700));
                tiles[row][col] = tile;
                grid.add(tile, col, row);
            }
        }
        return grid;
    }

    private VBox buildKeyboard() {
        // standard qwerty layout, split into 3 rows
        VBox rows = new VBox(8);
        rows.setAlignment(Pos.CENTER);

        rows.getChildren().add(buildKeyboardRow("QWERTYUIOP"));
        rows.getChildren().add(buildKeyboardRow("ASDFGHJKL"));
        rows.getChildren().add(buildBottomKeyboardRow());
        return rows;
    }

    private HBox buildKeyboardRow(String letters) {
        HBox row = new HBox(6);
        row.setAlignment(Pos.CENTER);
        for (int i = 0; i < letters.length(); i++) {
            row.getChildren().add(createLetterKey(letters.charAt(i)));
        }
        return row;
    }

    private HBox buildBottomKeyboardRow() {
        // bottom row gets enter and delete
        HBox row = new HBox(6);
        row.setAlignment(Pos.CENTER);
        keyboardEnterButton = createCommandKey("Enter", 92, this::submitGuess,
                lucideIcon(LUCIDE_ENTER_1, LUCIDE_ENTER_2));
        row.getChildren().add(keyboardEnterButton);
        for (char letter : "ZXCVBNM".toCharArray()) {
            row.getChildren().add(createLetterKey(letter));
        }
        row.getChildren().add(createCommandKey("Del", 92, this::deleteLastLetter,
                lucideIcon(LUCIDE_DELETE_1, LUCIDE_DELETE_2, LUCIDE_DELETE_3)));
        return row;
    }

    private Button createLetterKey(char letter) {
        Button key = createCommandKey(String.valueOf(letter), KEY_WIDTH, () -> addLetter(letter));
        key.setId("key-" + letter);
        return key;
    }

    private Button createCommandKey(String text, int width, Runnable action) {
        return createCommandKey(text, width, action, null);
    }

    private Button createCommandKey(String text, int width, Runnable action, Node icon) {
        Button key = new Button(text);
        key.setMinSize(width, KEY_HEIGHT);
        key.setPrefSize(width, KEY_HEIGHT);
        key.setMaxSize(width, KEY_HEIGHT);
        key.setAlignment(Pos.CENTER);
        key.setFocusTraversable(false);
        key.setGraphic(icon);
        key.setGraphicTextGap(6);
        key.setStyle(keyStyle(SLATE_800));
        key.setOnAction(event -> action.run());
        return key;
    }

    private Node lucideIcon(String... paths) {
        // draw the svg paths as one little icon
        Group group = new Group();
        for (String pathData : paths) {
            SVGPath path = new SVGPath();
            path.setContent(pathData);
            path.setFill(null);
            path.setStroke(Paint.valueOf(SLATE_50));
            path.setStrokeWidth(2);
            path.setStrokeLineCap(StrokeLineCap.ROUND);
            path.setStrokeLineJoin(StrokeLineJoin.ROUND);
            group.getChildren().add(path);
        }
        group.setScaleX(ICON_SCALE);
        group.setScaleY(ICON_SCALE);
        return group;
    }

    private void addLetter(char letter) {
        // stop extra letters once the row is full
        if (guessField.isDisabled() || guessField.getText().length() >= WordleGame.WORD_LENGTH) {
            return;
        }
        guessField.appendText(String.valueOf(letter));
        guessField.requestFocus();
    }

    private void deleteLastLetter() {
        String text = guessField.getText();
        // nothing to delete if the game is over or the box is empty
        if (guessField.isDisabled() || text.isEmpty()) {
            return;
        }
        guessField.setText(text.substring(0, text.length() - 1));
        guessField.positionCaret(guessField.getText().length());
        guessField.requestFocus();
    }

    private void keepGuessFieldWordleShaped(String newText) {
        // keep the input like wordle: letters only, max 5, all caps
        String cleaned = newText.replaceAll("[^a-zA-Z]", "").toUpperCase(Locale.ROOT);
        if (cleaned.length() > WordleGame.WORD_LENGTH) {
            cleaned = cleaned.substring(0, WordleGame.WORD_LENGTH);
        }
        if (!newText.equals(cleaned)) {
            guessField.setText(cleaned);
            guessField.positionCaret(cleaned.length());
        }
    }

    private void submitGuess() {
        // no more guesses after the round ends
        if (game.isGameOver()) {
            return;
        }

        String guess = game.normalize(guessField.getText());
        if (!game.hasFiveLetters(guess)) {
            showError("Invalid guess", "Please enter exactly 5 letters.");
            return;
        }
        if (!game.isKnownWord(guess)) {
            showError("Invalid word", "That word is not in this game's English word list.");
            return;
        }

        try {
            // save the row before submitting, because the game counts it after
            int row = game.getGuessesUsed();
            WordleGame.LetterResult[] result = game.submitGuess(guess);
            paintGuess(row, guess, result);
            guessField.clear();

            if (game.isWon(guess)) {
                statusLabel.setText("You won! The word was " + game.getSecretWord() + ".");
                setGuessingDisabled(true);
            } else if (game.isGameOver()) {
                statusLabel.setText("Game over. The word was " + game.getSecretWord() + ".");
                setGuessingDisabled(true);
            } else {
                statusLabel.setText("Nice try. Guesses left: " + (WordleGame.MAX_GUESSES - game.getGuessesUsed()));
            }
        } catch (IllegalStateException ex) {
            showError("Round finished", ex.getMessage());
        }
    }

    private void paintGuess(int row, String guess, WordleGame.LetterResult[] result) {
        // fill the row and update the matching keyboard keys
        for (int col = 0; col < WordleGame.WORD_LENGTH; col++) {
            String letter = String.valueOf(guess.charAt(col));
            String color = colorFor(result[col]);
            tiles[row][col].setText(letter);
            tiles[row][col].setStyle(tileStyle(color, color));
            paintKeyboardLetter(letter.charAt(0), result[col]);
        }
    }

    private void paintKeyboardLetter(char letter, WordleGame.LetterResult result) {
        Button key = (Button) keyboard.lookup("#key-" + letter);
        if (key == null) {
            return;
        }

        // keep the best color so far for each key
        String currentStyle = key.getStyle();
        if (currentStyle.contains(GREEN_600)) {
            return;
        }
        if (currentStyle.contains(AMBER_500) && result == WordleGame.LetterResult.ABSENT) {
            return;
        }
        key.setStyle(keyStyle(colorFor(result)));
    }

    private String colorFor(WordleGame.LetterResult result) {
        // same colors used by both board and keyboard
        return switch (result) {
            case CORRECT -> GREEN_600;
            case PRESENT -> AMBER_500;
            case ABSENT -> SLATE_500;
        };
    }

    private String tileStyle(String background, String border) {
        // tile css in one place so reset is easy
        return "-fx-background-color: " + background + ";"
                + "-fx-border-color: " + border + ";"
                + "-fx-border-width: 2;"
                + "-fx-text-fill: " + SLATE_50 + ";"
                + "-fx-font-size: 28px;"
                + "-fx-font-weight: bold;"
                + "-fx-background-radius: 6;"
                + "-fx-border-radius: 6;";
    }

    private String keyStyle(String background) {
        // keyboard button css stays simple and reusable
        return "-fx-background-color: " + background + ";"
                + "-fx-text-fill: " + SLATE_50 + ";"
                + "-fx-font-size: 16px;"
                + "-fx-font-weight: bold;"
                + "-fx-background-radius: 6;"
                + "-fx-border-color: " + SLATE_700 + ";"
                + "-fx-border-radius: 6;"
                + "-fx-padding: 0;";
    }

    private String actionButtonStyle(String background) {
        // used for guess, new game, and similar buttons
        return "-fx-background-color: " + background + ";"
                + "-fx-text-fill: " + SLATE_50 + ";"
                + "-fx-font-weight: bold;"
                + "-fx-background-radius: 6;"
                + "-fx-padding: 0 18 0 18;";
    }

    private void startNewRound() {
        // reset everything for a fresh round
        game.startNewGame();
        statusLabel.setText("New word chosen. Guess a 5-letter word.");
        guessField.clear();
        setGuessingDisabled(false);
        guessField.requestFocus();

        for (int row = 0; row < WordleGame.MAX_GUESSES; row++) {
            for (int col = 0; col < WordleGame.WORD_LENGTH; col++) {
                tiles[row][col].setText("");
                tiles[row][col].setStyle(tileStyle(SLATE_900, SLATE_700));
            }
        }

        // keyboard goes back to the default look
        keyboard.lookupAll(".button").forEach(node -> node.setStyle(keyStyle(SLATE_800)));
    }

    private void setGuessingDisabled(boolean disabled) {
        // lock the main input controls when the round ends
        guessField.setDisable(disabled);
        guessButton.setDisable(disabled);
        keyboardEnterButton.setDisable(disabled);
    }

    private void showError(String title, String message) {
        // simple popup for bad input or a finished round
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.initOwner(stage);
        alert.getDialogPane().setStyle("-fx-background-color: " + SLATE_900 + ";");
        alert.showAndWait();
    }
}
