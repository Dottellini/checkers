package Game;

import java.util.*;

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
