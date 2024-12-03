package de.yanwittmann.nextstation.model.card;

import de.yanwittmann.nextstation.util.TextureProvider;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
public abstract class GameCard implements TextureProvider  {
    private final String type;
}
