package Main;

import java.util.ArrayList;
import java.util.List;

public class Inventory {

    private List<Item> items;

    public Inventory() {

        items = new ArrayList<>();
    }

    public void addItem(Item item) {

        if (item == null) {
            return;
        }

        // Try to stack with an existing item.
        for (Item existing : items) {

            if (canStack(existing, item)) {

                existing.addAmount(item.getAmount());
                return;
            }
        }

        // No matching item found.
        items.add(item);
    }

    private boolean canStack(Item first, Item second) {

        if (first == null || second == null) {
            return false;
        }

        if (first.getType() != second.getType()) {
            return false;
        }

        return first.getName().equalsIgnoreCase(
                second.getName()
        );
    }

    public void removeItem(Item item) {

        if (item == null) {
            return;
        }

        items.remove(item);
    }

    public boolean removeItemAmount(
            Item item,
            int amount
    ) {

        if (item == null || amount <= 0) {
            return false;
        }

        if (!items.contains(item)) {
            return false;
        }

        if (item.getAmount() < amount) {
            return false;
        }

        item.removeAmount(amount);

        if (item.getAmount() <= 0) {
            items.remove(item);
        }

        return true;
    }

    public Item findItem(ItemType type) {

        if (type == null) {
            return null;
        }

        for (Item item : items) {

            if (item.getType() == type) {
                return item;
            }
        }

        return null;
    }

    public Item findItem(String name) {

        if (name == null) {
            return null;
        }

        for (Item item : items) {

            if (item.getName().equalsIgnoreCase(name)) {
                return item;
            }
        }

        return null;
    }

    public boolean hasItem(ItemType type) {

        return findItem(type) != null;
    }

    public boolean hasItem(String name) {

        return findItem(name) != null;
    }

    public List<Item> getItems() {

        return items;
    }

    public int getItemCount() {

        return items.size();
    }

    public void clear() {

        items.clear();
    }
}