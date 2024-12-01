package de.yanwittmann.nextstation.util;

import de.yanwittmann.nextstation.model.board.RailwayConnection;
import de.yanwittmann.nextstation.model.board.RailwayConnectionIntersection;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class TmpIntersection {
    private int x, y;
    private Set<RailwayConnectionIntersection.Direction> directions = new HashSet<>();
    private Set<RailwayConnection> connections = new HashSet<>();

    public TmpIntersection(int x, int y) {
        this.x = x;
        this.y = y;
    }
}
