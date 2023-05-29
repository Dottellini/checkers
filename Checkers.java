import java.util.*;
import java.util.stream.Collectors;

class Game {
    List<Checker> checkersList = new ArrayList<>();
    int player = 0;
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
            if(i < 12 || i > 19) checkersList.add(new Checker(y < 4 ? 0 : 1, i % 4, y, i)); //These are checkers for players 0 and 1
            else checkersList.add(new Checker(2, i % 4, y, i)); //These are empty spaces
        }
    }

    //move can be "0" or "1". 0 is a move left, 1 is a move right
    Game move(int piecePos, int move) throws IllegalArgumentException {
        assert move == 1 || move == 0 : "Move must be 0 or 1";

        Game copy = Game.of(this.checkersList);
        Checker piece = copy.findPiece(piecePos);
        if(piece.player != this.player) throw new IllegalArgumentException("This is not your playing Piece");
        if((piece.player == 0 && piece.pos > 27 )||(piece.player == 1 && piece.pos < 4)) throw new IllegalArgumentException("Player cant move outside of playing field vertically");

        int moveDirection = piece.player == 0 ? +1 : -1; //This determines the direction (up or down) the move is going (Dependant on who is playing)
        int piecePosOffset = ((move == 0 ? 4 : 3) + ((piece.pos / 4) % 2 == 1 ? (piece.player == 0 ? 1 : 0) : (piece.player == 0 ? 0 : 1))) * moveDirection; //This calculates the position of the Target Field Offset to the current field.
        //The first check looks if the move is going right or left and returns the necessary offset. The second check is necessary since we are moving diagonally and every second row, our 
        //move offsets are 5 and 4 instead of 4 and 3. Also we need to check for player since the boards "every secoond row" changes wether moving up or down

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
        copy.player = this.player == 0 ? 1 : 0;
        return copy;
        
    }

    Checker findPiece(int pos) {
        Optional<Checker> pieceOptional = checkersList.stream().filter(c -> c.pos == pos).findFirst();
        if(!pieceOptional.isPresent()) throw new IllegalArgumentException("No piece found");
        return pieceOptional.get();
    }

    Game attack(Checker piece, Checker target, int move, int moveDirection) {
        if((piece.player == 0 && piece.pos > 23)||(piece.player == 1 && piece.pos < 8)) throw new IllegalArgumentException("Player cant attack outside of playing field vertically");
        int piecePosOffset = (move == 0 ? 9 : 7) * moveDirection;
        Checker landingPiece = findPiece(piece.pos + piecePosOffset);
        if((landingPiece.pos / 4) % 2 != (piece.pos / 4) % 2) throw new IllegalArgumentException("Player cant move outside of playing field horizontally");
        landingPiece.become(piece);
        target.kill();
        piece.kill();
        return this;
    }

    @Override
    public String toString() {
        return checkersList.stream().sorted((a, b) -> a.pos - b.pos).map(Object::toString).collect(Collectors.joining("\n"));
    }
}

class Checker {
    int player; //can be 0 or 1, standing for player 0 and player 1, or 2 meaning this is an empty field
    boolean alive = true;
    int x;
    int y;
    int pos;

    Checker(int player, int x, int y, int pos) {
        this.player = player;
        this.x = x;
        this.y = y;
        this.pos = pos;
        if(this.player == 2) this.alive = false; //Empty fields are not alive
    }

    //This checker becomes the target checker
    void become(Checker target) {
        this.player = target.player;
        this.alive = target.alive;
    }

    //Kills current Checker
    void kill() {
        this.alive = false;
        this.player = 2;
    }

    @Override
    public String toString() {
        return "(" + this.x + "/" + this.y + ") Pos:" + this.pos + " Team:" + this.player;
    }
}

/*class Dame extends Checker {
    Dame() {

    }
}*/