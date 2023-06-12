package Game;

enum Player {
    ONE(0),
    TWO(1),
    NONE(2);

    public final int value;

    private Player(int value) {
        this.value = value;
    }
}
