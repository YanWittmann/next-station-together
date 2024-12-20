package de.yanwittmann.nextstation.model;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class BoardRendererTest extends Canvas {

    private final File dir;

    private final int width;
    private final int height;
    private final JSONArray districts;
    private final JSONArray stations;
    private final JSONArray connections;
    private final JSONArray intersections;
    private final JSONArray riverLayout;
    private final Map<String, Image> iconCache = new HashMap<>();

    // Global variable for station size
    public static int STATION_SIZE = 75;

    public BoardRendererTest(File dir, JSONObject boardData) {
        this.dir = dir;
        this.width = boardData.getInt("width");
        this.height = boardData.getInt("height");
        this.districts = boardData.getJSONArray("districts");
        this.stations = boardData.getJSONArray("stations");
        this.connections = boardData.getJSONArray("connections");
        this.intersections = boardData.getJSONArray("intersections");
        this.riverLayout = boardData.getJSONObject("riverLayout").getJSONArray("path");
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        drawRiver(g);
        drawDistricts(g);
        drawConnections(g);
        drawIntersections(g);
        drawStations(g);
    }

    private void drawDistricts(Graphics g) {
        g.setColor(Color.YELLOW);
        ((Graphics2D) g).setStroke(new BasicStroke(4));
        for (int i = 0; i < districts.length(); i++) {
            JSONObject district = districts.getJSONObject(i);
            int x = district.getInt("x") * STATION_SIZE;
            int y = district.getInt("y") * STATION_SIZE;
            int w = district.getInt("width") * STATION_SIZE;
            int h = district.getInt("height") * STATION_SIZE;
            g.drawRect(x, y, w, h);
        }
    }

    private void drawStations(Graphics g) {
        for (int i = 0; i < stations.length(); i++) {
            JSONObject station = stations.getJSONObject(i);
            int x = station.getInt("x") * STATION_SIZE;
            int y = station.getInt("y") * STATION_SIZE;
            String texture = station.getString("texture");

            Image icon = getIcon(texture);
            if (icon != null) {
                g.drawImage(icon, x + 5, y + 5, STATION_SIZE - 10, STATION_SIZE - 10, this);
            }
        }
    }

    private void drawConnections(Graphics g) {
        g.setColor(new Color(237, 237, 237));
        ((Graphics2D) g).setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{9}, 0));
        for (int i = 0; i < connections.length(); i++) {
            JSONObject connection = connections.getJSONObject(i);
            int x1 = connection.getInt("x1") * STATION_SIZE;
            int y1 = connection.getInt("y1") * STATION_SIZE;
            int x2 = connection.getInt("x2") * STATION_SIZE;
            int y2 = connection.getInt("y2") * STATION_SIZE;

            g.drawLine(x1 + STATION_SIZE / 2, y1 + STATION_SIZE / 2, x2 + STATION_SIZE / 2, y2 + STATION_SIZE / 2);
        }
    }

    private void drawIntersections(Graphics g) {
        // 3/5
        final int intersectionSize = STATION_SIZE * 3 / 5;
        for (int i = 0; i < intersections.length(); i++) {
            JSONObject intersection = intersections.getJSONObject(i);
            int x = intersection.getInt("x") * STATION_SIZE + (STATION_SIZE - intersectionSize) / 2;
            int y = intersection.getInt("y") * STATION_SIZE + (STATION_SIZE - intersectionSize) / 2;
            String texture = intersection.getString("texture");

            Image icon = getIcon(texture);
            if (icon != null) {
                g.drawImage(icon, x, y, intersectionSize, intersectionSize, this);
            }
        }
    }

    private void drawRiver(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setStroke(new BasicStroke(16, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = 0; i < riverLayout.length() - 1; i++) {
            JSONObject point1 = riverLayout.getJSONObject(i);
            JSONObject point2 = riverLayout.getJSONObject(i + 1);
            int x1 = (int) (point1.getFloat("x") * ((float) STATION_SIZE)) - STATION_SIZE / 2;
            int y1 = (int) (point1.getFloat("y") * ((float) STATION_SIZE)) - STATION_SIZE / 2;
            int x2 = (int) (point2.getFloat("x") * ((float) STATION_SIZE)) - STATION_SIZE / 2;
            int y2 = (int) (point2.getFloat("y") * ((float) STATION_SIZE)) - STATION_SIZE / 2;
            g.setColor(new Color(102, 196, 210));
            g.drawLine(x1, y1, x2, y2);
        }
    }

    private Image getIcon(String texture) {
        if (iconCache.containsKey(texture)) {
            return iconCache.get(texture);
        }

        try {
            Image icon = Toolkit.getDefaultToolkit().getImage(dir.getAbsolutePath() + "/img/" + texture + ".png");
            iconCache.put(texture, icon);
            return icon;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Frame display(File baseDir) throws IOException {
        String content = new String(Files.readAllBytes(Paths.get(baseDir.getAbsolutePath() + "/board-data.json")));
        JSONObject boardData = new JSONObject(content);

        Frame frame = new Frame("Board Renderer");
        BoardRendererTest boardRenderer = new BoardRendererTest(baseDir, boardData);
        frame.add(boardRenderer);
        frame.setSize(boardRenderer.width * STATION_SIZE + 20, boardRenderer.height * STATION_SIZE + 40);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                frame.dispose();
            }
        });

        // write screenshot to the arg directory
        // wait a second for the window to render
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // grab the boardRenderer's content
        Image image = boardRenderer.createImage(boardRenderer.getWidth(), boardRenderer.getHeight());
        Graphics graphics = image.getGraphics();
        boardRenderer.paint(graphics);
        try {
            ImageIO.write((RenderedImage) image, "png", new File(baseDir.getParent(), baseDir.getName() + ".png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return frame;
    }
}