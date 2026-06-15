package com.wordle;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

/**
 * Contains the game rules for a Wordle-style word guessing game.
 *
 * Keeping the rules separate from the JavaFX screen makes the project easier to
 * explain: MainApp draws the interface, and this class decides whether guesses
 * are valid, correct, or partially correct.
 */
public class WordleGame {
    public static final int WORD_LENGTH = 5;
    public static final int MAX_GUESSES = 6;

    // one tile's result
    public enum LetterResult {
        CORRECT,
        PRESENT,
        ABSENT
    }

    private static final String WORD_LIST_RESOURCE = "/com/wordle/valid-wordle-words.txt";
    private static final List<String> WORDS = loadWords();
    private static final Set<String> WORD_SET = Set.copyOf(WORDS);

    private final Random random = new Random();
    private String secretWord;
    private int guessesUsed;
    private boolean gameOver;

    public WordleGame() {
        startNewGame();
    }

    /**
     * Starts a fresh game by choosing a random secret word and clearing progress.
     */
    public void startNewGame() {
        // pick a new hidden word and clear the round state
        secretWord = WORDS.get(random.nextInt(WORDS.size()));
        guessesUsed = 0;
        gameOver = false;
    }

    /**
     * Checks one guess and returns one color/result per letter.
     *
     * The two-pass algorithm matches Wordle's behavior with duplicate letters:
     * first mark exact matches, then mark present letters only while copies of
     * that letter remain unused in the secret word.
     */
    public LetterResult[] submitGuess(String rawGuess) {
        String guess = normalize(rawGuess);
        if (!hasFiveLetters(guess)) {
            throw new IllegalArgumentException("Enter a 5-letter word.");
        }
        if (!isKnownWord(guess)) {
            throw new IllegalArgumentException("That word is not in this game's English word list.");
        }
        if (gameOver) {
            throw new IllegalStateException("The game is already over. Start a new round.");
        }

        // first mark exact matches, then handle the leftover letters
        LetterResult[] result = new LetterResult[WORD_LENGTH];
        Arrays.fill(result, LetterResult.ABSENT);
        boolean[] secretLetterUsed = new boolean[WORD_LENGTH];

        for (int i = 0; i < WORD_LENGTH; i++) {
            if (guess.charAt(i) == secretWord.charAt(i)) {
                result[i] = LetterResult.CORRECT;
                secretLetterUsed[i] = true;
            }
        }

        for (int guessIndex = 0; guessIndex < WORD_LENGTH; guessIndex++) {
            if (result[guessIndex] == LetterResult.CORRECT) {
                continue;
            }
            for (int secretIndex = 0; secretIndex < WORD_LENGTH; secretIndex++) {
                if (!secretLetterUsed[secretIndex] && guess.charAt(guessIndex) == secretWord.charAt(secretIndex)) {
                    result[guessIndex] = LetterResult.PRESENT;
                    secretLetterUsed[secretIndex] = true;
                    break;
                }
            }
        }

        // one more turn is done now
        guessesUsed++;
        if (guess.equals(secretWord) || guessesUsed >= MAX_GUESSES) {
            gameOver = true;
        }
        return result;
    }

    public boolean isValidGuess(String rawGuess) {
        // quick check for ui validation
        String guess = normalize(rawGuess);
        return hasFiveLetters(guess) && isKnownWord(guess);
    }

    public boolean hasFiveLetters(String rawGuess) {
        // wordle only accepts 5 clean letters
        String guess = normalize(rawGuess);
        return guess.length() == WORD_LENGTH && guess.chars().allMatch(Character::isLetter);
    }

    public boolean isKnownWord(String rawGuess) {
        // only allow words from the word list
        return WORD_SET.contains(normalize(rawGuess));
    }

    public boolean isWon(String rawGuess) {
        // winner if the guess matches the secret word
        return normalize(rawGuess).equals(secretWord);
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public int getGuessesUsed() {
        return guessesUsed;
    }

    public String getSecretWord() {
        return secretWord;
    }

    public List<String> getWords() {
        // copy so callers can't mess with the real list
        return new ArrayList<>(WORDS);
    }

    public String normalize(String guess) {
        // null input turns into an empty string
        if (guess == null) {
            return "";
        }
        return guess.trim().toUpperCase(Locale.ROOT);
    }

    private static List<String> loadWords() {
        try (InputStream input = WordleGame.class.getResourceAsStream(WORD_LIST_RESOURCE)) {
            if (input == null) {
                return fallbackWords();
            }

            // linked set keeps order and skips duplicates
            LinkedHashSet<String> words = new LinkedHashSet<>();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String word = line.trim().toUpperCase(Locale.ROOT);
                    if (word.length() == WORD_LENGTH && word.chars().allMatch(Character::isLetter)) {
                        words.add(word);
                    }
                }
            }

            if (words.isEmpty()) {
                return fallbackWords();
            }
            return new ArrayList<>(words);
        } catch (IOException ex) {
            return fallbackWords();
        }
    }

    private static List<String> fallbackWords() {
        // backup list in case the resource file is missing
        return List.of("APPLE", "BRAVE", "CHAIR", "DREAM", "EARTH", "HELLO", "SLATE");
    }
}
