package Main;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

public class Investigation {

    private final List<InvestigationPoint> points =
            new ArrayList<>();

    public void addPoint(InvestigationPoint point) {
        if (point == null) {
            return;
        }

        points.add(point);
    }

    public boolean investigate(Rectangle actorBounds) {
        InvestigationPoint nearest = getNearestInteractable(actorBounds);

        if (nearest == null) {
            return false;
        }

        return nearest.investigate();
    }

    public InvestigationPoint getNearestInteractable(Rectangle actorBounds) {
        if (actorBounds == null) {
            return null;
        }

        InvestigationPoint nearest = null;
        double nearestDistance = Double.MAX_VALUE;

        int actorCenterX =
                actorBounds.x + actorBounds.width / 2;
        int actorCenterY =
                actorBounds.y + actorBounds.height / 2;

        for (InvestigationPoint point : points) {
            if (!point.canInteract(actorBounds)) {
                continue;
            }

            int pointCenterX =
                    point.getX() + point.getWidth() / 2;
            int pointCenterY =
                    point.getY() + point.getHeight() / 2;

            int dx = actorCenterX - pointCenterX;
            int dy = actorCenterY - pointCenterY;

            double distance = Math.sqrt(dx * dx + dy * dy);

            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearest = point;
            }
        }

        return nearest;
    }

    public InvestigationPoint investigateAndGet(Rectangle actorBounds) {
        InvestigationPoint point = getNearestInteractable(actorBounds);

        if (point == null) {
            return null;
        }

        if (!point.investigate()) {
            return null;
        }

        return point;
    }

    public void draw(Graphics2D g2) {
        if (g2 == null) {
            return;
        }

        for (InvestigationPoint point : points) {
            point.draw(g2);
        }
    }

    public List<InvestigationPoint> getPoints() {
        return points;
    }

    public void clear() {
        points.clear();
    }
}
