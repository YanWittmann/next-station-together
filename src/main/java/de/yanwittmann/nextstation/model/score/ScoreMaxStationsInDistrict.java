package de.yanwittmann.nextstation.model.score;

import de.yanwittmann.nextstation.util.TextureAccess;

public class ScoreMaxStationsInDistrict extends ScoreContributor {
    public ScoreMaxStationsInDistrict() {
        super("ScoreMaxStationsInDistrict");
    }

    @Override
    protected TextureAccess.TextureData getBaseTexture() {
        return TextureAccess.TexturesIndex.SCORE_MAX_STATIONS_IN_DISTRICT.get();
    }
}
