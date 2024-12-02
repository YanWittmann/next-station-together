package de.yanwittmann.nextstation.model.score.progress;

import de.yanwittmann.nextstation.util.TextureAccess;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor(force = true)
public abstract class ProgressScoreContributor {
    protected final String type;

    public ProgressScoreContributor(String type) {
        this.type = type;
    }

    public abstract List<TextureAccess.TextureData> getTextures();
}
