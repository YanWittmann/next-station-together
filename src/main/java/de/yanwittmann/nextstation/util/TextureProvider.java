package de.yanwittmann.nextstation.util;

public interface TextureProvider {
    TextureAccess.TextureData getTexture();

    default String getTextureHash() {
        return getTexture().fileHash();
    }
}
