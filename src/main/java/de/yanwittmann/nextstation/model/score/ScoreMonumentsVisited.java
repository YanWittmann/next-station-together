package de.yanwittmann.nextstation.model.score;

import de.yanwittmann.nextstation.util.TextureAccess;

public class ScoreMonumentsVisited extends ScoreContributor {
    public ScoreMonumentsVisited() {
        super("ScoreMonumentsVisited");
    }

    @Override
    protected TextureAccess.TextureData getBaseTexture() {
        return TextureAccess.TexturesIndex.STATION_BASE_MONUMENT.get().cropToVisibleArea()
                .overlay(TextureAccess.TexturesIndex.STATION_SHAPE_MONUMENT.get());
    }
}
