package Game;

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
