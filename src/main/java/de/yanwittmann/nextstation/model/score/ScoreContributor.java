package de.yanwittmann.nextstation.model.score;

import de.yanwittmann.nextstation.util.TextureAccess;
import de.yanwittmann.nextstation.util.TextureProvider;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.awt.*;

@Data
@NoArgsConstructor(force = true)
public abstract class ScoreContributor implements TextureProvider {

    protected final String type;
    protected int multiplier = 1;
    protected Color multiplierColor = new Color(255, 16, 100);

    public ScoreContributor(String type) {
        this.type = type;
    }

    public TextureAccess.TextureData getTexture() {
        TextureAccess.TextureData texture = getBaseTexture();
        if (multiplier > 1) {
            int multiplierCircleDimensions = (int) (texture.getImage().getWidth() / 100f * 50);
            TextureAccess.TextureData multiplierCircle = TextureAccess.TexturesIndex.SCORE_CIRCLE.get()
                    .tintNonTransparent(multiplierColor, 0.8f)
                    .addText(String.valueOf(multiplier), new Font("Arial", Font.BOLD, 70), Color.WHITE, 0.5f, 0.42f, "center")
                    .scale(multiplierCircleDimensions, multiplierCircleDimensions);

            texture = texture
                    .pad(new Color(0, 0, 0, 0), 20, multiplierCircle.getImage().getWidth() / 2 + 1, 0, 0)
                    .overlay(multiplierCircle,
                            texture.getImage().getWidth() - multiplierCircle.getImage().getWidth() / 2, 0,
                            multiplierCircle.getImage().getWidth(), multiplierCircle.getImage().getHeight());
        }
        return texture;
    }

    protected TextureAccess.TextureData getBaseTexture() {
        return TextureAccess.TexturesIndex.STATION_BASE_REGULAR.get("none");
    }
}
