package de.yanwittmann.nextstation.model.score;

import de.yanwittmann.nextstation.util.TextureAccess;

public class ScoreRiverCrossings extends ScoreContributor {
    public ScoreRiverCrossings() {
        super("ScoreRiverCrossings");
    }

    @Override
    protected TextureAccess.TextureData getBaseTexture() {
        return TextureAccess.TexturesIndex.SCORE_RIVER_CROSSINGS.get();
    }
}
