package de.yanwittmann.nextstation.model.board;

import de.yanwittmann.nextstation.util.TextureAccess;
import de.yanwittmann.nextstation.util.TextureProvider;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Data
public class RailwayConnectionIntersection implements TextureProvider {
    private int x, y;
    private Direction bottom;
    private Direction top;

    public RailwayConnectionIntersection(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public TextureAccess.TextureData getTexture() {
        return TextureAccess.TexturesIndex.CONNECTION_INTERSECTION.get(bottom.getTextureName())
                .overlay(TextureAccess.TexturesIndex.CONNECTION_INTERSECTION.get(top.getTextureName()));
    }

public boolean intersects(RailwayConnection connection) {
    int x1 = connection.getX1();
    int y1 = connection.getY1();
    int x2 = connection.getX2();
    int y2 = connection.getY2();

    // Check if the intersection point (x, y) lies on the line segment from (x1, y1) to (x2, y2)
    double crossProduct = (y - y1) * (x2 - x1) - (x - x1) * (y2 - y1);
    if (Math.abs(crossProduct) > 1e-6) {
        return false;
    }

    double dotProduct = (x - x1) * (x2 - x1) + (y - y1) * (y2 - y1);
    if (dotProduct < 0) {
        return false;
    }

    double squaredLength = (x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1);
    if (dotProduct > squaredLength) {
        return false;
    }

    return true;
}

    public boolean containsDirection(Direction direction) {
        return bottom == direction || top == direction;
    }

    @Getter
    @AllArgsConstructor
    public enum Direction {
        LEFT_TO_RIGHT("lr"),
        UP_TO_DOWN("tb"),
        BOTTOM_LEFT_TO_TOP_RIGHT("bltr"),
        TOP_LEFT_TO_BOTTOM_RIGHT("tlbr");

        private final String textureName;
    }
}
