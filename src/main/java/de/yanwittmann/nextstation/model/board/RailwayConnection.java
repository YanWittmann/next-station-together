package de.yanwittmann.nextstation.model.board;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RailwayConnection {
    private int x1, y1, x2, y2;

    public RailwayConnection(Station a, Station b) {
        this(a.getX(), a.getY(), b.getX(), b.getY());
    }
}
