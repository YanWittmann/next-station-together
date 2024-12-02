package de.yanwittmann.nextstation.model.score.progress;

import de.yanwittmann.nextstation.model.score.ScoreContributor;
import de.yanwittmann.nextstation.util.TextureAccess;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class ProgressScoreCompoundContributor extends ProgressScoreContributor {
    private ScoreContributor scoreContributorA;
    private ScoreContributor scoreContributorB;
    private ScoreContributor scoreContributorC;

    public ProgressScoreCompoundContributor() {
        super("ProgressScoreCompoundContributor");
    }

    @Override
    public List<TextureAccess.TextureData> getTextures() {
        final List<TextureAccess.TextureData> textures = new ArrayList<>();
        if (scoreContributorA != null) textures.add(scoreContributorA.getTexture());
        if (scoreContributorB != null) textures.add(scoreContributorB.getTexture());
        if (scoreContributorC != null) textures.add(scoreContributorC.getTexture());
        return textures;
    }
}
