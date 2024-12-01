package de.yanwittmann.nextstation.model.score;

import de.yanwittmann.nextstation.util.TextureAccess;
import de.yanwittmann.nextstation.util.TextureProvider;
import lombok.Data;

@Data
public abstract class ScoreContributor implements TextureProvider {

    protected final String type;
    protected int multiplier = 1;

    public ScoreContributor(String type) {
        this.type = type;
    }

    public TextureAccess.TextureData getTexture() {
        return TextureAccess.TexturesIndex.STATION_BASE_REGULAR.get();
    }
}
