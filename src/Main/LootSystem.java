package Main;

import java.util.Random;

public class LootSystem {

    private static final Random random = new Random();

    // =========================================================
    // GENERATE RANDOM LOOT
    // =========================================================

    public static Item generateLoot() {

        int roll = random.nextInt(100);

        // 50% Ammo
        if (roll < 50) {

            int amount =
                    6 + random.nextInt(13);

            return new Ammo(amount);
        }

        // 35% Healing
        if (roll < 85) {

            return new HealingItem(1);
        }

        // 15% Key
        return createKey();
    }

    // =========================================================
    // CREATE AMMO
    // =========================================================

    public static Ammo createAmmo(int amount) {

        return new Ammo(
                Math.max(1, amount)
        );
    }

    // =========================================================
    // CREATE HEALING
    // =========================================================

    public static HealingItem createHealing(int amount) {

        return new HealingItem(
                Math.max(1, amount)
        );
    }

    // =========================================================
    // CREATE KEY
    // =========================================================

    public static Item createKey() {

        return new Item(
                "Key",
                ItemType.KEY,
                1
        );
    }

    // =========================================================
    // FILL CONTAINER
    // =========================================================

    public static void fillContainer(
            LootContainer container) {

        if (container == null) {
            return;
        }

        container.setItem(
                generateLoot()
        );
    }
}