import java.util.*;
import java.util.stream.Collectors;

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
        List<Checker> copyList = new ArrayList<>();
        for(Checker c: checkersList) {
            copyList.add(c.clone());
        }
        this.checkersList = copyList;
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

        Game copy = Game.of(this.checkersList);
        Checker piece = copy.findPiece(piecePos);
        Checker movePiece = copy.findPiece(movePos);

        if(!piece.alive) return this; //cant move an empty field
        if(piece.player != this.player) return this; //This is not your playing piece
        if(movePiece.player == piece.player) return this; //The space is already occupied by a piece of the same player, so we just return the same game and dont Move

        int offset = movePiece.pos - piece.pos;
        Move move = piece.retrieveMoveTo(movePiece);

        //Attack logic that works for normal and Dame piece
        //////////////////////
        //Attack by movePos being a jump over the target to be attacked
        if(offset == move.attack || offset == -move.attack) {
            int moveDirection = piece.player == Player.ONE ? 1 : -1;
            int offsetByRowNum = 0; //This is necessary for Backwards jump attacking
            if(move == Move.BACKLEFT || move == Move.BACKRIGHT) {
                if(player == Player.ONE) {
                    offsetByRowNum = (piece.pos/4) % 2;
                }
                if(player == Player.TWO) {
                    offsetByRowNum = (piece.pos/4) % 2 == 0 ? -1 : 0;
                }
            }
            Checker target = copy.findPiece(piece.pos + moveDirection * (move.attack - move.value) + offsetByRowNum);
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
        //////////////////////////

        if(piece.getClass() == Dame.class) {
            if(!movePiece.alive && movePiece.player == Player.NONE) {
                copy.checkersList.set(movePiece.pos, movePiece.asDame(piece)); //TODO: Check if this works lmao
                piece.kill();
                copy.player = this.player == Player.ONE ? Player.TWO : Player.ONE;
                return copy;
            }
        }
        //regular move without attacking etc.
        if(move == Move.BACKLEFT || move == Move.BACKRIGHT) return this; //throw new IllegalArgumentException("Cant move Backwards");
        
        if((player == Player.ONE && movePiece.pos >= 28) || (player == Player.TWO && movePiece.pos <= 3)) {
            copy.checkersList.set(movePiece.pos, movePiece.asDame(piece)); //If at the end of the board, piece becomes a Dame
        } else {
            movePiece.become(piece);
        }

        piece.kill();
        copy.player = this.player == Player.ONE ? Player.TWO : Player.ONE;

        return copy;
    }

    Game attack(Checker piece, Checker target, Move move) {
        int movementDirection = piece.player == Player.ONE ? 1 : -1;
        int moveAttackValue = move.attack;
        Checker landingChecker = findPiece(piece.pos + (movementDirection * moveAttackValue));
        if(((landingChecker.pos / 4) % 2) != ((piece.pos / 4) % 2)) return this; //throw new IllegalArgumentException("Piece cant land behind attacked piece");
        if((player == Player.ONE && landingChecker.pos >= 28) || (player == Player.TWO && landingChecker.pos <= 3)) {
            checkersList.set(landingChecker.pos, landingChecker.asDame(piece)); //If at the end of the game, piece becomes a Dame
        } else {
            landingChecker.become(piece);
        }
        target.kill();
        piece.kill();
        return this;
    }

    Checker findPiece(int x, int y) {
        assert x >= 0 && x < 4 && y >= 0 && y < 8;
        Optional<Checker> pieceOptional = checkersList.stream().filter(c -> c.x == x && c.y == y).findFirst();
        if(!pieceOptional.isPresent()) throw new IllegalArgumentException("No piece found");
        return pieceOptional.get();
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
        //toString in Numbers
        //return checkersList.stream().sorted((a, b) -> a.pos - b.pos).map(Object::toString).collect(Collectors.joining("\n"));

        //toString as Playingfield
        String s = "Your turn: " + this.player + "\n";
        String[] pieces = new String[]{" O ", " + ", "   ", " D "};
        
        for(Checker c: checkersList) {
            if(c.pos % 4 == 0) s += "\n";
            if(c.y % 2 == 1) s+= " ■ ";
            if(c.getClass() == Dame.class) s += pieces[3];
            else s += pieces[c.player.value];
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

    Dame asDame(Checker t) {
        Dame d = new Dame(t.player, this.x, this.y, this.pos);
        d.alive = true;
        return d;
    }

    //Kills current Checker
    void kill() {
        this.alive = false;
        this.player = Player.NONE;
    }

    //Returns a List of possible Moves of this piece in Game g
    List<Integer> possibleMoves(Game g) {
        if(g.player != player) throw new IllegalArgumentException("Not your turn");
        List<Integer> movePositions = new ArrayList<>();
        if(player == Player.NONE) return movePositions;
        int rowNum = getRowModulo();

        for(Move m: Move.values()){
            int moveValue = 0;
            if(player == Player.ONE) {
                if(m == Move.LEFT || m == Move.RIGHT) moveValue = m.value + rowNum;
                if(m == Move.BACKLEFT || m == Move.BACKRIGHT) moveValue = m.value - (rowNum == 0 ? 1 : 0);
            }
            if(player == Player.TWO) {
                if(m == Move.LEFT || m == Move.RIGHT) moveValue = -m.value - (rowNum == 0 ? 1 : 0);
                if(m == Move.BACKLEFT || m == Move.BACKRIGHT) moveValue = -m.value + rowNum;
            }
            Game copy = g.move(pos, pos + moveValue);
            if(!copy.equals(g)) {
                movePositions.add(pos + m.value);
            }
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

    //TODO: Mit der unteren methode ersetzen?
    //Return Move that is required to get to target (Only works if the target is 1 row away)
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

    //retrieveMoveTo using the x, y coordinates (works for any amount of rows)
    Move retrieveMoveTo(Checker target, int xOffset, int xDirection, int yDirection) {
        //Calculate the MoveDirection
        Move moveDir = Move.NONE;
        if(xDirection > 0 && yDirection > 0) {
            moveDir = player == Player.ONE ? Move.LEFT : Move.BACKRIGHT;
        }
        if(xDirection < 0 && yDirection > 0) {
            moveDir = player == Player.ONE ? Move.RIGHT : Move.BACKLEFT;
        }
        if(xDirection > 0 && yDirection < 0) {
            moveDir = player == Player.ONE ? Move.BACKLEFT : Move.RIGHT;
        }
        if(xDirection < 0 && yDirection < 0) {
            moveDir = player == Player.ONE ? Move.BACKRIGHT : Move.LEFT;
        }

        return moveDir;
    }

    //Return the Row Modulo number (even or odd row)
    int getRowModulo() {
        return (this.pos/4) % 2;
    }

    public Checker clone() {
        return new Checker(this.player, this.x, this.y, this.pos);
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

    public Dame clone() {
        return new Dame(this.player, this.x, this.y, this.pos);
    }

    //PoossibleMoves
    List<Integer> possibleMoves(Game g) {
        if(g.player != player) throw new IllegalArgumentException("Not your turn");
        List<Integer> movePositions = new ArrayList<>();
        for(Checker c: g.checkersList) {
            if(this.canReach(c, g)) {
                movePositions.add(c.pos);
            }
        }
        return movePositions;
    }
    
    //checks if there is a piece in the way from the current one to the target
    boolean canReach(Checker target, Game g) {
        if(player == Player.NONE) return false; //empty field cant Move
        if(this.pos == target.pos) return false;

        int rowNum = getRowModulo();
        int xOffset = target.x - x;
        int yOffset = target.y - y;

        int xDirection = xOffset < 0 ? -1 : 1;
        int yDirection = yOffset < 0 ? -1 : 1;

        if(xOffset == 0) {
            xDirection = (target.pos - this.pos) % 2 == rowNum ? 1 : -1;
        }

        //Calculate the MoveDirection
        Move moveDir = retrieveMoveTo(target, xOffset, xDirection, yDirection);

        if(moveDir == Move.NONE) return false; //If no move found, there was something wrong

        int xCurrent = this.x; // X Value of next Piece in direction to target piece
        int yCurrent = this.y; // Y Value of next Piece in direction to target piece

        Checker currentChecker = g.findPiece(xCurrent, yCurrent);

        while(!currentChecker.equals(target)) {
            int rowOfCurrent = currentChecker.getRowModulo();
            if(moveDir == Move.LEFT && player == Player.ONE || moveDir == Move.BACKRIGHT && player == Player.TWO) {
                xCurrent += rowOfCurrent;
                yCurrent += 1;
            }
            else if(moveDir == Move.RIGHT && player == Player.ONE || moveDir == Move.BACKLEFT && player == Player.TWO) {
                xCurrent -= (rowOfCurrent == 0 ? 1 : 0);
                yCurrent += 1;
            }
            else if(moveDir == Move.BACKLEFT && player == Player.ONE || moveDir == Move.RIGHT && player == Player.TWO) {
                xCurrent += rowOfCurrent;
                yCurrent -= 1;
            }
            else if(moveDir == Move.BACKRIGHT && player == Player.ONE || moveDir == Move.LEFT && player == Player.TWO) {
                xCurrent -= (rowOfCurrent == 0 ? 1 : 0);
                yCurrent -= 1;
            };

            if(xCurrent < 0 || xCurrent > 3 || yCurrent < 0 || yCurrent > 7) return false;
            currentChecker = g.findPiece(xCurrent, yCurrent);
            if(currentChecker.player != Player.NONE && !currentChecker.equals(target)) {
                return false;
            }
        }

        return true;
    }
}




//TESTING

Game g = new Game();

//Regular Move Player 1
assert g.move(10, 13).findPiece(13).player == Player.ONE && g.move(10, 13).findPiece(10).player == Player.NONE : "Regular Move Player 1";
//Regular Move Player 2
assert g.move(10, 13).move(21, 17).findPiece(17).player == Player.TWO && g.move(10, 13).move(21, 17).findPiece(21).player == Player.NONE : "Regular Move Player 2";
//Backwards Move Player 1
assert g.move(10, 13).move(13, 10).equals(g.move(10, 13)) : "Backwards Move Player 1";
//Backwards Move Player 2
assert g.move(10, 13).move(21, 17).move(11, 15).move(17, 21).equals(g.move(10, 13).move(21, 17).move(11, 15)) : "Backwards Move Player 2";
//Attack Player 1
Game c = g.move(10, 13).move(22, 18).move(13, 18);
assert !c.findPiece(18).alive && c.findPiece(22).player == Player.ONE : "Attack Player 1";
//Attack Player 2
c = g.move(10, 13).move(21, 17).move(11, 15).move(17, 13);
assert !c.findPiece(13).alive && c.findPiece(10).player == Player.TWO : "Attack Player 2";
//Jump Attack Player 1
c = g.move(10, 13).move(22, 18).move(13, 22);
assert !c.findPiece(18).alive && c.findPiece(22).player == Player.ONE : "Jump Attack Player 1";
//Jump Attack Player 2
c = g.move(10, 13).move(21, 17).move(11, 15).move(17, 10);
assert !c.findPiece(13).alive && c.findPiece(10).player == Player.TWO : "Jump Attack Player 2";
//Backwards Attack Player 1
c = g.move(10, 13).move(21, 18).move(13, 17).move(22, 19).move(17, 21).move(19, 15).move(21, 18);
assert !c.findPiece(18).alive && c.findPiece(14).player == Player.ONE : "Backwards Attack Player 1";
//Backwards Attack Player 2
c = g.move(10, 13).move(21, 18).move(11, 15).move(18, 14).move(8, 12).move(14, 10).move(12, 16).move(10, 13);
assert !c.findPiece(13).alive && c.findPiece(17).player == Player.TWO : "Backwards Attack Player 2";
//Backwards Jump Attack Player 1
c = g.move(10, 13).move(21, 18).move(13, 17).move(22, 19).move(17, 21).move(19, 15).move(21, 14);
assert !c.findPiece(18).alive && c.findPiece(14).player == Player.ONE : "Backwards Jump Attack Player 1";
//Backwards Jump Attack Player 2
c = g.move(10, 13).move(21, 18).move(11, 15).move(18, 14).move(8, 12).move(14, 10).move(12, 16).move(10, 17);
assert !c.findPiece(13).alive && c.findPiece(17).player == Player.TWO : "Backwards Jump Attack Player 2";
//Illegal move: Cant Reach
assert g.move(10, 20).equals(g) : "Illegal move: Cant Reach";
//Illegal move: No free space
assert g.move(10, 6).equals(g) : "Illegal move: No free space";
//Illegal move: Move outside horizontally
assert g.move(8, 11).equals(g) : "Illegal move: Move outside horizontally";
//Illegal move: Move outside vertically
assert g.move(1, -1).equals(g) : "Illegal move: Move outside vertically";
assert g.move(30, 32).equals(g) : "Illegal move: Move outside vertically";

//Check if creating Dame works
for(int i = 0; i < g.boardSize; i++) {
    Checker c = g.findPiece(i);
    Checker dummy = new Checker(Player.NONE, c.x, c.y, c.pos);
    dummy.alive = false;
    g.checkersList.set(i, dummy);
}
g.checkersList.set(4, new Checker(Player.TWO, 0, 1, 4));
g.checkersList.set(26, new Checker(Player.ONE, 2, 6, 26));

Game d = g.move(26, 30);

d = d.move(4, 0);
assert d.checkersList.get(0).getClass() == Dame.class && d.checkersList.get(30).getClass() == Dame.class: "Check if creating Dame works";
