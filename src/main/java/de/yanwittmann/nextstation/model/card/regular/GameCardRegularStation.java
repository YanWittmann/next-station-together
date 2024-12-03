package de.yanwittmann.nextstation.model.card.regular;

import de.yanwittmann.nextstation.model.card.GameCard;
import de.yanwittmann.nextstation.util.TextureAccess;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(callSuper = true)
public class GameCardRegularStation extends GameCard {

    private CardSymbol cardType = CardSymbol.RECTANGLE;
    private String description = "Station";

    public GameCardRegularStation() {
        super("GameCardRegularStation");
    }

    @Override
    public TextureAccess.TextureData getTexture() {
        this.description = "Build a connection to a " + cardType.name().toLowerCase() + " station.";
        return TextureAccess.TexturesIndex.CARD_STATION_UNDERGROUND.get()
                .overlay(cardType.getTexture().cropToVisibleArea(), 335, 66, 109, 109);
    }

    @Getter
    @AllArgsConstructor
    public enum CardSymbol {
        RECTANGLE(TextureAccess.TexturesIndex.STATION_SHAPE_SQUARE.get()),
        CIRCLE(TextureAccess.TexturesIndex.STATION_SHAPE_CIRCLE.get()),
        TRIANGLE(TextureAccess.TexturesIndex.STATION_SHAPE_TRIANGLE.get()),
        PENTAGON(TextureAccess.TexturesIndex.STATION_SHAPE_PENTAGON.get()),
        JOKER(TextureAccess.TexturesIndex.CARD_SYMBOL_JOKER.get());

        private final TextureAccess.TextureData texture;

        public static List<CardSymbol> excludingJoker() {
            return Arrays.stream(CardSymbol.values()).filter(cardSymbol -> cardSymbol != JOKER).collect(Collectors.toList());
        }
    }
}
