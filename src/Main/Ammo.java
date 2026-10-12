package Main;

public class Ammo extends Item {

    public Ammo() {
        this(1);
    }

    public Ammo(int amount) {
        super("9mm Ammo", ItemType.AMMO, amount <= 0 ? 1 : amount);
    }
}
