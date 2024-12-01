package de.yanwittmann.nextstation.model.card;

import de.yanwittmann.nextstation.util.TextureAccess;
import de.yanwittmann.nextstation.util.TextureProvider;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class StationCard implements TextureProvider  {
    private final List<StationCardComponent> components = new ArrayList<>();


    @Override
    public TextureAccess.TextureData getTexture() {
        return TextureAccess.TexturesIndex.UNKNOWN.get();
    }
}
