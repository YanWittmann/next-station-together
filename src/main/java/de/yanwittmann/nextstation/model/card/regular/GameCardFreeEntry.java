package de.yanwittmann.nextstation.model.card.regular;

import de.yanwittmann.nextstation.model.card.GameCard;
import de.yanwittmann.nextstation.util.TextureAccess;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class GameCardFreeEntry extends GameCard {
    private String description = "Either to be used like a joker, or draw a line to a monument and an additional line from there to any symbol station.";

    public GameCardFreeEntry() {
        super("GameCardFreeEntry");
    }

    @Override
    public TextureAccess.TextureData getTexture() {
        return TextureAccess.TexturesIndex.CARD_FREE_ENTRY.get();
    }
}
