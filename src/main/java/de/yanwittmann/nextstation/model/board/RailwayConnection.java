package de.yanwittmann.nextstation.model.board;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class RailwayConnection {
    private int x1, y1, x2, y2;

    public RailwayConnection(Station a, Station b) {
        this(a.getX(), a.getY(), b.getX(), b.getY());
    }

    public Map.Entry<Integer, Integer> intersection(RailwayConnection other) {
        int x3 = other.getX1();
        int y3 = other.getY1();
        int x4 = other.getX2();
        int y4 = other.getY2();

        int x1 = this.getX1();
        int y1 = this.getY1();
        int x2 = this.getX2();
        int y2 = this.getY2();

        int denominator = ((x1 - x2) * (y3 - y4)) - ((y1 - y2) * (x3 - x4));
        if (denominator == 0) return null;

        int x = (((x1 * y2) - (y1 * x2)) * (x3 - x4) - (x1 - x2) * ((x3 * y4) - (y3 * x4))) / denominator;
        int y = (((x1 * y2) - (y1 * x2)) * (y3 - y4) - (y1 - y2) * ((x3 * y4) - (y3 * x4))) / denominator;

        if (x < Math.min(x1, x2) || x > Math.max(x1, x2) || x < Math.min(x3, x4) || x > Math.max(x3, x4)) return null;
        if (y < Math.min(y1, y2) || y > Math.max(y1, y2) || y < Math.min(y3, y4) || y > Math.max(y3, y4)) return null;

        return Map.entry(x, y);
    }

    public RailwayConnectionIntersection.Direction getDirection() {
        if (x1 == x2) {
            return RailwayConnectionIntersection.Direction.UP_TO_DOWN;
        } else if (y1 == y2) {
            return RailwayConnectionIntersection.Direction.LEFT_TO_RIGHT;
        } else if ((x1 < x2 && y1 < y2) || (x2 < x1 && y2 < y1)) {
            return RailwayConnectionIntersection.Direction.TOP_LEFT_TO_BOTTOM_RIGHT;
        } else {
            return RailwayConnectionIntersection.Direction.BOTTOM_LEFT_TO_TOP_RIGHT;
        }
    }
}
