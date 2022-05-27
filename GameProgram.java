// May 26, 2022

package com.avromi;

import java.util.*;

public class GameProgram {
    public static void main(String[] args) {
        Scanner kb = new Scanner(System.in);  // scanner for input
        boolean playAgain;  // flag to play game again or not

        displayStartGameMessage();

        do {  // play game and continue playing additional games whilst user wants to
            String input;  // to hold user input
            // get game difficulty from user
            System.out.print("Enter the game difficulty. Easy, Medium, or Hard (E/M/H): ");
            input = kb.nextLine().toUpperCase();
            char choice = input.charAt(0);
            while (!(choice == 'E' || choice == 'M' || choice == 'H')) {
                System.out.print("Enter the game difficulty. Easy, Medium, or Hard (E/M/H): ");
                input = kb.nextLine().toUpperCase();
                choice = input.charAt(0);
            }
            GameDifficulty gameDifficulty = choice == 'E' ?  // set game difficulty based on user input
                    GameDifficulty.EASY : choice == 'M' ? GameDifficulty.MEDIUM : GameDifficulty.HARD;

            OthelloBoard board = new OthelloBoard(gameDifficulty);  // create new OthelloBoard for this game
            int userSelectedRow, userSelectedCol;  // to hold row + col selections when user places piece

            System.out.println("Here is the state of the board now:\n" + board);
            while (board.isSomeLegalMoveLeft()) {  // run game whilst both players have legal moves
                if (board.isSomeLegalMoveLeft(Player.USER)) {  // if user has a legal move on the board then let them go
                    // get and place piece from user
                    displayPromptForNextUserMove();
                    input = kb.nextLine();
                    int[] rowAndCol = convertUserLocationToIntegerIndex(input);  // get actual indexes from user input
                    userSelectedRow = rowAndCol[0];
                    userSelectedCol = rowAndCol[1];
                    while (!board.isLegalMove(userSelectedRow, userSelectedCol, board.getUSER_SYMBOL())) {
                        // while illegal move or bad number entered - ask again
                        System.out.println("That is an invalid location. ");
                        displayPromptForNextUserMove();
                        input = kb.nextLine();
                        rowAndCol = convertUserLocationToIntegerIndex(input);
                        userSelectedRow = rowAndCol[0];
                        userSelectedCol = rowAndCol[1];
                    }
                    // run user's turn and display number of pieces flipped
                    System.out.println("\nYou flipped " +
                            board.runTurn(userSelectedRow, userSelectedCol, board.getUSER_SYMBOL()) + " pieces this turn. ");
                    System.out.println("Here's the state of the board now: ");
                    System.out.println(board.toString(true));
                } else {  // if user doesn't have a legal move on board then notify user
                    System.out.println("There are no legal moves right now for you - ");
                }
                if (board.isSomeLegalMoveLeft(Player.PROGRAM)) {  // if program has a legal move on the board then let them go
                    System.out.println("Program's turn, press enter to have it go: ");
                    kb.nextLine();
                    System.out.println("Program went and flipped " + board.programsTurn() + " pieces.");
                    System.out.println(board.toString(true));
                    System.out.println("Press enter to continue.");
                    kb.nextLine();
                } else {  // if it doesn't then notify user
                    System.out.println("There are no legal moves right now for the program - ");
                }
                displayPiecesOnBoardCount(board);
            }

            Player winner = board.endGameAndGetWinner();  // end game and display end game message
            if (winner.equals(Player.USER)) {
                System.out.println("You won the game!");
                System.out.println("You had " + board.getUserPiecesOnBoard() + " pieces on the board.");
            } else if (winner.equals(Player.PROGRAM)) {
                System.out.println("You lost the game!");
                System.out.println("The program had " + board.getProgramPiecesOnBoard() + " pieces on the board. " +
                        "You only had " + board.getUserPiecesOnBoard() + " pieces on the board");
            } else {
                System.out.println("You tied!");
                System.out.println("You both had " + board.getUserPiecesOnBoard() + " pieces on the board.");
            }

            System.out.print("Would you like to play another game? (Y/N)");
            playAgain = kb.nextLine().toUpperCase().charAt(0) == 'Y';
        } while (playAgain);

        System.out.println("\nTotals\n-----------------------------\n");
        System.out.println("You played " + OthelloBoard.getNumUserGamesPlayed() + " game(s) today.");
        System.out.println("You won " + OthelloBoard.getUserWins() + " game(s) today, which is " +
                (OthelloBoard.getPercentageOfUserWins() * 100) + "% of the game(s) you played");
        System.out.println("You lost " + OthelloBoard.getUserLosses() + " game(s) today, which is " +
                (OthelloBoard.getPercentageOfUserLosses() * 100) + "% of the game(s) you played");
        System.out.println("You tied " + OthelloBoard.getUserTies() + " game(s) today, which is " +
                (OthelloBoard.getPercentageOfUserTies() * 100) + "% of the game(s) you played");
    }

    private static void displayStartGameMessage() {
        System.out.println("\nWelcome to Othello!");
        System.out.println("In this game the goal is to have the most pieces of your symbol/color on the board at " +
                "the end of the game.");
        System.out.println("Your symbol is: \"x\". The last piece placed is always shown with a capital letter and " +
                "slanted lines for visibility.");
        System.out.println("Let's start!");
    }

    private static void displayPiecesOnBoardCount(OthelloBoard board) {
        System.out.println("Board totals as of now: ");
        System.out.println("You have " + board.getUserPiecesOnBoard() + " pieces on the board");
        System.out.println("Program has " + board.getProgramPiecesOnBoard() + " pieces on the board\n");
    }

    private static void displayPromptForNextUserMove() {
        System.out.print("It's your turn! \nEnter the row and column to " +
                "place your piece (For example: \"D, 3\"): ");
    }

    private static int[] convertUserLocationToIntegerIndex(String userInput) {
        if (userInput.length() < 2) {
            // if just enter key hit, or only 1 digit entered then return -1 for each index to indicate error
            return new int[]{-1, -1};
        }
        // else, get correct indexes:
        // take string from user input and return int array with the row and column as integers and adjusted to match index
        char[] rowAndColumn = userInput.toUpperCase().toCharArray();  // make array of user's input.
        char firstCharacter = rowAndColumn[0];  // get first and last character inputted
        char lastCharacter = rowAndColumn[rowAndColumn.length - 1];
        // check each character's type -
        boolean firstCharacterIsDigit = Character.isDigit(firstCharacter);
        boolean lastCharacterIsDigit = Character.isDigit(lastCharacter);
        boolean firstCharacterIsLetter = Character.isLetter(firstCharacter);
        boolean lastCharacterIsLetter = Character.isLetter(lastCharacter);

        if ((firstCharacterIsDigit && lastCharacterIsDigit) ||  // if 2 digits or 2 letters entered then return -1 to indicate error
                (firstCharacterIsLetter && lastCharacterIsLetter)) {
            return new int[]{-1, -1};
        }

        // column headers
        ArrayList<Character> columnHeaders = new ArrayList<>(Arrays.asList('A', 'B', 'C', 'D', 'E', 'F', 'G', 'H'));
        // get row index from first character if it is a digit, otherwise from last character
        int rowIndex = firstCharacterIsDigit ? Character.getNumericValue(firstCharacter) - 1 :
                Character.getNumericValue(lastCharacter) - 1;
        // get corresponding letters' column index from first character if it is a letter, otherwise from last character
        int columnIndex = Character.isLetter(firstCharacter) ? columnHeaders.indexOf(firstCharacter) :
                columnHeaders.indexOf(lastCharacter);
        int[] indexes = {rowIndex, columnIndex};
        return indexes;
    }
}