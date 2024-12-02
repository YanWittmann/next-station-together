package de.yanwittmann.nextstation.model.score;

import de.yanwittmann.nextstation.util.TextureAccess;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ScoreIntersections extends ScoreContributor {
    private int usedPaths = 1;
    public ScoreIntersections() {
        super("ScoreIntersections");
    }

    @Override
    protected TextureAccess.TextureData getBaseTexture() {
        final TextureAccess.TextureData bottomTexture = usedPaths == 2 ? TextureAccess.TexturesIndex.CONNECTION_WALKED_INTERSECTION.get("tlbr") : TextureAccess.TexturesIndex.CONNECTION_INTERSECTION.get("tlbr");
        final TextureAccess.TextureData topTexture = TextureAccess.TexturesIndex.CONNECTION_WALKED_INTERSECTION.get("bltr");
        return bottomTexture.overlay(topTexture);
    }
}
