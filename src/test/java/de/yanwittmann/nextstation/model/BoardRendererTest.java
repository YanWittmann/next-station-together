package de.yanwittmann.nextstation.model;

import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class BoardRendererTest extends Canvas {

    private final int width;
    private final int height;
    private final JSONArray districts;
    private final JSONArray stations;
    private final JSONArray connections;
    private final Map<String, Image> iconCache = new HashMap<>();

    // Global variable for station size
    public static int STATION_SIZE = 75;

    public BoardRendererTest(JSONObject boardData) {
        this.width = boardData.getInt("width");
        this.height = boardData.getInt("height");
        this.districts = boardData.getJSONArray("districts");
        this.stations = boardData.getJSONArray("stations");
        this.connections = boardData.getJSONArray("connections");
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        drawDistricts(g);
        drawConnections(g);
        drawStations(g);
    }

    private void drawDistricts(Graphics g) {
        g.setColor(Color.YELLOW);
        ((Graphics2D) g).setStroke(new BasicStroke(2));
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
        g.setColor(Color.LIGHT_GRAY);
        ((Graphics2D) g).setStroke(new BasicStroke(1));
        for (int i = 0; i < connections.length(); i++) {
            JSONObject connection = connections.getJSONObject(i);
            int x1 = connection.getInt("x1") * STATION_SIZE;
            int y1 = connection.getInt("y1") * STATION_SIZE;
            int x2 = connection.getInt("x2") * STATION_SIZE;
            int y2 = connection.getInt("y2") * STATION_SIZE;

            g.drawLine(x1 + STATION_SIZE / 2, y1 + STATION_SIZE / 2, x2 + STATION_SIZE / 2, y2 + STATION_SIZE / 2);
        }
    }

    private Image getIcon(String texture) {
        if (iconCache.containsKey(texture)) {
            return iconCache.get(texture);
        }

        try {
            Image icon = Toolkit.getDefaultToolkit().getImage("target/createBoardTest/img/" + texture + ".png");
            iconCache.put(texture, icon);
            return icon;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) throws IOException {
        String content = new String(Files.readAllBytes(Paths.get("target/createBoardTest/board-data.json")));
        JSONObject boardData = new JSONObject(content);

        Frame frame = new Frame("Board Renderer");
        BoardRendererTest boardRenderer = new BoardRendererTest(boardData);
        frame.add(boardRenderer);
        frame.setSize(boardRenderer.width * STATION_SIZE + 20, boardRenderer.height * STATION_SIZE + 40);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                System.exit(0);
            }
        });
    }
}