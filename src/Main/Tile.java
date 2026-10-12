package Main;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;

/**
 * Reusable 32x32 Raven City map-art tiles.
 *
 * Existing map tileset:
 *   Row 0 = GRASS / GROUND
 *   Row 1 = DIRT
 *   Row 2 = ASPHALT ROAD
 *   Row 3 = ROAD EDGES
 *   Row 4 = SIDEWALK
 *   Row 5 = ROAD MARKINGS
 *
 * Building exterior tileset:
 *   Row 0 = BASIC EXTERIOR WALL
 *   Row 1 = WALL EDGES
 *   Row 2 = WALL CORNERS
 *   Row 3 = DOOR WALL SECTIONS
 *   Row 4 = WINDOW WALL SECTIONS
 *   Row 5 = BUILDING FOUNDATION / BASE
 *   Row 6 = DAMAGED / ABANDONED VARIATIONS
 *
 * Both tilesets use 32x32 source cells.
 */
public class Tile {

    public static final int TILE_SIZE = 32;

    // =========================================================
    // EXISTING MAP TILESET ROWS
    // =========================================================

    public static final int GRASS_ROW = 0;
    public static final int DIRT_ROW = 1;
    public static final int ASPHALT_ROW = 2;
    public static final int ROAD_EDGE_ROW = 3;
    public static final int SIDEWALK_ROW = 4;
    public static final int MARKING_ROW = 5;

    // =========================================================
    // BUILDING EXTERIOR TILESET ROWS
    // =========================================================

    public static final int BUILDING_WALL_ROW = 0;
    public static final int BUILDING_EDGE_ROW = 1;
    public static final int BUILDING_CORNER_ROW = 2;
    public static final int BUILDING_DOOR_ROW = 3;
    public static final int BUILDING_WINDOW_ROW = 4;
    public static final int BUILDING_FOUNDATION_ROW = 5;
    public static final int BUILDING_DAMAGE_ROW = 6;

    // =========================================================
    // TILESET IMAGES
    // =========================================================

    private final BufferedImage sheet;
    private final BufferedImage buildingSheet;

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public Tile() {

        sheet = loadImage(
                "/assets/map_tileset.png"
        );

        buildingSheet = loadImage(
                "/assets/building_exterior_sheet.png"
        );
    }

    // =========================================================
    // IMAGE LOADING
    // =========================================================

    private BufferedImage loadImage(String path) {

        try (
                InputStream input =
                        Tile.class.getResourceAsStream(path)
        ) {

            if (input == null) {

                System.err.println(
                        "Raven City: tileset not found: "
                        + path
                );

                return null;
            }

            BufferedImage image =
                    ImageIO.read(input);

            if (image == null) {

                System.err.println(
                        "Raven City: unable to read tileset: "
                        + path
                );
            }

            return image;

        } catch (IOException e) {

            System.err.println(
                    "Raven City: failed to load tileset: "
                    + path
            );

            return null;
        }
    }

    // =========================================================
    // EXISTING MAP TILESET STATUS
    // =========================================================

    public boolean isLoaded() {
        return sheet != null;
    }

    // =========================================================
    // BUILDING TILESET STATUS
    // =========================================================

    public boolean isBuildingLoaded() {
        return buildingSheet != null;
    }

    // =========================================================
    // EXISTING MAP TILE DRAW
    // =========================================================

    public void draw(
            Graphics2D g2,
            int column,
            int row,
            int x,
            int y
    ) {

        if (g2 == null || sheet == null) {
            return;
        }

        if (!validCell(
                sheet,
                column,
                row
        )) {
            return;
        }

        int sx =
                column * TILE_SIZE;

        int sy =
                row * TILE_SIZE;

        g2.drawImage(
                sheet,
                x,
                y,
                x + TILE_SIZE,
                y + TILE_SIZE,
                sx,
                sy,
                sx + TILE_SIZE,
                sy + TILE_SIZE,
                null
        );
    }

    // =========================================================
    // EXISTING MAP TILE ROTATION
    // =========================================================

    public void drawRotated(
            Graphics2D g2,
            int column,
            int row,
            int x,
            int y,
            double degrees
    ) {

        if (g2 == null || sheet == null) {
            return;
        }

        if (!validCell(
                sheet,
                column,
                row
        )) {
            return;
        }

        drawRotatedFromSheet(
                g2,
                sheet,
                column,
                row,
                x,
                y,
                degrees
        );
    }

    // =========================================================
    // BUILDING TILE DRAW
    // =========================================================

    public void drawBuilding(
            Graphics2D g2,
            int column,
            int row,
            int x,
            int y
    ) {

        if (g2 == null || buildingSheet == null) {
            return;
        }

        if (!validCell(
                buildingSheet,
                column,
                row
        )) {
            return;
        }

        int sx =
                column * TILE_SIZE;

        int sy =
                row * TILE_SIZE;

        g2.drawImage(
                buildingSheet,
                x,
                y,
                x + TILE_SIZE,
                y + TILE_SIZE,
                sx,
                sy,
                sx + TILE_SIZE,
                sy + TILE_SIZE,
                null
        );
    }

    // =========================================================
    // BUILDING TILE ROTATION
    // =========================================================

    public void drawBuildingRotated(
            Graphics2D g2,
            int column,
            int row,
            int x,
            int y,
            double degrees
    ) {

        if (
                g2 == null
                || buildingSheet == null
        ) {
            return;
        }

        if (!validCell(
                buildingSheet,
                column,
                row
        )) {
            return;
        }

        drawRotatedFromSheet(
                g2,
                buildingSheet,
                column,
                row,
                x,
                y,
                degrees
        );
    }

    // =========================================================
    // ROTATED TILE HELPER
    // =========================================================

    private void drawRotatedFromSheet(
            Graphics2D g2,
            BufferedImage source,
            int column,
            int row,
            int x,
            int y,
            double degrees
    ) {

        int sx =
                column * TILE_SIZE;

        int sy =
                row * TILE_SIZE;

        AffineTransform oldTransform =
                g2.getTransform();

        AffineTransform transform =
                new AffineTransform(
                        oldTransform
                );

        transform.translate(
                x + TILE_SIZE / 2.0,
                y + TILE_SIZE / 2.0
        );

        transform.rotate(
                Math.toRadians(degrees)
        );

        transform.translate(
                -TILE_SIZE / 2.0,
                -TILE_SIZE / 2.0
        );

        g2.setTransform(transform);

        g2.drawImage(
                source,
                0,
                0,
                TILE_SIZE,
                TILE_SIZE,
                sx,
                sy,
                sx + TILE_SIZE,
                sy + TILE_SIZE,
                null
        );

        g2.setTransform(
                oldTransform
        );
    }

    // =========================================================
    // VALID CELL CHECK
    // =========================================================

    private boolean validCell(
            BufferedImage source,
            int column,
            int row
    ) {

        if (
                source == null
                || column < 0
                || row < 0
        ) {
            return false;
        }

        int sx =
                column * TILE_SIZE;

        int sy =
                row * TILE_SIZE;

        return
                sx + TILE_SIZE <= source.getWidth()
                &&
                sy + TILE_SIZE <= source.getHeight();
    }

    // =========================================================
    // EXISTING MAP TILESET DIMENSIONS
    // =========================================================

    public int getColumns() {

        return sheet == null
                ? 0
                : sheet.getWidth() / TILE_SIZE;
    }

    public int getRows() {

        return sheet == null
                ? 0
                : sheet.getHeight() / TILE_SIZE;
    }

    // =========================================================
    // BUILDING TILESET DIMENSIONS
    // =========================================================

    public int getBuildingColumns() {

        return buildingSheet == null
                ? 0
                : buildingSheet.getWidth() / TILE_SIZE;
    }

    public int getBuildingRows() {

        return buildingSheet == null
                ? 0
                : buildingSheet.getHeight() / TILE_SIZE;
    }
}