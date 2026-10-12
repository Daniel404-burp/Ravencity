package Main;

public class Item {

    private String name;
    private ItemType type;
    private int amount;

    public Item(String name, ItemType type, int amount) {

        this.name = name;
        this.type = type;
        this.amount = Math.max(1, amount);
    }

    public String getName() {
        return name;
    }

    public ItemType getType() {
        return type;
    }

    public int getAmount() {
        return amount;
    }

    public void addAmount(int amount) {

        if (amount <= 0) {
            return;
        }

        this.amount += amount;
    }

    public boolean removeAmount(int amount) {

        if (amount <= 0) {
            return false;
        }

        if (amount > this.amount) {
            return false;
        }

        this.amount -= amount;

        return true;
    }
}