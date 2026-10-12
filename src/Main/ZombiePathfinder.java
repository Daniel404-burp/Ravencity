package Main;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

public class ZombiePathfinder {

    private static final int STEP = 32;
    private static final int SEARCH_RADIUS = 24;

    private static class Node {

        int x;
        int y;
        Node parent;

        Node(int x, int y, Node parent) {
            this.x = x;
            this.y = y;
            this.parent = parent;
        }
    }

    // =========================================================
    // FIND NEXT STEP
    // =========================================================

    public static Point findNextStep(
            Map map,
            int startX,
            int startY,
            int targetX,
            int targetY,
            int offsetX,
            int offsetY,
            int width,
            int height
    ) {

        Queue<Node> queue = new ArrayDeque<>();
        Set<String> visited = new HashSet<>();

        Node start = new Node(startX, startY, null);

        queue.add(start);
        visited.add(key(startX, startY));

        Node goal = null;

        while (!queue.isEmpty()) {

            Node current = queue.poll();

            // Close enough to Ethan
            if (Math.abs(current.x - targetX) <= SEARCH_RADIUS &&
                Math.abs(current.y - targetY) <= SEARCH_RADIUS) {

                goal = current;
                break;
            }

            addNode(
                    map,
                    queue,
                    visited,
                    current,
                    current.x + STEP,
                    current.y,
                    offsetX,
                    offsetY,
                    width,
                    height
            );

            addNode(
                    map,
                    queue,
                    visited,
                    current,
                    current.x - STEP,
                    current.y,
                    offsetX,
                    offsetY,
                    width,
                    height
            );

            addNode(
                    map,
                    queue,
                    visited,
                    current,
                    current.x,
                    current.y + STEP,
                    offsetX,
                    offsetY,
                    width,
                    height
            );

            addNode(
                    map,
                    queue,
                    visited,
                    current,
                    current.x,
                    current.y - STEP,
                    offsetX,
                    offsetY,
                    width,
                    height
            );
        }

        if (goal == null) {
            return null;
        }

        // Walk backwards until we reach
        // the first step after the zombie.
        Node current = goal;

        while (current.parent != null &&
               current.parent.parent != null) {

            current = current.parent;
        }

        return new Point(current.x, current.y);
    }

    // =========================================================
    // ADD NODE
    // =========================================================

    private static void addNode(
            Map map,
            Queue<Node> queue,
            Set<String> visited,
            Node parent,
            int x,
            int y,
            int offsetX,
            int offsetY,
            int width,
            int height
    ) {

        if (x < 0 ||
            y < 0 ||
            x >= Map.WORLD_WIDTH ||
            y >= Map.WORLD_HEIGHT) {

            return;
        }

        String id = key(x, y);

        if (visited.contains(id)) {
            return;
        }

        Rectangle bounds = new Rectangle(
                x + offsetX,
                y + offsetY,
                width,
                height
        );

        // Building collision blocks this node.
        if (map.collidesWithBuilding(bounds)) {
            return;
        }

        visited.add(id);

        queue.add(
                new Node(
                        x,
                        y,
                        parent
                )
        );
    }

    // =========================================================
    // KEY
    // =========================================================

    private static String key(int x, int y) {
        return x + ":" + y;
    }
}