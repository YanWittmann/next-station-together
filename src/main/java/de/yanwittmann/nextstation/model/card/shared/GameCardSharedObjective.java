package de.yanwittmann.nextstation.model.card.shared;

import de.yanwittmann.nextstation.model.card.GameCard;
import de.yanwittmann.nextstation.util.TextureAccess;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Data
@EqualsAndHashCode(callSuper = true)
public class GameCardSharedObjective extends GameCard {

    private CardSymbol cardType = CardSymbol.FIVE_MONUMENTS;
    private String description = "No description available.";

    public GameCardSharedObjective() {
        super("GameCardSharedObjective");
    }

    @Override
    public TextureAccess.TextureData getTexture() {
        this.description = cardType.getDescription();
        return cardType.getTexture();
    }

    @Getter
    @AllArgsConstructor
    public enum CardSymbol {
        // london
        FIVE_MONUMENTS(TextureAccess.TexturesIndex.CARD_SHARED_OBJECTIVE.get("5_monuments"), "Draw your Underground network such that it connects to all 5 tourist sites in the city."),
        SIX_RIVER_CROSSINGS(TextureAccess.TexturesIndex.CARD_SHARED_OBJECTIVE.get("6_river_crossings"), "Draw your Underground network such that it crosses the river at least 6 times."),
        EIGHT_DOUBLE_INTERCHANGES(TextureAccess.TexturesIndex.CARD_SHARED_OBJECTIVE.get("8_interchanges"), "Draw your Underground network such that it has at least 8 Double Interchanges."),
        ALL_DISTRICTS(TextureAccess.TexturesIndex.CARD_SHARED_OBJECTIVE.get("all_districts"), "Draw your Underground network such that it connects to at least one station in all districts of the city."),
        ALL_STATIONS_IN_CENTRAL(TextureAccess.TexturesIndex.CARD_SHARED_OBJECTIVE.get("all_stations_in_central"), "Draw your Underground network such that it connects to all 9 stations in the central district of the city."),
        // paris
        THREE_TRIPLE_INTERSECTIONS(TextureAccess.TexturesIndex.CARD_SHARED_OBJECTIVE.get("3_triple_intersections"), "Draw your Underground network such that it has at least 3 Triple Interchanges."),
        SEVEN_DISTRICTS_IN_ONE_LINE(TextureAccess.TexturesIndex.CARD_SHARED_OBJECTIVE.get("7_districts_in_one_line"), "Draw your Underground network such that it connects to at least one station in 7 different districts of the city."),
        ANY_JOINED_LINE_TWO_DOUBLE_INTERSECTIONS(TextureAccess.TexturesIndex.CARD_SHARED_OBJECTIVE.get("any_joined_line_2_double_intersections"), "Create a metro network (counting all of your lines) that contains at least 2 different overhead crossings that have been used twice."),
        ANY_JOINED_LINE_SIX_MONUMENTS(TextureAccess.TexturesIndex.CARD_SHARED_OBJECTIVE.get("any_joined_line_6_monuments"), "Create a metro network (counting all of your lines) that includes at least 6 different tourist sites."),;

        private final TextureAccess.TextureData texture;
        private final String description;
    }
}
