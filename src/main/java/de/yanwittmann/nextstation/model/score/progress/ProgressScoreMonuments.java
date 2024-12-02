package de.yanwittmann.nextstation.model.score.progress;

import de.yanwittmann.nextstation.util.TextureAccess;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class ProgressScoreMonuments extends ProgressScoreContributor {
    private List<String> fields = new ArrayList<>(List.of("0", "1", "2", "4", "6", "8", "11", "14", "17", "21", "25"));
    private List<String> textures = new ArrayList<>();

    public ProgressScoreMonuments() {
        super("ProgressScoreMonuments");
    }

    @Override
    public List<TextureAccess.TextureData> getTextures() {
        final List<TextureAccess.TextureData> textures = new ArrayList<>();
        this.textures = new ArrayList<>();
        for (String text : fields) {
            final TextureAccess.TextureData baseTexture = TextureAccess.TexturesIndex.STATION_BASE_MONUMENT.get();
            final TextureAccess.TextureData compound = baseTexture.addText(text, new Font("Arial", Font.BOLD, 100), new Color(33, 0, 89), 0.5f, 0.46f, "center");
            textures.add(compound);
            this.textures.add(compound.fileHash());
        }
        return textures;
    }
}
