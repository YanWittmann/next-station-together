package de.yanwittmann.nextstation.model.score;

import de.yanwittmann.nextstation.util.TextureAccess;

public class ScoreDistrictsVisited extends ScoreContributor {
    public ScoreDistrictsVisited() {
        super("ScoreDistrictsVisited");
    }

    @Override
    protected TextureAccess.TextureData getBaseTexture() {
        return TextureAccess.TexturesIndex.SCORE_DISTRICTS_COUNT.get();
    }
}
