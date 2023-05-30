import java.util.*;
import java.util.stream.Collectors;

enum Player {
    ONE,
    TWO,
    NONE
}

enum Move {
    LEFT(4),
    RIGHT(3),
    BACKLEFT(-3),
    BACKRIGHT(-4),
    NONE(0);

    public final int value;

    private Move(int value) {
        this.value = value;
    }
}

class Game {
    List<Checker> checkersList = new ArrayList<>();
    Player player = Player.ONE;
    int boardSize = 32;

    static Game of(List<Checker> checkersList) {
        return new Game(checkersList);
    }

    private Game(List<Checker> checkersList) {
        this.checkersList = checkersList;
    }

    Game() {
        int y = -1;
        for(int i = 0; i < boardSize; i++) {
            if(i % 4 == 0) y++;
            if(i < 12 || i > 19) checkersList.add(new Checker(y < 4 ? Player.ONE : Player.TWO, i % 4, y, i)); //These are checkers for players 1 and 2
            else checkersList.add(new Checker(Player.NONE, i % 4, y, i)); //These are empty spaces
        }
    }

    Game move(int piecePos, Move move) throws IllegalArgumentException {
        assert !isGameOver() : "Game is over";

        //TODO:
        //Rückwärts angreifen
        //Schlagzwang prüfen
        //Dame einbauen -> unendlich weit springen, unendlich weit angreifen?

        Game copy = Game.of(this.checkersList);
        Checker piece = copy.findPiece(piecePos);
        if(piece.player != this.player) throw new IllegalArgumentException("This is not your playing Piece");
        if((piece.player == Player.ONE && piece.pos > 27 )||(piece.player == Player.TWO && piece.pos < 4)) throw new IllegalArgumentException("Player cant move outside of playing field vertically");

        int moveDirection = piece.player == Player.ONE ? (move == Move.RIGHT || move == Move.LEFT ? +1 : -1) : (move == Move.RIGHT || move == Move.LEFT ? -1 : +1); //This determines the direction (up or down) the move is going (Dependant on who is playing)
        int piecePosOffset = piece.targetPieceOffset(move, moveDirection); //This calculates the position of the Target Piece Offset to the current piece.

        Checker targetPiece = copy.findPiece(piece.pos + piecePosOffset);
        if((targetPiece.pos / 4) % 2 == (piece.pos / 4) % 2) throw new IllegalArgumentException("Player cant move outside of playing field horizontally");

        //Here we do the actual moving
        //////////////////////////////
        if(targetPiece.player == piece.player) return this; //The space is already occupied by a piece of the same player, so we just return the same game and dont Move
        if(!piece.alive) return this; //cant move an empty field
        
        //The target is an enemy piece
        if(targetPiece.alive && targetPiece.player != piece.player) {
            return copy.attack(piece, targetPiece, move, moveDirection);
        }

        //regular move without attacking etc.
        targetPiece.become(piece);
        piece.kill();
        copy.player = this.player == Player.ONE ? Player.TWO : Player.ONE;
        return copy;
        
    }

    Game newMove(int piecePos, int targetPos) throws IllegalArgumentException {
        assert !isGameOver() : "Game is over";
        if(targetPos > 31 || targetPos < 0) throw new IllegalArgumentException("Player cant move outside of playing field vertically");

        Game copy = Game.of(this.checkersList);
        Checker piece = copy.findPiece(piecePos);
        Checker target = copy.findPiece(targetPos);

        if(!piece.alive) return this; //cant move an empty field
        if(piece.player != this.player) throw new IllegalArgumentException("This is not your playing Piece");
        if(!piece.canReach(target)) throw new IllegalArgumentException("Target cant be reached");
        if(target.player == piece.player) return this; //The space is already occupied by a piece of the same player, so we just return the same game and dont Move

        //TODO: Add logic for Dame
        if(piece.getClass() == Dame.class) {

        }

        Move move = piece.retrieveMoveTo(target);
        if(target.alive && target.player != piece.player) {
            return copy.newAttack(piece, target, move);
        }

        //regular move without attacking etc.
        target.become(piece);
        piece.kill();
        copy.player = this.player == Player.ONE ? Player.TWO : Player.ONE;

        return copy;
    }

    Game attack(Checker piece, Checker target, Move move, int moveDirection) {
        if((piece.player == Player.ONE && piece.pos > 23)||(piece.player == Player.TWO && piece.pos < 8)) throw new IllegalArgumentException("Player cant attack outside of playing field vertically");
        int piecePosOffset = (move == Move.LEFT ? 9 : 7) * moveDirection;
        Checker landingPiece = findPiece(piece.pos + piecePosOffset);
        if((landingPiece.pos / 4) % 2 != (piece.pos / 4) % 2) throw new IllegalArgumentException("Player cant move outside of playing field horizontally");
        landingPiece.become(piece);
        target.kill();
        piece.kill();
        return this;
    }

    Game newAttack(Checker piece, Checker target, Move move) {
        //TODO: MAKE NEW ATTACK
        //int moveDirection = piece.player == Player.ONE ? () : ();
        //int landingPieceOffset = move == Move.LEFT || ? 9 : 7;
        return this;
    }

    Checker findPiece(int pos) {
        Optional<Checker> pieceOptional = checkersList.stream().filter(c -> c.pos == pos).findFirst();
        if(!pieceOptional.isPresent()) throw new IllegalArgumentException("No piece found");
        return pieceOptional.get();
    }

    long pieceAmountOfPlayer(Player player) {
        return checkersList.stream().filter(item -> item.alive && item.player == player).count();
    }

    boolean isGameOver() {
        long pieceAmount1= pieceAmountOfPlayer(Player.ONE); //Pieces of player 1
        long pieceAmount2 = pieceAmountOfPlayer(Player.TWO); //Pieces of player 2
        if(pieceAmount1 <= 0 || pieceAmount2 <= 0) return true;
        return false;
    }

    Player isWinning() {
        long pieceAmount1 = pieceAmountOfPlayer(Player.ONE); //Pieces of player 1
        long pieceAmount2 = pieceAmountOfPlayer(Player.TWO); //Pieces of player 2
        if(pieceAmount1 > pieceAmount2) return Player.ONE;
        else if (pieceAmount1 < pieceAmount2) return Player.TWO;
        return Player.NONE;
    }

    @Override
    public String toString() {
        return checkersList.stream().sorted((a, b) -> a.pos - b.pos).map(Object::toString).collect(Collectors.joining("\n"));
    }
}

class Checker {
    Player player; //Uses the Player Enum
    boolean alive = true;
    int x;
    int y;
    int pos;
    int leftMoveOffset = 4;
    int rightMoveOffset = 3;

    Checker(Player player, int x, int y, int pos) {
        this.player = player;
        this.x = x;
        this.y = y;
        this.pos = pos;
        if(this.player == Player.NONE) this.alive = false; //Empty fields are not alive
    }

    //This checker becomes the target checker
    void become(Checker target) {
        this.player = target.player;
        this.alive = target.alive;
    }

    //Kills current Checker
    void kill() {
        this.alive = false;
        this.player = Player.NONE;
    }

    int targetPieceOffset(Move move, int moveDirection) {
        //The first check looks if the move is going right or left and returns the necessary offset. The second check is necessary since we are moving diagonally and every second row, our 
        //move offsets are 5 and 4 instead of 4 and 3. Also we need to check for player since the boards "every secoond row" changes wether moving up or down
        return ((move == Move.LEFT ? this.leftMoveOffset : this.rightMoveOffset) + getRowModulo()) * moveDirection;
    }

    //Checks if target is in reach (1 row up or down)
    boolean canReach(Checker target) {
        int rowNum = getRowModulo();
        Move move = retrieveMoveTo(target);
        if(rowNum != target.getRowModulo() && move != Move.NONE) return true;
        return false;
    }

    Move retrieveMoveTo(Checker target) {
        int offset = target.pos - this.pos; 
        int rowNum = getRowModulo();
        if(this.player == Player.ONE) {
            if(offset == Move.LEFT.value + rowNum) return Move.LEFT;
            if(offset == Move.RIGHT.value + rowNum) return Move.RIGHT;
            rowNum = rowNum == 1 ? 0 : 1;
            if(offset == Move.BACKRIGHT.value - rowNum) return Move.BACKRIGHT;
            if(offset == Move.BACKLEFT.value - rowNum) return Move.BACKLEFT;
        } else if (this.player == Player.TWO) {
            if(offset == -Move.BACKRIGHT.value + rowNum) return Move.BACKRIGHT;
            if(offset == -Move.BACKLEFT.value + rowNum) return Move.BACKLEFT;
            rowNum = rowNum == 1 ? 0 : 1;    
            if(offset == -Move.LEFT.value - rowNum) return Move.LEFT;
            if(offset == -Move.RIGHT.value - rowNum) return Move.RIGHT;
        }

        return Move.NONE;
    }

    private int getRowModulo() {
        return (this.pos/4) % 2;
    }

    @Override
    public String toString() {
        return "(" + this.x + "/" + this.y + ") Pos:" + this.pos + " Team:" + this.player;
    }
}

class Dame extends Checker {
    Dame(Player player, int x, int y, int pos) {
        super(player, x, y, pos);
    }
}