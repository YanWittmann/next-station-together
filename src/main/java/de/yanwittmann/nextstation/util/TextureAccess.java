package de.yanwittmann.nextstation.util;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

@Slf4j
public class TextureAccess {

    public final static String TEXTURE_DIR = "/board/texture/";
    public final static String BOARD_DIR = "board/";

    @Data
    public static class TextureData {
        private final String path;
        private final BufferedImage image;

        public TextureData(String path, BufferedImage image) {
            this.path = path.replaceAll("^/+", "").replaceAll("/+$", "");
            this.image = image;
        }

        public TextureData overlay(TextureData overlay) {
            return new TextureData(path + "/" + overlay.path, overlayImages(image, overlay.image));
        }

        public TextureData tint(Color tintColor, float tintOpacity) {
            final BufferedImage tintedImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
            final Graphics2D graphics = tintedImage.createGraphics();
            graphics.drawImage(image, 0, 0, null);
            graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, tintOpacity));
            graphics.setColor(tintColor);
            graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
            graphics.dispose();
            return new TextureData(path + "/tinted", tintedImage);
        }

        public void write(File dir) throws IOException {
            final File output = new File(dir, fileHash() + ".png");
            output.getParentFile().mkdirs();
            ImageIO.write(image, "png", output);
        }

        public String fileHash() {
            return Integer.toHexString(path.hashCode());
        }

        public static void write(Collection<TextureData> textures, File dir) throws IOException {
            for (TextureData texture : textures) {
                texture.write(dir);
            }
        }

        public static void write(TextureData optTexture, File dir) throws IOException {
            if (optTexture != null) {
                optTexture.write(dir);
            }
        }

        public static void write(TextureProvider optProvider, File dir) throws IOException {
            if (optProvider != null) {
                write(optProvider.getTexture(), dir);
            }
        }
    }

    public enum TexturesIndex {
        UNKNOWN("none.png"),

        STATION_BASE_REGULAR(TEXTURE_DIR + BOARD_DIR + "station_base.png"),
        STATION_BASE_MONUMENT(TEXTURE_DIR + BOARD_DIR + "station_base_monument.png"),
        STATION_SHAPE_CIRCLE(TEXTURE_DIR + BOARD_DIR + "station_shape_circle.png"),
        STATION_SHAPE_TRIANGLE(TEXTURE_DIR + BOARD_DIR + "station_shape_triangle.png"),
        STATION_SHAPE_SQUARE(TEXTURE_DIR + BOARD_DIR + "station_shape_square.png"),
        STATION_SHAPE_PENTAGON(TEXTURE_DIR + BOARD_DIR + "station_shape_pentagon.png"),
        STATION_SHAPE_JOKER(TEXTURE_DIR + BOARD_DIR + "station_shape_joker.png"),
        ;

        private final String path;

        TexturesIndex(String path) {
            this.path = path;
        }

        public String getPath() {
            return path;
        }

        public TextureData get() {
            return new TextureData(path, getImageOrFallback(path));
        }

        public TextureData get(String filenameSuffix) {
            final String effectivePath = path.replaceAll("\\.png$", "") + filenameSuffix + ".png";
            return new TextureData(effectivePath, getImageOrFallback(effectivePath));
        }

        public TexturesIndex otherwise(boolean condition, TexturesIndex other) {
            return condition ? this : other;
        }
    }

    // image compositing and manipulation methods

    public static BufferedImage scaleImage(BufferedImage image, int width, int height) {
        final BufferedImage scaledImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D graphics = scaledImage.createGraphics();
        graphics.drawImage(image, 0, 0, width, height, null);
        graphics.dispose();
        return scaledImage;
    }

    public static BufferedImage overlayImages(BufferedImage background, BufferedImage overlay, int x, int y) {
        final Graphics2D graphics = background.createGraphics();
        graphics.drawImage(overlay, x, y, null);
        graphics.dispose();
        return background;
    }

    public static BufferedImage overlayImages(BufferedImage background, BufferedImage overlay) {
        final BufferedImage scaledOverlay = scaleImage(overlay, background.getWidth(), background.getHeight());
        return overlayImages(background, scaledOverlay, 0, 0);
    }

    // basic methods for loading images from resources

    public static InputStream getResource(String path) {
        return TextureAccess.class.getResourceAsStream(path);
    }

    public static BufferedImage getImage(String path) throws IOException {
        final InputStream inputStream = getResource(path);
        if (inputStream == null) throw new IOException("Resource not found: " + path);
        return ImageIO.read(inputStream);
    }

    public static BufferedImage getImageOrFallback(String path) {
        try {
            return getImage(path);
        } catch (IOException e) {
            log.error("Failed to load image for from: {}", path, e);
            try {
                return getImage("/board/icon/missing.png");
            } catch (IOException ex) {
                log.error("Failed to load fallback image", ex);
                return createDummyImage(256, 256);
            }
        }
    }

    private static BufferedImage createDummyImage(int width, int height) {
        final BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D graphics = image.createGraphics();
        graphics.setColor(new Color(255, 0, 255));
        graphics.fillRect(0, 0, width, height);
        graphics.setColor(Color.BLACK);
        graphics.setFont(new Font("Arial", Font.BOLD, 48));
        for (int i = 0; i < 20; i++) {
            graphics.drawString("?", (int) (Math.random() * width), (int) (Math.random() * height));
        }
        graphics.setColor(Color.WHITE);
        graphics.drawRect(0, 0, width - 1, height - 1);
        graphics.dispose();
        return image;
    }

    // helper methods

    public static void cleanDirectory(File dir) {
        if (dir.exists() && dir.isDirectory()) {
            for (File file : dir.listFiles()) {
                if (file.isDirectory()) {
                    cleanDirectory(file);
                }
                file.delete();
            }
        }
    }
}
