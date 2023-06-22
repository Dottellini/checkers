import java.util.*;
import java.util.stream.Collectors;


import java.lang.reflect.WildcardType;
import processing.core.PApplet;
import processing.core.PGraphics;


//PROCESSING Stuff
public class Checkers extends PApplet {
    Stack<IGame> history = new Stack<>();
    static int width = 1600;
    static int height = 900;
    int playingFieldX = 100;
    int playingFieldY = 100;
    int whiteField = color(247, 216, 186);
    int darkField = color(77, 66, 55);
    int playingFieldOffsetX = (width - 800) / 2; //These are used to center the playingfield
    int playingFieldOffsetY = (height - 800) / 2;
    int playerOneColor = color(138, 35, 12);
    int playerTwoColor = color(245, 233, 220);
    boolean botPlayerActivated = false;
    IGame game;
    Checker selectedChecker = null;
    List<MoveElem> possibleMovesSelectedChecker = new ArrayList<>();
    Textbutton revertMoveButton;
    Textbutton botPlayerButton;

    
    public static void main(String[] args) {
        String[] appArgs = {"Dame"};
		Checkers mySketch = new Checkers();
		PApplet.runSketch(appArgs, mySketch);
    }

    public void settings() {
        size(width, height);
    }

    public void setup() {
        background(color(255, 255, 255));
        noStroke();
        game = new Game();
        revertMoveButton = new Textbutton(width - 275, height - 200, "Revert Move");
        botPlayerButton = new Textbutton(width - 290, height - 400, "   Activate Bot", color(20, 220, 40), 190, 50);
        
    }

    public void mousePressed() {
        if(revertMoveButton.isClicked(mouseX, mouseY)) {
            if(!history.empty()) {
                game = history.pop();
            }
        }

        if(botPlayerButton.isClicked(mouseX, mouseY)) {
            botPlayerActivated = !botPlayerActivated;
            if(botPlayerActivated) {
                botPlayerButton.setColor(color(220, 10, 20));
                botPlayerButton.setText("Deactivate Bot");
            } else {
                botPlayerButton.setColor(color(20, 220, 40));
                botPlayerButton.setText("   Activate Bot");
            }
        }


        for(Checker c: game.getPlayingfield()) {
            if(c.isClicked(mouseX, mouseY, 100, playingFieldOffsetX, playingFieldOffsetY)) {
                if(selectedChecker == null) selectedChecker = c;
                else {
                    Game newGame = game.move(selectedChecker.pos, c.pos);
                    if(!newGame.equals(game)) {
                        history.add(game);
                        game = newGame;
                    }
                    selectedChecker = null;
                }
            }
        }
        
    }

    public void draw() {
        //Get possible moves for selected Piece
        if(selectedChecker != null) {
            possibleMovesSelectedChecker = selectedChecker.possibleMoves(game);
        } else {
            possibleMovesSelectedChecker = new ArrayList<>();
        }

        if(botPlayerActivated && game.getPlayer() == Player.ONE) {
            MoveElem bestMove = game.bestMove();
            game = game.move(bestMove.from, bestMove.to);
        }

        if(game.getPossibleMoves().size() == 0 && !game.isGameOver()){
            game = game.changePlayer();
        }




        strokeWeight(4);

        //Draw the Playingfield
        stroke(color(46, 46, 46));
        for(int i = 0; i < 8; i++) {
            for(int j = 0; j < 8; j++) {
                if((i + j) % 2 == 0) {
                    fill(whiteField);
                    rect(playingFieldOffsetX + playingFieldX * j, playingFieldOffsetY + playingFieldY * i, 100, 100);
                } else {
                    fill(darkField);
                    rect(playingFieldOffsetX + playingFieldX * j, playingFieldOffsetY + playingFieldY * i, 100, 100);
                }
            }
        }

        //Draw the Checkerpieces
        for(Checker c: game.getPlayingfield()) {
            boolean isPossibleMove = false;
            int skipFieldOffsetX = (c.x + c.y % 2) * 100;
            int strokeColor = color(46, 46, 46);
            if(c.equals(selectedChecker)) strokeColor = color(255, 0, 0);

            for(MoveElem m : possibleMovesSelectedChecker) {
                if(m.to == c.pos) {
                    isPossibleMove = true;
                    strokeColor = color(31, 218, 255);
                }
            }
            
            if(c.getClass() == Dame.class) { //Draw a dame piece
                fill(c.player == Player.ONE ? playerOneColor : playerTwoColor);
                stroke(strokeColor);
                circle(playingFieldOffsetX + playingFieldX * (c.x) + 50 + skipFieldOffsetX, playingFieldOffsetY + playingFieldY * c.y + 50, 75);
                circle(playingFieldOffsetX + playingFieldX * (c.x) + 50 + skipFieldOffsetX, playingFieldOffsetY + playingFieldY * c.y + 50, 40);
            } else {
                if(c.player == Player.NONE) { //Draw an empty space with stroke if it is a possible move
                    noFill();
                    if(isPossibleMove) stroke(strokeColor);
                    else noStroke();
                } else { //Draw a player piece
                    fill(c.player == Player.ONE ? playerOneColor : playerTwoColor);
                    stroke(strokeColor);
                }
                circle(playingFieldOffsetX + playingFieldX * (c.x) + 50 + skipFieldOffsetX, playingFieldOffsetY + playingFieldY * c.y + 50, 75);
            }
        }

        revertMoveButton.draw(super.g);
        botPlayerButton.draw(super.g);
    }
}


class Textbutton {
    int x, y;
    int width = 155;
    int height = 50;
    String text = "";
    int color = -1;

    Textbutton(int x, int y, String text) {
        this.x = x;
        this.y = y;
        this.text = text;
    }

    Textbutton(int x, int y, String text, int color, int width, int height) {
        this.x = x;
        this.y = y;
        this.text = text;
        this.color = color;
        this.width = width;
        this.height = height;
    }

    void setColor(int color) {
        this.color = color;
    }

    void setText(String t) {
        this.text = t;
    }

    boolean isClicked(int mouseX, int mouseY) {       
        return mouseX >= x - width && mouseX <= x + width && mouseY >= y - height && mouseY <= y + height;
    }

    public void draw(PGraphics g) {
        g.noStroke();
        g.textSize(30);
        g.fill(color == -1 ? g.color(61, 175, 224) : color);
        g.rect(x, y, width, height);
        g.fill(g.color(255, 255, 255));
        g.text(text, x, y + 35);
    }
}



// GAME LOGIC STARTS HERE


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

class MoveElem {
    int from;
    int to;

    MoveElem(int from, int to) {
        this.from = from;
        this.to = to;
    }

    @Override
    public boolean equals(Object other) {
        if(other == null) return false;
        if(other == this) return true;
        if(other.getClass() != getClass()) return false;
        MoveElem that = (MoveElem)other;
        return that.from == this.from && that.to == this.to;
    }

    @Override
    public String toString() {
        return from + "->" + to;
    }
}

interface IGame {
    public Game move(int piecePos, int movePos);
    public Game attack(Checker piece, Checker target, Move move);
    public List<Checker> getPlayingfield();
    public int pieceAmountOfPlayer(Player player);
    public Player getPlayer();
    public Game changePlayer();
    public Checker findPiece(int x, int y); //Needs to be visible for Dame movement check
    public MoveElem bestMove();
    public List<MoveElem> getPossibleMoves();
    default public boolean isGameOver() {
        int pieceAmount1= pieceAmountOfPlayer(Player.ONE); //Pieces of player 1
        int pieceAmount2 = pieceAmountOfPlayer(Player.TWO); //Pieces of player 2
        if(pieceAmount1 <= 0 || pieceAmount2 <= 0) return true;
        return false;
    }
    default public Player isWinning() {
        int pieceAmount1 = pieceAmountOfPlayer(Player.ONE); //Pieces of player 1
        int pieceAmount2 = pieceAmountOfPlayer(Player.TWO); //Pieces of player 2
        if(pieceAmount1 > pieceAmount2) return Player.ONE;
        else if (pieceAmount1 < pieceAmount2) return Player.TWO;
        return Player.NONE;
    }
}

class Game implements IGame {
    List<Checker> checkersList = new ArrayList<>();
    Player player = Player.TWO;
    int boardSize = 32;
    Checker previousMoveChecker = null; //This is the checker that must be used after attacking
    boolean isConsecutiveAttack = false;

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

    public Player getPlayer() {
        return player;
    }

    public List<Checker> getPlayingfield() {
        return this.checkersList;
    }

    public Game changePlayer() {
        Game copy = Game.of(this.checkersList);
        copy.player = this.player == Player.ONE ? Player.TWO : Player.ONE;
        return copy;
    }

    public Game move(int piecePos, int movePos) {
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


        //TODO: Hier folgemove prüfen
        if(previousMoveChecker != null && previousMoveChecker.player == this.player) { //If true, last move was an attack
            if(!piece.equals(previousMoveChecker)) return this; //If selected piece is not he previous, youre not allowed to move
            isConsecutiveAttack = true;
            if(piece.possibleMoves(copy).size() == 0) {
                copy.player = this.player == Player.ONE ? Player.TWO : Player.ONE;
                return copy;
            }; //Piece cant make a move
        }
        

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
            if(!piece.canReach(target, copy)) return this; //throw new IllegalArgumentException("Target cant be reached");
            if(target.alive && target.player != piece.player) {
                return copy.attack(piece, target, move);
            }
        }

        if(!piece.canReach(movePiece, copy)) return this; //throw new IllegalArgumentException("Target cant be reached");

        if(movePiece.alive && movePiece.player != piece.player) {
            return copy.attack(piece, movePiece, move);
        }
        //////////////////////////
        if(isConsecutiveAttack) return this; //Cant make a regular move after consecutive Attack

        if(piece.getClass() == Dame.class) {
            if(!movePiece.alive && movePiece.player == Player.NONE) {
                copy.checkersList.set(movePiece.pos, movePiece.asDame(piece));
                kill(piece, copy);
                copy.player = player == Player.ONE ? Player.TWO : Player.ONE;
                copy.previousMoveChecker = movePiece;
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

        kill(piece, copy);
        copy.previousMoveChecker = movePiece;
        copy.player = player == Player.ONE ? Player.TWO : Player.ONE;

        return copy;
    }

    public Game attack(Checker piece, Checker target, Move move) {
        int movementDirection = piece.player == Player.ONE ? 1 : -1;
        int moveAttackValue = move.attack;
        if(piece.pos + (movementDirection * moveAttackValue) > 31 || piece.pos + (movementDirection * moveAttackValue) < 0) {
            return this;
        }
        Checker landingChecker = findPiece(piece.pos + (movementDirection * moveAttackValue));
        if(((landingChecker.pos / 4) % 2) != ((piece.pos / 4) % 2)) return this; //throw new IllegalArgumentException("Piece cant land behind attacked piece");
        if(landingChecker.player != Player.NONE) return this;

        if((piece.player == Player.ONE && landingChecker.pos >= 28) || (piece.player == Player.TWO && landingChecker.pos <= 3) || (piece.getClass() == Dame.class)) {
            checkersList.set(landingChecker.pos, landingChecker.asDame(piece)); //If at the end of the game, piece becomes a Dame
        } else {
            landingChecker.become(piece);
        }
        this.player = piece.player;
        this.previousMoveChecker = landingChecker;
        kill(target, this);
        kill(piece, this);
        return this;
    }

    void kill(Checker c, Game g) {
        Checker d = new Checker(Player.NONE, c.x, c.y, c.pos);
        d.kill();
        g.checkersList.set(c.pos, d);
    }

    public Checker findPiece(int x, int y) {
        assert x >= 0 && x < 4 && y >= 0 && y < 8;
        Optional<Checker> pieceOptional = checkersList.stream().filter(c -> c.x == x && c.y == y).findFirst();
        if(!pieceOptional.isPresent()) throw new IllegalArgumentException("No piece found " + x + " " + y);
        return pieceOptional.get();
    }

    Checker findPiece(int pos) {
        Optional<Checker> pieceOptional = checkersList.stream().filter(c -> c.pos == pos).findFirst();
        if(!pieceOptional.isPresent()) throw new IllegalArgumentException("No piece found " + pos);
        return pieceOptional.get();
    }

    public int pieceAmountOfPlayer(Player player) {
        return (int)checkersList.stream().filter(item -> item.alive && item.player == player).count();
    }


    public List<MoveElem> getPossibleMoves() {
        List<MoveElem> moves = new ArrayList<>();
        for(Checker c: checkersList) {
            if(c.player != this.player) continue; //Not this players turn
            c.possibleMoves(this).stream().forEach(m -> moves.add(m));
        }
        
        return moves;
    }


    //TODO: Bestmove ist nicht immer der beste move
    //bot spielt manchmal keinen move aber das spiel geht doch weiter
    //PossibleMoves methode von Checker prüfen
    //TODO: Zweiter zug muss folgezug sein
    public MoveElem bestMove() {
        assert !this.isGameOver();

        int bestValue = Integer.MIN_VALUE;
        MoveElem bestMove = null;
        List<MoveElem> possibleMoves = this.getPossibleMoves();
        System.out.println(possibleMoves);
        for(MoveElem m: possibleMoves) {
            Game nextGame = this.move(m.from, m.to);
            int moveValue = miniMax(nextGame, false, 4);

            if(moveValue > bestValue) {
                bestValue = moveValue;
                bestMove = m;
            }
        }

        return bestMove;
    }

    private int miniMax(Game g, boolean isMaximizingPlayer, int depth) {
        if(g.isGameOver() || depth == 0) {
            return evaluate(g, isMaximizingPlayer);
        }

        int bestValue = isMaximizingPlayer ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        List<MoveElem> possibleMoves = g.getPossibleMoves();
        
        for(MoveElem m: possibleMoves) {
            Game nextGame = g.move(m.from, m.to);
            int childValue = miniMax(nextGame, !isMaximizingPlayer, depth - 1);
            bestValue = isMaximizingPlayer ? Math.max(bestValue, childValue) : Math.min(bestValue, childValue);
        }

        return bestValue;
    }

    private int evaluate(Game g, boolean isMaximizingPlayer) {
        if(g.isGameOver()){
            if(isMaximizingPlayer) return Integer.MIN_VALUE;
            else if(!isMaximizingPlayer) return Integer.MAX_VALUE;
            else return 0;
        }

        int maximizingPlayerCount = isMaximizingPlayer ? g.pieceAmountOfPlayer(g.player) : g.pieceAmountOfPlayer(g.player == Player.ONE ? Player.TWO : Player.ONE);
        int minimizingPlayerCount = !isMaximizingPlayer ? g.pieceAmountOfPlayer(g.player) : g.pieceAmountOfPlayer(g.player == Player.ONE ? Player.TWO : Player.ONE);
        int eval = maximizingPlayerCount - minimizingPlayerCount;


        return isMaximizingPlayer ? eval : -eval;
    }

    public boolean equalsWithoutPlayer(Object other) {
        if(other == null) return false;
        if(other == this) return true;
        if(other.getClass() != getClass()) return false;
        Game that = (Game)other;
        return that.checkersList.equals(checkersList);
    }

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

    //This method is used for Processing mouse click check
    boolean isClicked(int mouseX, int mouseY, int diameter, int playingFieldOffsetX, int playingFieldOffsetY) {
        int skipFieldOffsetX = (x + y % 2) * 100;
        int checkValueX = playingFieldOffsetX + 100 * (x) + 50 + skipFieldOffsetX;
        int checkValueY = playingFieldOffsetY + 100 * (y) + 50;

        return mouseX >= checkValueX - diameter/2 && mouseX <= checkValueX + diameter/2 && mouseY >= checkValueY - diameter/2 && mouseY <= checkValueY + diameter/2;
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
    List<MoveElem> possibleMoves(IGame g) {
        List<MoveElem> movePositions = new ArrayList<>();
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
        
            if((!copy.equalsWithoutPlayer(g)) && (pos + moveValue >= 0) && (pos + moveValue < 32)) {
                movePositions.add(new MoveElem(pos, pos + moveValue));
            }
        }

        return movePositions;
    }

    //Checks if target is in reach (1 row up or down)
    boolean canReach(Checker target, IGame g) {
        int rowNum = getRowModulo();
        Move move = retrieveMoveTo(target);
        if(rowNum != target.getRowModulo() && move != Move.NONE) return true;
        return false;
    }

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
        return "(" + this.x + "/" + this.y + ") Pos:" + this.pos + " Team:" + this.player + " TYPE: " + this.getClass();
    }
}

class Dame extends Checker {
    Dame(Player player, int x, int y, int pos) {
        super(player, x, y, pos);
    }

    @Override
    public Dame clone() {
        return new Dame(this.player, this.x, this.y, this.pos);
    }

    //PossibleMoves
    @Override
    List<MoveElem> possibleMoves(IGame g) {
        List<MoveElem> movePositions = new ArrayList<>();
        if(this.player != g.getPlayer()) return movePositions; 
        for(Checker c: g.getPlayingfield()) {
            if(this.canReach(c, g)) {
                if(c.alive && (player == Player.ONE ? c.y - this.y > 1 : c.y - this.y < -1)) continue; //If target is enemy piece and is not in attack range
                if(c.alive && c.player != player) { //Check if attack is possible, if not, skip
                    Game copy = Game.of(g.getPlayingfield());
                    if(copy.attack(this, c, this.retrieveMoveTo(c)).equalsWithoutPlayer(copy)) continue;
                }

                movePositions.add(new MoveElem(pos, c.pos));
            }
        }
        return movePositions;
    }
    
    //checks if there is a piece in the way from the current one to the target
    @Override
    boolean canReach(Checker target, IGame g) {
        if(player == Player.NONE) return false; //empty field cant Move
        if(target.player == player) return false; //cant move on team piece
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
/*
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

//Dame movement
assert !d.equals(d.move(0, 9)): "Move dame multiple backright";
*/