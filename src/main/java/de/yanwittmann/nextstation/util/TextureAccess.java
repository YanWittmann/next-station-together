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
    public final static String SCORE_DIR = "score/";

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

        public TextureData overlay(TextureData overlay, int x, int y, int width, int height) {
            final BufferedImage scaledOverlay = scaleImage(overlay.image, width, height);
            return new TextureData(path + "/" + overlay.path, overlayImages(image, scaledOverlay, x, y));
        }

        public TextureData tintNonTransparent(Color tintColor, float tintOpacity) {
            final BufferedImage tintedImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
            final Graphics2D graphics = tintedImage.createGraphics();
            graphics.drawImage(image, 0, 0, null);
            for (int x = 0; x < image.getWidth(); x++) {
                for (int y = 0; y < image.getHeight(); y++) {
                    final int color = image.getRGB(x, y);
                    if ((color & 0xFF000000) != 0) {
                        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, tintOpacity));
                        graphics.setColor(tintColor);
                        graphics.fillRect(x, y, 1, 1);
                    }
                }
            }
            graphics.dispose();
            return new TextureData(path + "/tinted-" +
                    tintColor.getRed() + "-" + tintColor.getGreen() + "-" + tintColor.getBlue() + "-" + tintOpacity,
                    tintedImage);
        }

        public TextureData tintOnlyColorWithThreshold(Color tintColor, float tintOpacity, Color refColor, int threshold) {
            final BufferedImage tintedImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
            final Graphics2D graphics = tintedImage.createGraphics();
            graphics.drawImage(image, 0, 0, null);
            graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, tintOpacity));
            graphics.setColor(tintColor);

            for (int x = 0; x < image.getWidth(); x++) {
                for (int y = 0; y < image.getHeight(); y++) {
                    final int color = image.getRGB(x, y);
                    final int refRed = refColor.getRed();
                    final int refGreen = refColor.getGreen();
                    final int refBlue = refColor.getBlue();
                    final int red = (color >> 16) & 0xFF;
                    final int green = (color >> 8) & 0xFF;
                    final int blue = color & 0xFF;

                    if (Math.abs(red - refRed) <= threshold && Math.abs(green - refGreen) <= threshold && Math.abs(blue - refBlue) <= threshold) {
                        graphics.fillRect(x, y, 1, 1);
                    }
                }
            }
            graphics.dispose();
            return new TextureData(path + "/tinted-" +
                    tintColor.getRed() + "-" + tintColor.getGreen() + "-" + tintColor.getBlue() + "-" + tintOpacity + "-" +
                    refColor.getRed() + "-" + refColor.getGreen() + "-" + refColor.getBlue() + "-" + threshold,
                    tintedImage);
        }

        public TextureData addText(String str, Font font, Color color, float xp, float yp, String alignment) {
            BufferedImage newImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = newImage.createGraphics();
            g.drawImage(image, 0, 0, null);
            g.setFont(font);
            g.setColor(color);
            FontMetrics metrics = g.getFontMetrics(font);
            int x = (int) (xp * image.getWidth());
            int y = (int) (yp * image.getHeight());

            if ("center".equalsIgnoreCase(alignment)) {
                x -= metrics.stringWidth(str) / 2;
                y += metrics.getAscent() / 2;
            } else if ("right".equalsIgnoreCase(alignment)) {
                x -= metrics.stringWidth(str);
                y += metrics.getAscent() / 2;
            } else if ("left".equalsIgnoreCase(alignment)) {
                y += metrics.getAscent() / 2;
            }

            g.drawString(str, x, y);
            g.dispose();
            return new TextureData(path + "/text-" + str, newImage);
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

        public TextureData pad(Color color, int top, int right, int left, int bottom) {
            final BufferedImage paddedImage = new BufferedImage(image.getWidth() + left + right, image.getHeight() + top + bottom, BufferedImage.TYPE_INT_ARGB);
            final Graphics2D graphics = paddedImage.createGraphics();
            graphics.setColor(color);
            graphics.fillRect(0, 0, paddedImage.getWidth(), paddedImage.getHeight());
            graphics.drawImage(image, left, top, null);
            graphics.dispose();
            return new TextureData(path + "/padded-" + top + "-" + right + "-" + left + "-" + bottom, paddedImage);
        }

        public TextureData scale(int newWidth, int newHeight) {
            return new TextureData(path + "/scaled-" + newWidth + "-" + newHeight, scaleImage(image, newWidth, newHeight));
        }

        public TextureData cropToVisibleArea() {
            // find first pixel top, left, right, bottom and crop to that area
            int top = 0;
            int left = 0;
            int right = image.getWidth();
            int bottom = image.getHeight();
            for (int y = 0; y < image.getHeight(); y++) {
                boolean hasContent = false;
                for (int x = 0; x < image.getWidth(); x++) {
                    if ((image.getRGB(x, y) & 0xFF000000) != 0) {
                        hasContent = true;
                        break;
                    }
                }
                if (hasContent) {
                    top = y;
                    break;
                }
            }
            for (int y = image.getHeight() - 1; y >= 0; y--) {
                boolean hasContent = false;
                for (int x = 0; x < image.getWidth(); x++) {
                    if ((image.getRGB(x, y) & 0xFF000000) != 0) {
                        hasContent = true;
                        break;
                    }
                }
                if (hasContent) {
                    bottom = y;
                    break;
                }
            }
            for (int x = 0; x < image.getWidth(); x++) {
                boolean hasContent = false;
                for (int y = 0; y < image.getHeight(); y++) {
                    if ((image.getRGB(x, y) & 0xFF000000) != 0) {
                        hasContent = true;
                        break;
                    }
                }
                if (hasContent) {
                    left = x;
                    break;
                }
            }
            for (int x = image.getWidth() - 1; x >= 0; x--) {
                boolean hasContent = false;
                for (int y = 0; y < image.getHeight(); y++) {
                    if ((image.getRGB(x, y) & 0xFF000000) != 0) {
                        hasContent = true;
                        break;
                    }
                }
                if (hasContent) {
                    right = x;
                    break;
                }
            }
            final BufferedImage croppedImage = new BufferedImage(right - left, bottom - top, BufferedImage.TYPE_INT_ARGB);
            final Graphics2D graphics = croppedImage.createGraphics();
            graphics.drawImage(image, 0, 0, right - left, bottom - top, left, top, right, bottom, null);
            graphics.dispose();
            return new TextureData(path + "/cropped", croppedImage);
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
        STATION_SHAPE_MONUMENT(TEXTURE_DIR + BOARD_DIR + "station_shape_monument.png"),

        CONNECTION_INTERSECTION(TEXTURE_DIR + BOARD_DIR + "crossing_"),
        CONNECTION_WALKED_INTERSECTION(TEXTURE_DIR + BOARD_DIR + "crossing_walked_"),

        SCORE_DISTRICTS_COUNT(TEXTURE_DIR + SCORE_DIR + "districts.png"),
        SCORE_MAX_STATIONS_IN_DISTRICT(TEXTURE_DIR + SCORE_DIR + "max_stations_in_district.png"),
        SCORE_RIVER_CROSSINGS(TEXTURE_DIR + SCORE_DIR + "river_crossings.png"),
        SCORE_CIRCLE(TEXTURE_DIR + SCORE_DIR + "circle.png"),
        SCORE_INTERCHANGE_STATIONS(TEXTURE_DIR + SCORE_DIR + "interchange_"),
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
