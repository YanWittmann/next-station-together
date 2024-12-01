package de.yanwittmann.nextstation.model.score;

import de.yanwittmann.nextstation.util.TextureAccess;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.awt.*;

@Data
@EqualsAndHashCode(callSuper = true)
public class ScoreInterchangeStations extends ScoreContributor {
    private int amountInterchanges;

    public ScoreInterchangeStations() {
        super("ScoreInterchangeStations");
        super.multiplierColor = new Color(40, 150, 0);
    }

    @Override
    protected TextureAccess.TextureData getBaseTexture() {
        return TextureAccess.TexturesIndex.SCORE_INTERCHANGE_STATIONS.get(String.valueOf(amountInterchanges));
    }
}
