import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

enum Player {
    ONE(0),
    TWO(1),
    NONE(2);

    public final int value;

    private Player(int value) {
        this.value = value;
    }
}

enum Move {
    LEFT(4, 9),
    RIGHT(3, 7),
    BACKLEFT(-3, -7),
    BACKRIGHT(-4, -9),
    NONE(0, 0);

    public final int value;
    public final int attack;

    private Move(int value, int attack) {
        this.value = value;
        this.attack = attack;
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

    Game move(int piecePos, int movePos) {
        assert !isGameOver() : "Game is over";
        if(movePos > 31 || movePos < 0) return this; //throw new IllegalArgumentException("Player cant move outside of playing field vertically");

        //TODO:
        //Dame einbauen -> unendlich weit springen, unendlich weit angreifen, aber muss direkt hinter Stein ziehen

        Game copy = Game.of(this.checkersList);
        Checker piece = copy.findPiece(piecePos);
        Checker movePiece = copy.findPiece(movePos);

        if(!piece.alive) return this; //cant move an empty field
        if(piece.player != this.player) return this; //This is not your playing piece
        if(movePiece.player == piece.player) return this; //The space is already occupied by a piece of the same player, so we just return the same game and dont Move

        //TODO: Add logic for Dame
        if(piece.getClass() == Dame.class) {

        }

        int offset = movePiece.pos - piece.pos;
        Move move = piece.retrieveMoveTo(movePiece);

        //Attack by movePos being a jump over the target to be attacked
        if(offset == move.attack || offset == -move.attack) {
            int moveDirection = piece.player == Player.ONE ? 1 : -1;
            Checker target = findPiece(piece.pos + moveDirection * (move.attack - move.value));
            if(!piece.canReach(target)) return this; //throw new IllegalArgumentException("Target cant be reached");
            
            if(target.alive && target.player != piece.player) {
                return copy.attack(piece, target, move);
            }

            return this;
        }

        if(!piece.canReach(movePiece)) return this; //throw new IllegalArgumentException("Target cant be reached");

        if(movePiece.alive && movePiece.player != piece.player) {
            return copy.attack(piece, movePiece, move);
        }

        //regular move without attacking etc.
        if(move == Move.BACKLEFT || move == Move.BACKRIGHT) return this; //throw new IllegalArgumentException("Cant move Backwards");
        movePiece.become(piece);
        piece.kill();
        copy.player = this.player == Player.ONE ? Player.TWO : Player.ONE;

        return copy;
    }

    Game attack(Checker piece, Checker target, Move move) {
        int movementDirection = piece.player == Player.ONE ? 1 : -1;
        int moveAttackValue = move.attack;
        Checker landingChecker = findPiece(piece.pos + (movementDirection * moveAttackValue));
        if(((landingChecker.pos / 4) % 2) != ((piece.pos / 4) % 2)) return this; //throw new IllegalArgumentException("Piece cant land behind attacked piece");
        landingChecker.become(piece);
        target.kill();
        piece.kill();
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

    //TODO: Zugalgorithmus

    @Override
    public boolean equals(Object other) {
        if(other == null) return false;
        if(other == this) return true;
        if(other.getClass() != getClass()) return false;
        Game that = (Game)other;
        return that.player == this.player && that.checkersList.equals(checkersList);
    }

    @Override
    public String toString() {
        System.out.println("Your turn: " + this.player);
        //toString in Numbers
        //return checkersList.stream().sorted((a, b) -> a.pos - b.pos).map(Object::toString).collect(Collectors.joining("\n"));

        //toString as Playingfield
        String s = "\n";
        String[] pieces = new String[]{" O ", " + ", "   "};
        
        for(Checker c: checkersList) {
            if(c.pos % 4 == 0) s += "\n";
            if(c.y % 2 == 1) s+= " ■ ";
            s += pieces[c.player.value];
            if(c.y % 2 == 0) s+= " ■ ";
        }


        return s;
    }
}

class Checker {
    Player player; //Uses the Player Enum
    boolean alive = true;
    int x;
    int y;
    int pos;

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

    List<Integer> possibleMoves(Game g) {
        //TODO: Possible Moves method for Zugalgorithmus
        List<Integer> movePositions = new ArrayList<>();
        if(player == Player.NONE) return movePositions;
        for(Move m: Move.values()){
            //Mit g.move(piecepos, targetpos (durch m)) jeden move ausprobieren und return vergleichen
            g.findPiece(pos + m.value);
        }
        return movePositions;
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
            if(offset == Move.LEFT.value + rowNum || offset == Move.LEFT.attack) return Move.LEFT;
            if(offset == Move.RIGHT.value + rowNum || offset == Move.RIGHT.attack) return Move.RIGHT;
            rowNum = rowNum == 1 ? 0 : 1;
            if(offset == Move.BACKRIGHT.value - rowNum || offset == Move.BACKRIGHT.attack) return Move.BACKRIGHT;
            if(offset == Move.BACKLEFT.value - rowNum || offset == Move.BACKLEFT.attack) return Move.BACKLEFT;
        } else if (this.player == Player.TWO) {
            if(offset == -Move.BACKRIGHT.value + rowNum || offset == -Move.BACKRIGHT.attack) return Move.BACKRIGHT;
            if(offset == -Move.BACKLEFT.value + rowNum || offset == -Move.BACKLEFT.attack) return Move.BACKLEFT;
            rowNum = rowNum == 1 ? 0 : 1;    
            if(offset == -Move.LEFT.value - rowNum || offset == -Move.LEFT.attack) return Move.LEFT;
            if(offset == -Move.RIGHT.value - rowNum || offset == -Move.RIGHT.attack) return Move.RIGHT;
        }

        return Move.NONE;
    }

    private int getRowModulo() {
        return (this.pos/4) % 2;
    }

    @Override
    public boolean equals(Object other) {
        if(other == null) return false;
        if(other == this) return true;
        if(other.getClass() != getClass()) return false;
        Checker that = (Checker)other;
        return that.player == this.player && that.alive == alive && that.x == x && that.y == y && that.pos == pos;
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



//TESTING

