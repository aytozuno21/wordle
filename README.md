# Wordle


https://github.com/user-attachments/assets/f6daf83c-a453-4cae-a580-22b35caf84fb


Run the project from the folder with:

```bash
./mvnw clean javafx:run
```

On Windows, use:

```bash
mvnw.cmd clean javafx:run
```

This project is a JavaFX version of Wordle where the player gets 6 tries to guess a hidden 5-letter word. We built it by separating the game rules from the screen, which made the code easier to organize and explain. The `WordleGame` class handles the secret word, guess checking, and win/loss rules, while `MainApp` handles the user interface, buttons, tiles, and keyboard display. We also used AtlantaFX to make the app look cleaner and to make it easier to create reusable UI components instead of styling everything from scratch.

When the app starts, it picks a random secret word and shows an empty 6 by 5 board. The player can type in the text box or click the on-screen keyboard. We added input cleanup so only letters are accepted, the word stays uppercase, and the guess never goes past 5 letters. When a guess is submitted, the game compares each letter against the secret word in two passes. First, it marks exact matches as correct. Then, it checks the remaining letters to see if they are in the word somewhere else. That second step is important because Wordle has to handle repeated letters properly, so a letter should not be marked twice just because it appears more than once.

The board updates after each guess by coloring the tiles green, yellow, or gray. The keyboard also updates so the player can keep track of which letters have already been used. We made sure the keyboard does not downgrade a letter if it was already found in a better state. For example, if a letter is already green, later guesses should not change it back to yellow or gray. That part needed a bit of extra care because the on-screen keyboard should show the best result so far, not just the latest guess.

One of the main things we had to set up was the word list. Instead of hard-coding every possible guess into the game, we used a file called `valid-wordle-words.txt`, which contains a list of all valid Wordle words. The game pulls the random secret word from this file, and it also uses the same file to check whether the player’s input is a valid word before accepting the guess. That made the game feel more complete and realistic, and it also kept the code cleaner. We added a fallback list too, so the game still works if the resource file is missing for some reason.
