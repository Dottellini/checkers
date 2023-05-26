import java.util.*;

class Game {
    ArrayList<Checker> checkersList = new ArrayList<>();
    int boardSize = 32;

    Game() {
        for(int i = 0; i < boardSize; i++) {
            checkersList.add(new Checker());
        }
    }
}

class Checker {
    int team;
    boolean alive = true;
    int pos;

    Checker(int team, int pos) {
        this.team = team;
        this.pos = pos;
    }
}

class Dame extends Checker {

}