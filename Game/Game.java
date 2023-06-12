package Game;

import java.util.*;

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