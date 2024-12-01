package de.yanwittmann.nextstation.model.board;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BoardDistrict {
    private int x, y;
    private int width, height;

    public boolean containsGeometrically(Station station) {
        return station.getX() >= x && station.getX() < x + width && station.getY() >= y && station.getY() < y + height;
    }

    public int area() {
        return width * height;
    }

    public boolean containsGeometrically(int x, int y) {
        return x >= this.x && x < this.x + width && y >= this.y && y < this.y + height;
    }
}
