# Wordle

Run the project from the folder with:

```bash
./mvnw clean javafx:run
```

On Windows, use:

```bash
mvnw.cmd clean javafx:run
```

This project is a JavaFX version of Wordle where the player gets 6 tries to guess a hidden 5-letter word. I built it by separating the game rules from the screen, which made the code easier to organize and explain. The `WordleGame` class handles the secret word, guess checking, and win/loss rules, while `MainApp` handles the user interface, buttons, tiles, and keyboard display. That split also made debugging easier because I could focus on either the logic or the visual part without mixing both together.

When the app starts, it picks a random secret word and shows an empty 6 by 5 board. The player can type in the text box or click the on-screen keyboard. I added input cleanup so only letters are accepted, the word stays uppercase, and the guess never goes past 5 letters. When a guess is submitted, the game compares each letter against the secret word in two passes. First it marks exact matches as correct. Then it checks the remaining letters to see if they are in the word somewhere else. That second step matters because Wordle has to handle repeated letters correctly, so a letter should not be marked twice just because it appears more than once.

The board updates after each guess by coloring the tiles green, yellow, or gray. The keyboard also updates so the player can keep track of which letters have already been used. I made sure the keyboard does not downgrade a letter if it was already found in a better state. For example, if a letter is already green, later guesses should not change it back to yellow or gray. That part needed a little extra care because the on-screen keyboard is supposed to show the best result so far, not just the latest one.

One of the main things I had to set up was the word list. Instead of hard-coding every possible guess into the game, I loaded a text file of valid 5-letter words from the resources folder. That made the game feel more complete and realistic, and it also kept the code cleaner. I added a fallback list too, so the game still works if the resource file is missing for some reason. Overall, the project was a good exercise in combining interface code, game logic, and data handling into one working program.
