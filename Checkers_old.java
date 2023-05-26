import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

class Game {
    ArrayList<Checker> checkersList = new ArrayList<>();
    int boardSize = 32;

    Game() {
        int y = -1;
        for(int i = 0; i < boardSize; i++) {
            if(i % 4 == 0) y++;
            if(i < 12 || i > 19) checkersList.add(new Checker(y < 4 ? 0 : 1, i % 4, y, i));
        }
    }

    //move can be "0" or "1". 0 is a move left, 1 is a move right
    /*void move(int piecePos, int move) {
        Checker piece = checkersList.get(piecePos);
    }*/

    @Override
    public String toString() {
        //String[] checkerSymbols = new String[]{"S", "K"};
        String s = checkersList.stream().map(Object::toString).collect(Collectors.joining("\n"));

        /*for(Checker item: checkersList) {
            s += checkerSymbols[item.team] + (item.x < 3 ? "| |" : "") + (item.x == 3 ? "\n" : "");
            if(item.x == 3 && item.y == 2) s += " | | | | | | | | \n | | | | | | | | \n";
        }*/

        return s;
    }
}

class Checker {
    int team;
    boolean alive = true;
    int x;
    int y;
    int pos;

    Checker(int team, int x, int y, int pos) {
        this.team = team;
        this.x = x;
        this.y = y;
        this.pos = pos;
    }

    @Override
    public String toString() {
        return "(" + this.x + "/" + this.y + ") Pos:" + this.pos + " Team:" + this.team;
    }
}

/*class Dame extends Checker {
    Dame() {

    }
}*/