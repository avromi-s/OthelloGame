// May 26, 2022

import java.lang.StringBuilder;

public class OthelloBoard {
    private final String USER_SYMBOL = "x";  // string of symbol representing user's piece on board
    private final String PROGRAM_SYMBOL = "o";  // program's symbol
    private final String EMPTY_SPACE_SYMBOL = "\s";  // string for empty space

    private final int BOARD_SIZE = 8;
    private final String[][] BOARD = new String[BOARD_SIZE][BOARD_SIZE];  // 2D array to hold board with pieces on it

    private int lastMoveRow = -1;  // hold indexes of last moves
    private int lastMoveCol = -1;
    private int movesToLookAhead;  // moves for program to look ahead (game's difficulty)

    private static int gamesPlayed = 0;
    private static int userWins = 0;
    private static int userLosses = 0;
    private static int userTies = 0;

    public OthelloBoard() {  // default constructor
        gamesPlayed++;
        setupBoard();
    }

    public OthelloBoard(OthelloBoard othelloBoardToCopy) {
        /* copy constructor. doesn't increment boards used.
        copy 2d array of board and other appropriate variables. all other fields are final or static.
        doesn't increment games played as this is used for copying, not creating new game */
        for (int row = 0; row < othelloBoardToCopy.BOARD.length; row++) {  // copy board's contents
            for (int column = 0; column < othelloBoardToCopy.BOARD[row].length; column++) {
                this.BOARD[row][column] = othelloBoardToCopy.BOARD[row][column];
            }
        }
        this.lastMoveRow = othelloBoardToCopy.lastMoveRow;  // copy other fields
        this.lastMoveCol = othelloBoardToCopy.lastMoveCol;
        this.movesToLookAhead = othelloBoardToCopy.movesToLookAhead;
    }

    public OthelloBoard(GameDifficulty gameDifficulty) {  // constructor with option to set game difficulty
        this();  // call default constructor for setup
        movesToLookAhead = getMovesToLookAhead(gameDifficulty);  // set moves to look ahead based on game difficulty
    }

    public String getUSER_SYMBOL() {  // return symbol used for user
        return USER_SYMBOL;
    }

    public String getPROGRAM_SYMBOL() {  // return symbol used for program
        return PROGRAM_SYMBOL;
    }

    public static int getUserWins() {  // get total wins
        return userWins;
    }

    public static int getUserLosses() {  // get total losses
        return userLosses;
    }

    public static int getUserTies() {  // get total playerTies
        return userTies;
    }

    public static int getNumUserGamesPlayed() {  // get total games played
        return gamesPlayed;
    }

    public static double getPercentageOfUserWins() {  // get percentage of wins out of total games played
        return (double) userWins / gamesPlayed;
    }

    public static double getPercentageOfUserLosses() {  // get percentage of losses out of total games played
        return (double) userLosses / gamesPlayed;
    }

    public static double getPercentageOfUserTies() {  // get percentage of playerTies out of total games played
        return (double) userTies / gamesPlayed;
    }

    public Player endGameAndGetWinner() {  // ends game (adds game to total wins/losses/ties) and returns winner
        int userPiecesOnBoard = getUserPiecesOnBoard();  // get total pieces on board for user & program
        int programPiecesOnBoard = getProgramPiecesOnBoard();
        if (userPiecesOnBoard > programPiecesOnBoard) {  // if user has more pieces, then user wins
            userWins++;
            return Player.USER;
        } else if (programPiecesOnBoard > userPiecesOnBoard) {
            userLosses++;
            return Player.PROGRAM;
        } else {
            userTies++;
            return Player.TIE;
        }
    }

    public int getUserPiecesOnBoard() {  // get number of user's pieces on board
        return getPiecesOnBoardCount(Player.USER);
    }

    public int getProgramPiecesOnBoard() {  // get number of program's pieces on board
        return getPiecesOnBoardCount(Player.PROGRAM);
    }

    private int getPiecesOnBoardCount(Player player) {
        /* get number of pieces on board from player indicated */
        String playerToTest = getPlayerString(player);  // assign appropriate string to test, based on indicated player
        int piecesOnBoard = 0;
        for (String[] row : BOARD) {
            for (String column : row) {
                if (column.equals(playerToTest)) {
                    piecesOnBoard++;
                }
            }
        }
        return piecesOnBoard;
    }

    public int programsTurn() {
        /* places piece for program based on difficulty set
        returns number of pieces flipped. so if no legal move then returns 0 */
        int[] bestMoveFound = nextBestMove(PROGRAM_SYMBOL, USER_SYMBOL, movesToLookAhead);  // get best move for difficulty
        int piecesFlipped = runTurn(bestMoveFound[0], bestMoveFound[1], PROGRAM_SYMBOL);  // run turn
        lastMoveRow = bestMoveFound[0];  // update program's last move indexes
        lastMoveCol = bestMoveFound[1];
        return piecesFlipped;
    }

    public int runTurn(int rowIndexStart, int colIndexStart, String playerSymbol) {
        // overloaded method to automatically run turn on same instance of OthelloBoard
        return runTurn(this, rowIndexStart, colIndexStart, playerSymbol);
    }

    private int runTurn(OthelloBoard othelloBoard, int rowIndexStart, int colIndexStart, String playerSymbol) {
        /* Flip pieces in all directions where valid.
         * playerSymbol - symbol (piece type/color) of player who's placing the piece this turn
         * returns number of pieces flipped, 0 if no pieces flipped (i.e., illegal move) */
        int piecesFlipped = 0;
        int[] directions = {-1, 0, 1};  // directions to flip (angles)
        for (int rowDirection : directions) {
            for (int colDirection : directions) {
                if (rowDirection == 0 && colDirection == 0) {  // skip if both row & col direction would be 0
                    continue;
                }
                piecesFlipped += flipPiecesSingleDirection(othelloBoard, rowIndexStart, colIndexStart, playerSymbol,
                        rowDirection, colDirection);
            }
        }
        if (piecesFlipped > 0) {  // if move went, update the latest move's indexes
            othelloBoard.lastMoveRow = rowIndexStart;
            othelloBoard.lastMoveCol = colIndexStart;
        }
        return piecesFlipped;
    }

    private int flipPiecesSingleDirection(OthelloBoard othelloBoard, int rowIndexStart, int colIndexStart,
                                          String playerSymbol, int rowDirection, int colDirection) {
        /* flip pieces on array board passed in: pass in actual board to flip pieces or pass copy to see if legal or
         * how many flips would happen
         * Flip valid pieces starting from given row & col indexes in direction given */
        int piecesFlipped = 0;
        boolean hitOpponentPiece = false;
        boolean hitEmptySpace = false;
        if (rowIndexStart < 0 || colIndexStart < 0 || rowIndexStart >= othelloBoard.BOARD_SIZE  // if illegal move then return 0
                || colIndexStart >= othelloBoard.BOARD_SIZE) {
            return piecesFlipped;
        }

        int currentRowPosition = rowIndexStart + rowDirection;  // start from place after given indexes
        int currentColPosition = colIndexStart + colDirection;
        boolean inRange = !(currentRowPosition < 0 || currentRowPosition >= BOARD_SIZE ||
                currentColPosition < 0 || currentColPosition >= BOARD_SIZE);  // flag to check if position is in range
        while (inRange & !hitEmptySpace) {  // while position is in array's bounds and didn't hit empty space
            if (othelloBoard.BOARD[currentRowPosition][currentColPosition].equals(playerSymbol)) {  // if hit players own piece
                if (hitOpponentPiece) {  // flip pieces in between if had hit opponents piece in between
                    piecesFlipped = -2;  // subtract beginning pieces as they're not being technically flipped, just placed
                    while (!(currentRowPosition == rowIndexStart - rowDirection &&
                            currentColPosition == colIndexStart - colDirection)) {
                        // flip pieces until and including place where original piece was placed (places that pieces also)
                        placePiece(othelloBoard, currentRowPosition, currentColPosition, playerSymbol);
                        piecesFlipped++;
                        currentColPosition -= colDirection;  // go backwards towards piece placed
                        currentRowPosition -= rowDirection;
                    }
                }
                break;  // end loop if hadn't hit opponents' piece yet - no pieces to flip, or if pieces were flipped
            } else {  // if hit opponents piece or empty space, set hit opponent flag and continue in direction given
                hitEmptySpace = othelloBoard.BOARD[currentRowPosition][currentColPosition].equals(EMPTY_SPACE_SYMBOL);
                hitOpponentPiece = !hitEmptySpace;  // if didn't hit empty space, then that means opponent piece was hit
                currentColPosition += colDirection;
                currentRowPosition += rowDirection;
                inRange = !(currentRowPosition < 0 | currentRowPosition >= BOARD_SIZE || currentColPosition < 0 || currentColPosition >= BOARD_SIZE);  // check if next move will be in range
            }
        }
        return piecesFlipped;
    }

    public boolean isSomeLegalMoveLeft() {
        // overloaded method to check if there is any legal move remaining on this instance of the OthelloBoard
        return isSomeLegalMoveLeft(this);
    }

    public boolean isSomeLegalMoveLeft(Player playerToCheck) {
        // check if there is a legal move remaining on OthelloBoard for this player
        String playerToTest = getPlayerString(playerToCheck);
        return isSomeLegalMoveLeft(this, playerToTest);
    }

    private boolean isSomeLegalMoveLeft(OthelloBoard othelloBoard) {
        // check if there is a legal move remaining on OthelloBoard instance passed in
        return isSomeLegalMoveLeft(othelloBoard, PROGRAM_SYMBOL) ||  // if there's a legal move for either player
                isSomeLegalMoveLeft(othelloBoard, USER_SYMBOL);
    }

    private String getPlayerString(Player playerValue) {  // return string corresponding to enum value for player
        return playerValue == Player.USER ? this.USER_SYMBOL : this.PROGRAM_SYMBOL;
    }

    private boolean isSomeLegalMoveLeft(OthelloBoard othelloBoard, String playersSymbol) {
        // return true or false indicating if a legal move is present on given instance of board for given player
        for (int row = 0; row < BOARD.length; row++) {
            for (int column = 0; column < BOARD[row].length; column++) {
                if (isLegalMove(othelloBoard, row, column, playersSymbol)) {  // if legal move found return true
                    return true;
                }
            }
        }
        return false;  // if no legal move found return false (there's no legal move remaining)
    }

    public boolean isLegalMove(int row, int column, String playersSymbol) {
        // overloaded method for this instance of OthelloBoard
        return isLegalMove(this, row, column, playersSymbol);
    }

    private boolean isLegalMove(OthelloBoard othelloBoard, int row, int column, String playersSymbol) {
        // check if move is legal. does not actually flip pieces.
        return numberPiecesWouldBeFlipped(othelloBoard, row, column, playersSymbol) > 0;
    }

    private int numberPiecesWouldBeFlipped(OthelloBoard othelloBoard, int row, int column, String playersSymbol) {
        /* returns number of pieces that would be flipped with piece placed at given position.
         Does not place pieces on actual board, uses copy.
         pieces would only flip if it is a legal move so if function returns > 0, then move is legal */
        boolean outOfRange = row < 0 || row >= BOARD.length || column < 0 || column >= BOARD[0].length;  // true if position is out of array's range
        if (!outOfRange) {  // check if position is empty if it's in range
            boolean spaceEmpty = othelloBoard.BOARD[row][column].equals(EMPTY_SPACE_SYMBOL);  // is space at position empty
            if (spaceEmpty) {
                return runTurn(othelloBoard.copy(), row, column, playersSymbol);
                // if in array's range and space is empty, return number of pieces that would be flipped
            }
        }
        // otherwise, return 0 to indicate an illegal move
        return 0;
    }

    private void placePiece(int row, int column, String playersSymbol) {
        // overloaded method for this instance of OthelloBoard
        placePiece(this, row, column, playersSymbol);
    }

    private void placePiece(OthelloBoard othelloBoard, int row, int column, String playersSymbol) {
        // place piece at specified location with given player symbol.
        othelloBoard.BOARD[row][column] = playersSymbol;
    }

    private void setupBoard() {  // setup board with whitespace and first four pieces placed
        for (int row = 0; row < BOARD.length; row++) {
            for (int column = 0; column < BOARD[row].length; column++) {
                BOARD[row][column] = EMPTY_SPACE_SYMBOL;
            }
        }
        placePiece(3, 4, USER_SYMBOL);
        placePiece(4, 3, USER_SYMBOL);
        placePiece(3, 3, PROGRAM_SYMBOL);
        placePiece(4, 4, PROGRAM_SYMBOL);
    }

    @Override
    public String toString() {  // return board's current state with formatting + row & column headers, but without
        // highlighting last move
        return toString(false);
    }

    public String toString(boolean highlightLastMove) {
        /* return board's current state with formatting
        INCLUDING highlighting of last move placed */
        char[] columnHeaders = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H'};  // headers for column
        StringBuilder boardString = new StringBuilder();  // use StringBuilder for efficiency
        boardString.append("\n\s\s\s\s\s\s\s\s");
        for (int columnIndex = 0; columnIndex < columnHeaders.length; columnIndex++) {  // column headers and spacing
            boardString.append(columnHeaders[columnIndex]).append("\s\s\s");
        }
        boardString.append("\n\s\s\s\s\s\s---------------------------------\n");
        for (int row = 0; row < BOARD.length; row++) {
            boardString.append(row + 1).append("\s\s\s\s\s");  // row headers and spacing
            for (int column = 0; column < BOARD[row].length; column++) {
                /* If move is last move than format specially. if move is move before the last move then don't include
                 * standard separator between cells because it was provided by the specially formatted cell.
                 * if up to first column (0) and highlight last move is false, then add first separator for cell.
                 * or if highlight last move is true, but last move wasn't on this cell then add first separator for cell.
                 * else format normally */
                if ((!highlightLastMove && column == 0) || column == 0 && !((row == lastMoveRow) && (column == lastMoveCol))) {
                    // add beginning row '|' symbol unless it's being replaced by highlighted last move
                    boardString.append("|");
                }
                if (highlightLastMove && row == lastMoveRow && column == lastMoveCol) {
                    boardString.append("\\\s").append(BOARD[row][column].toUpperCase()).append("\s/");
                } else if (highlightLastMove && row == lastMoveRow && (column == lastMoveCol - 1)) {
                    boardString.append("\s").append(BOARD[row][column]).append("\s");
                } else {
                    boardString.append("\s").append(BOARD[row][column]).append("\s|");
                }
            }
            boardString.append("\n\s\s\s\s\s\s---------------------------------\n");
        }
        return boardString.toString();
    }

    private OthelloBoard copy() {  // return reference to copy of this instance of othelloBoard
        return new OthelloBoard(this);
    }

    public int[] nextBestMove(String playersSymbol, String opponentSymbol, int movesToLookAhead) {
        // overloaded method for this instance of OthelloBoard
        return nextBestMove(this, playersSymbol, opponentSymbol, movesToLookAhead);
    }

    public int[] nextBestMove(OthelloBoard othelloBoard, String playersSymbol,
                              String opponentSymbol, int movesToLookAhead) {
        /* find the next best move for given player (to some extent), look ahead given amount of moves to find overall
        best move (largest gain in pieces on board from now).
        cycles threw every possible move and, assuming opponent always does their own best move, and given player's future
        moves are also their best ones (this method), then method returns index of move that results in best outcome for
        given player by taking total pieces would be flipped by player minus the same for their opponent, preferring side
        pieces. Method only calculates the moves ahead given (>3 results in long processing time).
        */
        int[] bestMove = new int[2];  // array to hold row & column index
        int rowIndex = -1;
        int columnIndex = -1;
        bestMove[0] = rowIndex;
        bestMove[1] = columnIndex;
        if (!othelloBoard.isSomeLegalMoveLeft()) {  // if no legal moves then return
            return bestMove;
        }
        int bestTotalGainInPieces = Integer.MIN_VALUE;  // store the highest number of gain in pieces found for move
        String[][] board = othelloBoard.BOARD;  // for conciseness

        for (int row = 0; row < board.length; row++) {
            for (int column = 0; column < board[row].length; column++) {
                if (othelloBoard.isLegalMove(row, column, playersSymbol)) {  // if this is a legal move then check
                    int movesAheadChecked = 0;  // counter for moves ahead checked
                    OthelloBoard boardCheck = new OthelloBoard(othelloBoard);  // create copy of board to simulate moves
                    // place piece here for calling player and add pieces flipped to total
                    int playerEndScoreWouldBe = boardCheck.runTurn(row, column, playersSymbol);
                    int opponentsEndScoreWouldBe = 0;

                    while (movesAheadChecked < movesToLookAhead && isSomeLegalMoveLeft(boardCheck)) {
                        // while didn't check up to moves ahead specified and game isn't over
                        movesAheadChecked++;
                        int movesLeftToCheck = movesToLookAhead - movesAheadChecked;  // moves ahead left to check

                        // place best move for calling player's opponent and add pieces flipped to their total
                        int[] opponentsNextMove = boardCheck.nextBestMove(boardCheck, opponentSymbol,
                                playersSymbol, movesLeftToCheck);
                        opponentsEndScoreWouldBe += runTurn(boardCheck, opponentsNextMove[0], opponentsNextMove[1],
                                opponentSymbol);

                        // simulate calling player's own move after opponents next move and add that to their total
                        int[] playersNextMove = boardCheck.nextBestMove(boardCheck, playersSymbol,
                                opponentSymbol, movesLeftToCheck);  // place best move for calling player's next turn
                        playerEndScoreWouldBe += boardCheck.runTurn(playersNextMove[0], playersNextMove[1], playersSymbol);
                    }
                    // calculate if this move is best:
                    boolean isGreaterThanLastHighestMoveFound =
                            playerEndScoreWouldBe - opponentsEndScoreWouldBe > bestTotalGainInPieces;
                    boolean isEqualToLastHighestMoveFound =
                            playerEndScoreWouldBe - opponentsEndScoreWouldBe == bestTotalGainInPieces;
                    boolean isSidePlace =  // if pieces is on edge of board (generally better moves) test...
                            row == 0 || row == othelloBoard.BOARD.length - 1 ||
                                    column == 0 || column == othelloBoard.BOARD.length - 1;
                    // set this move as new best move found if it results in the greatest gain in pieces, OR results in equal
                    // gain in pieces as last highest move found, but it is a side piece
                    if (isGreaterThanLastHighestMoveFound || (isEqualToLastHighestMoveFound && isSidePlace)) {
                        // update number of pieces best move so far would ultimately get and indexes
                        bestTotalGainInPieces = playerEndScoreWouldBe - opponentsEndScoreWouldBe;
                        rowIndex = row;
                        columnIndex = column;
                    }
                }
            }
        }
        // return best move indexes and number of pieces would be flipped by that move
        bestMove[0] = rowIndex;
        bestMove[1] = columnIndex;
        return bestMove;
    }

    private int getMovesToLookAhead(GameDifficulty gameDifficulty) {
        /* return moves to look ahead based on game difficulty set */
        if (gameDifficulty == GameDifficulty.EASY) {
            /* if set to easy difficulty, then moves to look ahead for program is 0 (best move for that turn
            (preferring side pieces), won't estimate opponents next move and calculate based on that) */
            return 0;
        } else if (gameDifficulty == GameDifficulty.MEDIUM) {  // medium. will look ahead 2 moves and calculate best overall
            return 2;
        } else {  // hard - 4 moves ahead
            return 4;
        }
    }
}
