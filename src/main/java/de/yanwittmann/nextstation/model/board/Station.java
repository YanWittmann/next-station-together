package de.yanwittmann.nextstation.model.board;

import de.yanwittmann.nextstation.util.TextureAccess;
import de.yanwittmann.nextstation.util.TextureProvider;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.awt.*;

@Data
@AllArgsConstructor
public class Station implements TextureProvider {
    private int x, y;
    private StationType type;
    private boolean isMonument;
    private int startingPosition;

    public TextureAccess.TextureData getTexture() {
        final TextureAccess.TextureData base = TextureAccess.TexturesIndex.STATION_BASE_MONUMENT.otherwise(isMonument, TextureAccess.TexturesIndex.STATION_BASE_REGULAR).get()
                .overlay(type.texture);
        if (startingPosition != -1) {
            if (startingPosition == 0) {
                return base.tint(Color.RED, 0.5f);
            } else if (startingPosition == 1) {
                return base.tint(Color.BLUE, 0.5f);
            } else if (startingPosition == 2) {
                return base.tint(Color.GREEN, 0.5f);
            } else if (startingPosition == 3) {
                return base.tint(Color.YELLOW, 0.5f);
            }
        }
        return base;
    }

    public static Station randomType(int x, int y) {
        return new Station(x, y, StationType.random(), false, -1);
    }

    public enum StationType {
        RECTANGLE(TextureAccess.TexturesIndex.STATION_SHAPE_SQUARE.get()),
        CIRCLE(TextureAccess.TexturesIndex.STATION_SHAPE_CIRCLE.get()),
        TRIANGLE(TextureAccess.TexturesIndex.STATION_SHAPE_TRIANGLE.get()),
        PENTAGON(TextureAccess.TexturesIndex.STATION_SHAPE_PENTAGON.get()),
        // exclude from random
        JOKER(TextureAccess.TexturesIndex.STATION_SHAPE_JOKER.get());

        private final TextureAccess.TextureData texture;

        StationType(TextureAccess.TextureData texture) {
            this.texture = texture;
        }

        public static StationType random() {
            return StationType.values()[(int) (Math.random() * StationType.values().length - 1)];
        }
    }
}
