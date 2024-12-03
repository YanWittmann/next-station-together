package de.yanwittmann.nextstation.model.board;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Data
public class RiverLayout {
    // will be offset by 0.5 to match the center of the connections
    private final List<Point> path = new ArrayList<>();

    public void addPoint(Direction direction, float length) {
        final Point last = path.isEmpty() ? new Point(0, 0) : path.get(path.size() - 1);
        path.add(new Point(last.x + direction.getDx() * length, last.y + direction.getDy() * length));
    }

    public void addPoints(Point... points) {
        for (Point point : points) {
            path.add(new Point(point.x + 0.5f, point.y + 0.5f));
        }
    }

    public float pathLength() {
        float length = 0;
        for (int i = 1; i < path.size(); i++) {
            length += distance(path.get(i - 1), path.get(i));
        }
        return length;
    }

    public static float distance(Point a, Point b) {
        return (float) Math.sqrt(Math.pow(a.x - b.x, 2) + Math.pow(a.y - b.y, 2));
    }

    @Data
    @AllArgsConstructor
    public static class Point {
        private float x;
        private float y;
    }

    @Getter
    @AllArgsConstructor
    public enum Direction {
        UP(0, -1),
        RIGHT(1, 0),
        DOWN(0, 1),
        LEFT(-1, 0),
        UP_RIGHT(1, -1),
        RIGHT_DOWN(1, 1),
        DOWN_LEFT(-1, 1),
        LEFT_UP(-1, -1);

        private final int dx, dy;

        public boolean isDiagonal() {
            return dx != 0 && dy != 0;
        }

        public Direction divergeChance(float chance) {
            if (Math.random() < chance) {
                switch (this) {
                    case UP:
                        return randomElement(UP_RIGHT, LEFT_UP);
                    case RIGHT:
                        return randomElement(UP_RIGHT, RIGHT_DOWN);
                    case DOWN:
                        return randomElement(RIGHT_DOWN, DOWN_LEFT);
                    case LEFT:
                        return randomElement(DOWN_LEFT, LEFT_UP);
                    case UP_RIGHT:
                        return randomElement(UP, RIGHT);
                    case RIGHT_DOWN:
                        return randomElement(RIGHT, DOWN);
                    case DOWN_LEFT:
                        return randomElement(DOWN, LEFT);
                    case LEFT_UP:
                        return randomElement(LEFT, UP);
                }
            }
            return this;
        }

        private static <T> T randomElement(T... array) {
            return array[(int) (Math.random() * array.length)];
        }
    }
}
