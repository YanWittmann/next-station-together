package de.yanwittmann.nextstation.model.board;

import lombok.Data;

@Data
public class RailwayConnectionIntersection {
    private int x, y;
    private RailwayConnection connection1, connection2;
}
