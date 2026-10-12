package Main;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.util.ArrayList;
import java.util.List;

public class Map {

    // =========================================================
    // WORLD SETTINGS
    // =========================================================

    public static final int WORLD_WIDTH = 2000;
    public static final int WORLD_HEIGHT = 2000;

    public static final int TILE_SIZE = 64;

    // =========================================================
    // BUILDING SETTINGS
    // =========================================================

    private static final int WALL_THICKNESS = 18;

    // Width of the doorway opening
    private static final int DOOR_WIDTH = 80;

    // =========================================================
    // FOG OF WAR / EXPLORATION
    // =========================================================

    private static final int FOG_TILE_SIZE = 64;
    private static final int FOG_REVEAL_RADIUS = 320;
    private static final int FOG_COLS =
            (WORLD_WIDTH + FOG_TILE_SIZE - 1) / FOG_TILE_SIZE;
    private static final int FOG_ROWS =
            (WORLD_HEIGHT + FOG_TILE_SIZE - 1) / FOG_TILE_SIZE;

    private final boolean[][] explored =
            new boolean[FOG_ROWS][FOG_COLS];

    // =========================================================
    // BUILDING DATA
    // =========================================================

    private final List<Building> buildings =
            new ArrayList<>();

    private final List<Door> doors =
            new ArrayList<>();

    private final List<Window> windows =
            new ArrayList<>();

    // =========================================================
    // INVESTIGATION
    // =========================================================

    private final Investigation investigation =
            new Investigation();

    // =========================================================
    // MAP ART TILESET
    // =========================================================

    private final Tile tileSet = new Tile();

    private static final int ART_TILE_SIZE = 32;

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public Map() {

        // -----------------------------------------------------
        // TOP ROW
        // -----------------------------------------------------

        addBuilding(
                100,
                100,
                280,
                280,
                "HOUSE"
        );

        addBuilding(
                800,
                100,
                300,
                280,
                "HOUSE"
        );

        addBuilding(
                1550,
                100,
                300,
                280,
                "HOUSE"
        );

        // -----------------------------------------------------
        // MIDDLE ROW
        // -----------------------------------------------------

        addBuilding(
                100,
                800,
                280,
                220,
                "SHOP"
        );

        addBuilding(
                800,
                800,
                300,
                220,
                "SHOP"
        );

        addBuilding(
                1550,
                800,
                300,
                220,
                "HOUSE"
        );

        // -----------------------------------------------------
        // BOTTOM ROW
        // -----------------------------------------------------

        addBuilding(
                800,
                1400,
                400,
                300,
                "CLINIC"
        );

        addBuilding(
                100,
                1400,
                280,
                250,
                "HOUSE"
        );

        addBuilding(
                1550,
                1400,
                300,
                250,
                "HOUSE"
        );

        // =====================================================
        // INVESTIGATION POINTS
        // =====================================================

        investigation.addPoint(
                new InvestigationPoint(
                        900,
                        880,
                        32,
                        32,
                        "Medical Report",
                        "The report mentions unusual cellular recovery in Subject E-07."
                )
        );

        investigation.addPoint(
                new InvestigationPoint(
                        1120,
                        1510,
                        32,
                        32,
                        "Bloodstained Record",
                        "Someone tried to hide this record. The patient number is E-07."
                )
        );
    }

    // =========================================================
    // ADD BUILDING
    // =========================================================

    private void addBuilding(
            int x,
            int y,
            int width,
            int height,
            String name
    ) {

        buildings.add(
                new Building(
                        x,
                        y,
                        width,
                        height,
                        name
                )
        );

        int doorX =
                x + (width - DOOR_WIDTH) / 2;

        int doorY =
                y + height - WALL_THICKNESS;

        doors.add(
                new Door(
                        doorX,
                        doorY,
                        DOOR_WIDTH,
                        WALL_THICKNESS
                )
        );

        // Two side windows per building.
        int windowWidth = 48;
        int windowHeight = WALL_THICKNESS;
        int windowY = y + height / 2 - windowWidth / 2;

        windows.add(
                new Window(
                        x,
                        windowY,
                        windowHeight,
                        windowWidth
                )
        );

        windows.add(
                new Window(
                        x + width - WALL_THICKNESS,
                        windowY,
                        windowHeight,
                        windowWidth
                )
        );
    }

    // =========================================================
    // DRAW MAP
    // =========================================================

    public void draw(
            Graphics2D g2,
            int playerX,
            int playerY
    ) {

        // =====================================================
        // TILESET MAP ART
        // =====================================================

        drawGroundTiles(g2);
        drawRoadTiles(g2);

        // =====================================================
        // UPDATE EXPLORATION
        // =====================================================

        revealAroundPlayer(playerX + 32, playerY + 32);

        // =====================================================
        // BUILDINGS
        // =====================================================

        for (Building building : buildings) {

            if (building.containsPlayer(
                    playerX + 32,
                    playerY + 32
            )) {

                // Ethan is INSIDE.
                // Draw the interior instead of the roof.

                drawInterior(
                        g2,
                        building
                );

            } else {

                // Ethan is OUTSIDE.
                // Draw the dark roof.

                drawRoof(
                        g2,
                        building
                );
            }
        }

        // Investigation clues
        investigation.draw(g2);

        // =====================================================
        // FOG OF WAR
        // =====================================================

        drawFog(g2);

        // =====================================================
        // DOWNTOWN GATE
        // =====================================================

        drawGate(g2);

        // =====================================================
        // WORLD BORDER
        // =====================================================

        g2.setColor(Color.BLACK);

        g2.drawRect(
                0,
                0,
                WORLD_WIDTH - 1,
                WORLD_HEIGHT - 1
        );
    }

    // =========================================================
    // TILESET DRAWING
    // =========================================================

    private void drawGroundTiles(Graphics2D g2) {

        // Fill the entire 2000x2000 world with reusable 32x32 grass tiles.
        for (int y = 0; y < WORLD_HEIGHT; y += ART_TILE_SIZE) {
            for (int x = 0; x < WORLD_WIDTH; x += ART_TILE_SIZE) {

                int variant = groundVariant(x, y);
                tileSet.draw(g2, variant, 0, x, y);
            }
        }
    }

    private int groundVariant(int worldX, int worldY) {

        // Deterministic variation: the same world tile always gets the same art.
        int hash = worldX * 73856093 ^ worldY * 19349663;
        hash ^= (hash >>> 13);
        hash &= 0x7fffffff;

        // Mostly grass, with occasional dirt/stones/dead-grass details.
        int roll = hash % 100;

        if (roll < 62) return 0; // Basic dark grass
        if (roll < 76) return 1; // Lighter grass
        if (roll < 86) return 2; // Dense grass
        if (roll < 94) return 3; // Sparse grass
        if (roll < 97) return 4; // Small dirt patch
        if (roll < 99) return 5; // Small stone detail
        return 6;                 // Dead grass detail
    }

    private void drawRoadTiles(Graphics2D g2) {

        // Existing Chapter 2 road layout is preserved exactly.
    	drawHorizontalRoad(g2, 500);
    	drawHorizontalRoad(g2, 1100);

    	drawVerticalRoad(g2, 500);
    	drawVerticalRoad(g2, 1300);

    	drawRoadIntersections(g2);
    }
    private void drawRotatedTiledRect(
            Graphics2D g2,
            int x,
            int y,
            int width,
            int height,
            int column,
            int row,
            double degrees
    ) {

        Shape oldClip = g2.getClip();

        g2.clipRect(
                x,
                y,
                width,
                height
        );

        for (
                int drawY = y;
                drawY < y + height;
                drawY += ART_TILE_SIZE
        ) {

            for (
                    int drawX = x;
                    drawX < x + width;
                    drawX += ART_TILE_SIZE
            ) {

                tileSet.drawRotated(
                        g2,
                        column,
                        row,
                        drawX,
                        drawY,
                        degrees
                );
            }
        }

        g2.setClip(oldClip);
    }
    private void drawHorizontalRoad(Graphics2D g2, int roadY) {

        final int roadHeight = 180;
        final int sidewalk = 32;

        // =====================================================
        // HORIZONTAL SIDEWALKS
        // Rotate the vertical sidewalk tile 90 degrees.
        // =====================================================

        drawRotatedTiledRect(
                g2,
                0,
                roadY - sidewalk,
                WORLD_WIDTH,
                sidewalk,
                0,
                4,
                90
        );

        drawRotatedTiledRect(
                g2,
                0,
                roadY + roadHeight,
                WORLD_WIDTH,
                sidewalk,
                0,
                4,
                90
        );

        // =====================================================
        // HORIZONTAL ASPHALT
        // Rotate the road texture 90 degrees.
        // =====================================================

        drawRotatedTiledRect(
                g2,
                0,
                roadY,
                WORLD_WIDTH,
                roadHeight,
                0,
                2,
                90
        );

        // =====================================================
        // HORIZONTAL ROAD EDGES
        // Rotate the edge tile 90 degrees.
        // =====================================================

        drawRotatedTiledRect(
                g2,
                0,
                roadY,
                WORLD_WIDTH,
                ART_TILE_SIZE,
                0,
                3,
                90
        );

        drawRotatedTiledRect(
                g2,
                0,
                roadY + roadHeight - ART_TILE_SIZE,
                WORLD_WIDTH,
                ART_TILE_SIZE,
                0,
                3,
                90
        );

        // =====================================================
        // CENTER ROAD MARKINGS
        // KEEP THIS UNROTATED
        // =====================================================

        for (int x = 0; x < WORLD_WIDTH; x += 100) {

            tileSet.draw(
                    g2,
                    0,
                    6,
                    x,
                    roadY + 74
            );
        }
    }

    private void drawVerticalRoad(Graphics2D g2, int roadX) {

        final int roadWidth = 180;
        final int sidewalk = 32;

        drawTiledRect(g2, roadX - sidewalk, 0, sidewalk, WORLD_HEIGHT, 0, 4);
        drawTiledRect(g2, roadX + roadWidth, 0, sidewalk, WORLD_HEIGHT, 0, 4);

        drawTiledRect(g2, roadX, 0, roadWidth, WORLD_HEIGHT, 0, 2);

        drawTiledRect(g2, roadX, 0, ART_TILE_SIZE, WORLD_HEIGHT, 0, 3);
        drawTiledRect(g2, roadX + roadWidth - ART_TILE_SIZE, 0,
                ART_TILE_SIZE, WORLD_HEIGHT, 0, 3);

        for (int y = 0; y < WORLD_HEIGHT; y += 100) {
            tileSet.drawRotated(
                g2,
                0,
                6,
                roadX + 74,
                y,
                90
            );
        }
    }

    private void drawTiledRect(
            Graphics2D g2,
            int x,
            int y,
            int width,
            int height,
            int column,
            int row
    ) {

        Shape oldClip = g2.getClip();
        g2.clipRect(x, y, width, height);

        for (int drawY = y; drawY < y + height; drawY += ART_TILE_SIZE) {
            for (int drawX = x; drawX < x + width; drawX += ART_TILE_SIZE) {

                tileSet.draw(g2, column, row, drawX, drawY);
            }
        }

        g2.setClip(oldClip);
    }
    private void drawRoadIntersections(Graphics2D g2) {
        final int roadSize = 180;

        // Asphalt crossing area
        drawTiledRect(g2, 500, 500, roadSize, roadSize, 0, 2);
        drawTiledRect(g2, 1300, 500, roadSize, roadSize, 0, 2);
        drawTiledRect(g2, 500, 1100, roadSize, roadSize, 0, 2);
        drawTiledRect(g2, 1300, 1100, roadSize, roadSize, 0, 2);
    }

    // =========================================================
    // FOG OF WAR METHODS
    // =========================================================

    private void revealAroundPlayer(int centerX, int centerY) {

        int minX = Math.max(0, centerX - FOG_REVEAL_RADIUS);
        int maxX = Math.min(WORLD_WIDTH - 1, centerX + FOG_REVEAL_RADIUS);
        int minY = Math.max(0, centerY - FOG_REVEAL_RADIUS);
        int maxY = Math.min(WORLD_HEIGHT - 1, centerY + FOG_REVEAL_RADIUS);

        int startCol = minX / FOG_TILE_SIZE;
        int endCol = maxX / FOG_TILE_SIZE;
        int startRow = minY / FOG_TILE_SIZE;
        int endRow = maxY / FOG_TILE_SIZE;

        int radiusSquared = FOG_REVEAL_RADIUS * FOG_REVEAL_RADIUS;

        for (int row = startRow; row <= endRow; row++) {
            for (int col = startCol; col <= endCol; col++) {

                int tileCenterX = col * FOG_TILE_SIZE + FOG_TILE_SIZE / 2;
                int tileCenterY = row * FOG_TILE_SIZE + FOG_TILE_SIZE / 2;

                int dx = tileCenterX - centerX;
                int dy = tileCenterY - centerY;

                if (dx * dx + dy * dy <= radiusSquared) {
                    explored[row][col] = true;
                }
            }
        }
    }

    private void drawFog(Graphics2D g2) {

        g2.setColor(new Color(0, 0, 0, 235));

        for (int row = 0; row < FOG_ROWS; row++) {
            for (int col = 0; col < FOG_COLS; col++) {

                if (explored[row][col]) {
                    continue;
                }

                int x = col * FOG_TILE_SIZE;
                int y = row * FOG_TILE_SIZE;

                int width = Math.min(FOG_TILE_SIZE, WORLD_WIDTH - x);
                int height = Math.min(FOG_TILE_SIZE, WORLD_HEIGHT - y);

                g2.fillRect(x, y, width, height);
            }
        }
    }

    public boolean isAreaExplored(int worldX, int worldY) {

        if (worldX < 0 || worldX >= WORLD_WIDTH
                || worldY < 0 || worldY >= WORLD_HEIGHT) {
            return false;
        }

        return explored[worldY / FOG_TILE_SIZE][worldX / FOG_TILE_SIZE];
    }

    public double getExplorationPercent() {

        int exploredTiles = 0;
        int totalTiles = FOG_ROWS * FOG_COLS;

        for (int row = 0; row < FOG_ROWS; row++) {
            for (int col = 0; col < FOG_COLS; col++) {
                if (explored[row][col]) {
                    exploredTiles++;
                }
            }
        }

        return totalTiles == 0
                ? 0.0
                : (exploredTiles * 100.0) / totalTiles;
    }

    // =========================================================
    // BUILDING EXTERIOR / ROOF
    // =========================================================

    private void drawRoof(Graphics2D g2, Building building) {

        if (!tileSet.isBuildingLoaded()) {
            g2.setColor(new Color(20, 20, 20));
            g2.fillRect(building.x, building.y, building.width, building.height);
            return;
        }

        // Outside view:
        // 1. Complete opaque roof plane
        // 2. Thin top-down wall perimeter
        // 3. Top-down windows
        // 4. Top-down door
        drawRoofTiles(g2, building);
        drawExteriorWalls(g2, building);
        drawExteriorWindows(g2, building);
        drawBuildingDoor(g2, building);
    }

    // =========================================================
    // ROOF TILE ASSEMBLY
    // New sheet:
    // Row 0 = roof top edge
    // Row 1 = roof center / side edges
    // Row 2 = roof bottom edge
    // =========================================================

    private void drawRoofTiles(Graphics2D g2, Building building) {

        final int T = ART_TILE_SIZE;
        final int x = building.x;
        final int y = building.y;
        final int right = x + building.width;
        final int bottom = y + building.height;

        // First cover the entire building so NO grass can leak through.
        drawBuildingTiledRect(g2, x, y, building.width, building.height, 2, 1);

        // Top edge.
        drawBuildingTiledRect(
                g2,
                x + T,
                y,
                Math.max(0, building.width - 2 * T),
                T,
                1,
                0
        );

        // Bottom edge.
        drawBuildingTiledRect(
                g2,
                x + T,
                bottom - T,
                Math.max(0, building.width - 2 * T),
                T,
                1,
                2
        );

        // Left edge.
        drawBuildingTiledRect(
                g2,
                x,
                y + T,
                T,
                Math.max(0, building.height - 2 * T),
                0,
                1
        );

        // Right edge.
        drawBuildingTiledRect(
                g2,
                right - T,
                y + T,
                T,
                Math.max(0, building.height - 2 * T),
                6,
                1
        );

        // Four corners.
        drawBuildingTile(g2, 0, 0, x, y);
        drawBuildingTile(g2, 7, 0, right - T, y);
        drawBuildingTile(g2, 0, 2, x, bottom - T);
        drawBuildingTile(g2, 7, 2, right - T, bottom - T);
    }

    // =========================================================
    // THIN TOP-DOWN EXTERIOR WALLS
    // Row 3 of the new sheet.
    // =========================================================

    private void drawExteriorWalls(Graphics2D g2, Building building) {

        final int T = ART_TILE_SIZE;
        final int x = building.x;
        final int y = building.y;
        final int right = x + building.width;
        final int bottom = y + building.height;

        // The new wall art is intentionally thin and top-down.
        // We draw it over the roof perimeter instead of using the old
        // front-facing facade tiles.

        // Top wall.
        drawBuildingTiledRect(
                g2, x, y, building.width, T, 0, 3
        );

        // Left wall.
        drawBuildingTiledRect(
                g2,
                x,
                y + T,
                T,
                Math.max(0, building.height - 2 * T),
                2,
                3
        );

        // Right wall.
        drawBuildingTiledRect(
                g2,
                right - T,
                y + T,
                T,
                Math.max(0, building.height - 2 * T),
                2,
                3
        );

        // Bottom wall is split around the actual 80px collision doorway.
        int doorX = x + (building.width - DOOR_WIDTH) / 2;
        int bottomY = bottom - T;

        drawBuildingTiledRect(
                g2,
                x,
                bottomY,
                Math.max(0, doorX - x),
                T,
                0,
                3
        );

        drawBuildingTiledRect(
                g2,
                doorX + DOOR_WIDTH,
                bottomY,
                Math.max(0, right - (doorX + DOOR_WIDTH)),
                T,
                0,
                3
        );
    }

    // =========================================================
    // TOP-DOWN WINDOWS
    // Row 4:
    // C0 standard window
    // C1 dirty variation
    // C2 narrow window
    // C3 damaged window
    // C6 barred window
    // C7 covered window
    // =========================================================

    private void drawExteriorWindows(Graphics2D g2, Building building) {

        final int T = ART_TILE_SIZE;
        final int x = building.x;
        final int y = building.y;
        final int right = x + building.width;
        final int centerY = y + (building.height - T) / 2;

        // Keep Window objects for interaction/collision.
        // Only their old placeholder rendering is removed.

        drawBuildingTile(g2, 0, 4, x, centerY);
        drawBuildingTile(g2, 1, 4, right - T, centerY);
    }

    // =========================================================
    // BUILDING TILE HELPERS
    // =========================================================

    private void drawBuildingTile(
            Graphics2D g2,
            int column,
            int row,
            int x,
            int y
    ) {
        tileSet.drawBuilding(g2, column, row, x, y);
    }

    private void drawBuildingTiledRect(
            Graphics2D g2,
            int x,
            int y,
            int width,
            int height,
            int column,
            int row
    ) {

        if (width <= 0 || height <= 0) {
            return;
        }

        Shape oldClip = g2.getClip();
        g2.clipRect(x, y, width, height);

        for (int drawY = y; drawY < y + height; drawY += ART_TILE_SIZE) {
            for (int drawX = x; drawX < x + width; drawX += ART_TILE_SIZE) {
                tileSet.drawBuilding(
                        g2,
                        column,
                        row,
                        drawX,
                        drawY
                );
            }
        }

        g2.setClip(oldClip);
    }

    // =========================================================
    // DOOR
    // Row 4:
    // C4 = closed door
    // C5 = open door
    // =========================================================

    private void drawBuildingDoor(Graphics2D g2, Building building) {

        Door door = getDoorForBuilding(building);

        if (door == null || !tileSet.isBuildingLoaded()) {
            return;
        }

        final int T = ART_TILE_SIZE;
        final int visualWidth = 32;
        final int visualHeight = 32;

        int doorCenterX = door.getX() + door.getWidth() / 2;
        int visualX = doorCenterX - visualWidth / 2;
        int visualY = building.y + building.height - visualHeight;

        // Fill the complete collision doorway with a dark opening first.
        g2.setColor(new Color(8, 8, 8));
        g2.fillRect(
                door.getX(),
                building.y + building.height - T,
                door.getWidth(),
                T
        );

        int column = door.isOpen() ? 5 : 4;

        Shape oldClip = g2.getClip();
        g2.clipRect(
                door.getX(),
                building.y + building.height - T,
                door.getWidth(),
                T
        );

        tileSet.drawBuilding(
                g2,
                column,
                4,
                visualX,
                visualY
        );

        g2.setClip(oldClip);
    }

    // =========================================================
    // INTERIOR
    // =========================================================

    private void drawInterior(Graphics2D g2, Building building) {

        if (!tileSet.isBuildingLoaded()) {
            g2.setColor(new Color(85, 80, 74));
            g2.fillRect(
                    building.x,
                    building.y,
                    building.width,
                    building.height
            );
            return;
        }

        // No roof while Ethan is inside.
        // The entire footprint becomes dirty concrete floor.
        drawInteriorFloor(g2, building);

        // Thin perimeter walls remain visible.
        drawInteriorWalls(g2, building);

        // Same real Door object controls the visual open/closed state.
        drawBuildingDoor(g2, building);

        // Sparse damage/stain details only.
        drawInteriorDetails(g2, building);
    }

    // =========================================================
    // INTERIOR FLOOR
    // Row 5 = clean/worn concrete variants
    // =========================================================

    private void drawInteriorFloor(Graphics2D g2, Building building) {

        final int T = ART_TILE_SIZE;
        Shape oldClip = g2.getClip();
        g2.clipRect(
                building.x,
                building.y,
                building.width,
                building.height
        );

        int variant = 0;

        for (int y = building.y; y < building.y + building.height; y += T) {
            for (int x = building.x; x < building.x + building.width; x += T) {

                // Row 5 has eight floor variants.
                tileSet.drawBuilding(
                        g2,
                        variant % 8,
                        5,
                        x,
                        y
                );

                variant++;
            }
        }

        g2.setClip(oldClip);
    }

    // =========================================================
    // INTERIOR WALLS
    // Row 3 is used for the thin perimeter.
    // =========================================================

    private void drawInteriorWalls(Graphics2D g2, Building building) {

        final int T = ART_TILE_SIZE;
        final int x = building.x;
        final int y = building.y;
        final int right = x + building.width;
        final int bottom = y + building.height;

        drawBuildingTiledRect(
                g2, x, y, building.width, T, 0, 3
        );

        drawBuildingTiledRect(
                g2,
                x,
                y + T,
                T,
                Math.max(0, building.height - 2 * T),
                2,
                3
        );

        drawBuildingTiledRect(
                g2,
                right - T,
                y + T,
                T,
                Math.max(0, building.height - 2 * T),
                2,
                3
        );

        int doorX = x + (building.width - DOOR_WIDTH) / 2;

        drawBuildingTiledRect(
                g2,
                x,
                bottom - T,
                Math.max(0, doorX - x),
                T,
                0,
                3
        );

        drawBuildingTiledRect(
                g2,
                doorX + DOOR_WIDTH,
                bottom - T,
                Math.max(0, right - (doorX + DOOR_WIDTH)),
                T,
                0,
                3
        );
    }

    // =========================================================
    // INTERIOR DETAILS
    // Row 6 = cracks / stains / floor damage only.
    // =========================================================

    private void drawInteriorDetails(Graphics2D g2, Building building) {

        final int T = ART_TILE_SIZE;

        int x1 = building.x + 2 * T;
        int y1 = building.y + 2 * T;

        if (x1 < building.x + building.width - T
                && y1 < building.y + building.height - T) {
            drawBuildingTile(g2, 0, 6, x1, y1);
        }

        int x2 = building.x + building.width - 3 * T;
        int y2 = building.y + building.height - 3 * T;

        if (x2 >= building.x
                && y2 >= building.y) {
            drawBuildingTile(g2, 3, 6, x2, y2);
        }
    }

    // =========================================================
    // BUILDING COLLISION
    // =========================================================

    public boolean collidesWithBuilding(
            Rectangle playerBounds
    ) {

        for (Building building : buildings) {

            List<Rectangle> walls =
                    building.getCollisionWalls();

            for (Rectangle wall : walls) {

                if (!playerBounds.intersects(wall)) {
                    continue;
                }

                // A window is a real opening in the wall when it is open.
                // The building wall rectangle still exists behind it, so
                // skip that wall collision while Ethan is crossing an
                // opened window.
                boolean crossingOpenWindow = false;

                for (Window window : windows) {
                    if (window.isOpen()
                            && playerBounds.intersects(
                                    window.getCollisionBounds())) {
                        crossingOpenWindow = true;
                        break;
                    }
                }

                if (!crossingOpenWindow) {
                    return true;
                }
            }
        }

        for (Door door : doors) {

            if (!door.isOpen() &&
                playerBounds.intersects(door.getCollisionBounds())) {

                return true;
            }
        }

        for (Window window : windows) {

            if (!window.isOpen() &&
                playerBounds.intersects(window.getCollisionBounds())) {

                return true;
            }
        }

        return false;
    }

    // =========================================================
    // WINDOW INTERACTION
    // =========================================================

    public boolean interactWithWindow(Rectangle actorBounds) {

        Window nearest = null;
        double nearestDistance = Double.MAX_VALUE;

        int actorCenterX =
                actorBounds.x + actorBounds.width / 2;

        int actorCenterY =
                actorBounds.y + actorBounds.height / 2;

        for (Window window : windows) {

            if (!window.canInteract(actorBounds)) {
                continue;
            }

            int windowCenterX =
                    window.getX() + window.getWidth() / 2;

            int windowCenterY =
                    window.getY() + window.getHeight() / 2;

            int dx = actorCenterX - windowCenterX;
            int dy = actorCenterY - windowCenterY;

            double distance =
                    Math.sqrt(dx * dx + dy * dy);

            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearest = window;
            }
        }

        if (nearest == null) {
            return false;
        }

        nearest.toggle();
        return true;
    }

    // =========================================================
    // DOOR INTERACTION
    // =========================================================

    public boolean interactWithDoor(Rectangle actorBounds) {

        Door nearest = null;
        double nearestDistance = Double.MAX_VALUE;

        int actorCenterX =
                actorBounds.x + actorBounds.width / 2;

        int actorCenterY =
                actorBounds.y + actorBounds.height / 2;

        for (Door door : doors) {

            if (!door.canInteract(actorBounds)) {
                continue;
            }

            int doorCenterX =
                    door.getX() + door.getWidth() / 2;

            int doorCenterY =
                    door.getY() + door.getHeight() / 2;

            int dx = actorCenterX - doorCenterX;
            int dy = actorCenterY - doorCenterY;

            double distance =
                    Math.sqrt(dx * dx + dy * dy);

            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearest = door;
            }
        }

        if (nearest == null) {
            return false;
        }

        return nearest.toggle(actorBounds);
    }

    public Window getWindowAt(int x, int y) {

        for (Window window : windows) {

            if (window.getCollisionBounds().contains(x, y)) {
                return window;
            }
        }

        return null;
    }

    public Door getDoorAt(int x, int y) {

        for (Door door : doors) {

            if (door.getCollisionBounds().contains(x, y)) {
                return door;
            }
        }

        return null;
    }

    private Door getDoorForBuilding(Building building) {

        int index = buildings.indexOf(building);

        if (index < 0 || index >= doors.size()) {
            return null;
        }

        return doors.get(index);
    }

    // =========================================================
    // BUILDING INFORMATION
    // =========================================================

    public boolean isInsideBuilding(
            int playerX,
            int playerY
    ) {

        int centerX = playerX + 32;
        int centerY = playerY + 32;

        for (Building building : buildings) {

            if (building.containsPlayer(
                    centerX,
                    centerY
            )) {

                return true;
            }
        }

        return false;
    }

    // =========================================================
    // BUILDING INDEX
    // =========================================================

    /**
     * Returns the index of the building containing the supplied world point.
     * Returns -1 when the point is outside all buildings.
     */
    // =========================================================
    // INVESTIGATION INTERACTION
    // =========================================================

    public InvestigationPoint investigate(Rectangle actorBounds) {
        return investigation.investigateAndGet(actorBounds);
    }

    public Investigation getInvestigation() {
        return investigation;
    }

    public int getBuildingIndexAt(int worldX, int worldY) {

        for (int i = 0; i < buildings.size(); i++) {

            Building building = buildings.get(i);

            if (building.containsPlayer(worldX, worldY)) {
                return i;
            }
        }

        return -1;
    }

    // =========================================================
    // DOWNTOWN GATE
    // =========================================================

    private void drawGate(Graphics2D g2) {

        int gateX = 1750;
        int gateY = 1080;

        int gateWidth = 150;
        int gateHeight = 120;

        // Gate
        g2.setColor(
                new Color(110, 70, 40)
        );

        g2.fillRect(
                gateX,
                gateY,
                gateWidth,
                gateHeight
        );

        // Outline
        g2.setColor(Color.BLACK);

        g2.drawRect(
                gateX,
                gateY,
                gateWidth,
                gateHeight
        );

        // Label
        g2.setColor(Color.WHITE);

        g2.drawString(
                "DOWNTOWN",
                gateX + 30,
                gateY + 50
        );

        g2.drawString(
                "GATE",
                gateX + 55,
                gateY + 75
        );
    }

    // =========================================================
    // BUILDING CLASS
    // =========================================================

    private static class Building {

        int x;
        int y;
        int width;
        int height;
        String name;

        Building(
                int x,
                int y,
                int width,
                int height,
                String name
        ) {

            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.name = name;
        }

        // -----------------------------------------------------
        // CHECK IF PLAYER IS INSIDE
        // -----------------------------------------------------

        boolean containsPlayer(
                int centerX,
                int centerY
        ) {

            return
                    centerX > x + WALL_THICKNESS
                    &&
                    centerX < x + width - WALL_THICKNESS
                    &&
                    centerY > y + WALL_THICKNESS
                    &&
                    centerY < y + height - WALL_THICKNESS;
        }

        // -----------------------------------------------------
        // WALL COLLISION RECTANGLES
        // -----------------------------------------------------

        List<Rectangle> getCollisionWalls() {

            List<Rectangle> walls =
                    new ArrayList<>();

            // Top wall
            walls.add(
                    new Rectangle(
                            x,
                            y,
                            width,
                            WALL_THICKNESS
                    )
            );

            // Left wall
            walls.add(
                    new Rectangle(
                            x,
                            y,
                            WALL_THICKNESS,
                            height
                    )
            );

            // Right wall
            walls.add(
                    new Rectangle(
                            x + width - WALL_THICKNESS,
                            y,
                            WALL_THICKNESS,
                            height
                    )
            );

            // Door position
            int doorX =
                    x
                    + (width - DOOR_WIDTH) / 2;

            // Bottom wall LEFT
            walls.add(
                    new Rectangle(
                            x,
                            y + height - WALL_THICKNESS,
                            doorX - x,
                            WALL_THICKNESS
                    )
            );

            // Bottom wall RIGHT
            int rightStart =
                    doorX + DOOR_WIDTH;

            walls.add(
                    new Rectangle(
                            rightStart,
                            y + height - WALL_THICKNESS,
                            x + width - rightStart,
                            WALL_THICKNESS
                    )
            );

            return walls;
        }
    }
}