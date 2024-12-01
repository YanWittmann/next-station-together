package de.yanwittmann.nextstation.model.card;

import de.yanwittmann.nextstation.util.TextureAccess;
import de.yanwittmann.nextstation.util.TextureProvider;
import lombok.Data;

@Data
public class StationCardComponent implements TextureProvider {

    protected String name = "none";

    public TextureAccess.TextureData getTexture() {
        return TextureAccess.TexturesIndex.UNKNOWN.get();
    }
}
