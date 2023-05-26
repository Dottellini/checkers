class Game {
    int boardSize = 32; //8x8
    int[] board = new int[boardSize];

    Game() {
        for(int i = 0; i < boardSize; i++) {
            board[i] = i < 12 ? 1 : (i > 19 ? 2 : 0);
        }
    }

    //move can be "0" or "1". 0 is a move left, 1 is a move right
    void move(int piecePos, int move) {
        
    }

    @Override
    public String toString() {
        String[] checkerSymbols = new String[]{"| |", "S", "K"};
        String s = "";

        for(int i = 0; i < 64; i++) {
            if(i % 2 == 1) {
                s += checkerSymbols[board[i]];
            } else {
                s += "| |";
            }
        }
        return s;
    }
}