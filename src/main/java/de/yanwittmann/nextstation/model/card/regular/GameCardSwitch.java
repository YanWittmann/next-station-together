package de.yanwittmann.nextstation.model.card.regular;

import de.yanwittmann.nextstation.model.card.GameCard;
import de.yanwittmann.nextstation.util.TextureAccess;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class GameCardSwitch extends GameCard {
    private String description = "Pull another card. You may build the connection starting from anywhere on your line.";

    public GameCardSwitch() {
        super("GameCardSwitch");
    }

    @Override
    public TextureAccess.TextureData getTexture() {
        return TextureAccess.TexturesIndex.CARD_SWITCH.get();
    }
}
