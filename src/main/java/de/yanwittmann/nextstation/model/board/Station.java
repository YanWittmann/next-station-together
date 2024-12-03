package de.yanwittmann.nextstation.model.board;

import de.yanwittmann.nextstation.util.TextureAccess;
import de.yanwittmann.nextstation.util.TextureProvider;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.awt.*;

@Data
@AllArgsConstructor
public class Station implements TextureProvider {
    private int x, y;
    private StationType type;
    private boolean isMonument;
    private int startingPosition;

    public TextureAccess.TextureData getTexture() {
        TextureAccess.TextureData base = TextureAccess.TexturesIndex.STATION_BASE_MONUMENT.otherwise(isMonument, TextureAccess.TexturesIndex.STATION_BASE_REGULAR).get();
        TextureAccess.TextureData symbol = type.texture;

        if (startingPosition != -1) {
            symbol = symbol.tintNonTransparent(Color.WHITE, 1.0f);
            base = base.tintOnlyColorWithThreshold(Color.WHITE, 1.0f, Color.BLUE, 200);
            if (startingPosition == 0) {
                base = base.tintOnlyColorWithThreshold(new Color(255, 145, 35), 0.8f, Color.WHITE, 50);
            } else if (startingPosition == 1) {
                base = base.tintOnlyColorWithThreshold(new Color(35, 228, 255), 0.8f, Color.WHITE, 50);
            } else if (startingPosition == 2) {
                base = base.tintOnlyColorWithThreshold(new Color(83, 225, 0), 0.8f, Color.WHITE, 50);
            } else if (startingPosition == 3) {
                base = base.tintOnlyColorWithThreshold(new Color(221, 107, 255), 0.8f, Color.WHITE, 50);
            }
        }

        return base.overlay(symbol);
    }

    public static Station randomType(int x, int y) {
        return new Station(x, y, StationType.random(), false, -1);
    }

    public static Station ofType(int x, int y, StationType type) {
        return new Station(x, y, type, false, -1);
    }

    @Getter
    @AllArgsConstructor
    public enum StationType {
        RECTANGLE(TextureAccess.TexturesIndex.STATION_SHAPE_SQUARE.get()),
        CIRCLE(TextureAccess.TexturesIndex.STATION_SHAPE_CIRCLE.get()),
        TRIANGLE(TextureAccess.TexturesIndex.STATION_SHAPE_TRIANGLE.get()),
        PENTAGON(TextureAccess.TexturesIndex.STATION_SHAPE_PENTAGON.get()),
        // exclude from random
        JOKER(TextureAccess.TexturesIndex.STATION_SHAPE_JOKER.get());

        private final TextureAccess.TextureData texture;

        public static StationType random() {
            return StationType.values()[(int) (Math.random() * StationType.values().length - 1)];
        }
    }
}
