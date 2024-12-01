package de.yanwittmann.nextstation.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.yanwittmann.nextstation.model.board.BoardDistrict;
import de.yanwittmann.nextstation.model.board.RailwayConnection;
import de.yanwittmann.nextstation.model.board.RailwayConnectionIntersection;
import de.yanwittmann.nextstation.model.board.Station;
import de.yanwittmann.nextstation.model.card.StationCard;
import de.yanwittmann.nextstation.model.score.ScoreContributor;
import de.yanwittmann.nextstation.model.score.progress.ProgressScoreContributor;
import de.yanwittmann.nextstation.setup.BoardTemplates;
import de.yanwittmann.nextstation.util.TextureAccess;
import de.yanwittmann.nextstation.util.TextureProviderAdapterFactory;
import lombok.Data;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

@Data
public class GameBoard {

    // board properties
    private int width, height;

    // board elements
    private final List<BoardDistrict> districts = new ArrayList<>();
    private final List<Station> stations = new ArrayList<>();
    private final List<RailwayConnection> connections = new ArrayList<>();
    private final List<RailwayConnectionIntersection> intersections = new ArrayList<>();

    // cards
    private final List<StationCard> stationCards = new ArrayList<>();

    // scoring per turn: A * B + C
    private ScoreContributor turnWiseScoreContributorA;
    private ScoreContributor turnWiseScoreContributorB;
    private ScoreContributor turnWiseScoreContributorC;

    // scoring at end of game: A, B, C
    private ScoreContributor endGameScoreContributorA;
    private ScoreContributor endGameScoreContributorB;
    private ScoreContributor endGameScoreContributorC;

    // scoring shared objectives: A, B
    private ScoreContributor sharedObjectiveScoreContributorA;
    private ScoreContributor sharedObjectiveScoreContributorB;

    // scoring progress: A
    private ProgressScoreContributor progressScoreContributorA;

    public void finishSetup() {
        // connect stations with connections, compute neighbors
        final BoardTemplates temp = BoardTemplates.start(this);
        for (Station station : stations) {
            final Set<Station> neighbors = temp.findNeighbors(station);
            // add connections
            for (Station neighbor : neighbors) {
                connections.add(new RailwayConnection(station, neighbor));
            }
        }
    }

    public String serialize() {
        final Gson gson = new GsonBuilder()
                .registerTypeAdapterFactory(new TextureProviderAdapterFactory())
                .create();
        return gson.toJson(this);
    }

    public void writeSerialized(File dir) throws IOException {
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("Could not create directory: " + dir);
        }
        TextureAccess.cleanDirectory(dir);

        final String serialized = serialize();
        Files.writeString(new File(dir, "board-data.json").toPath(), serialized);

        // iterate over all elements that provide an icon and write them to the directory
        final File imgDir = new File(dir, "img");
        imgDir.mkdirs();

        for (Station station : stations) {
            TextureAccess.TextureData.write(station, imgDir);
        }
        for (StationCard card : stationCards) {
            TextureAccess.TextureData.write(card, imgDir);
        }

        TextureAccess.TextureData.write(turnWiseScoreContributorA, imgDir);
        TextureAccess.TextureData.write(turnWiseScoreContributorB, imgDir);
        TextureAccess.TextureData.write(turnWiseScoreContributorC, imgDir);
        TextureAccess.TextureData.write(endGameScoreContributorA, imgDir);
        TextureAccess.TextureData.write(endGameScoreContributorB, imgDir);
        TextureAccess.TextureData.write(endGameScoreContributorC, imgDir);
        TextureAccess.TextureData.write(sharedObjectiveScoreContributorA, imgDir);
        TextureAccess.TextureData.write(sharedObjectiveScoreContributorB, imgDir);
    }

    public Station getClosestStation(int x, int y) {
        Station closestStation = null;
        double closestDistance = Double.MAX_VALUE;
        for (Station station : stations) {
            final double distance = Math.sqrt(Math.pow(station.getX() - x, 2) + Math.pow(station.getY() - y, 2));
            if (distance < closestDistance) {
                closestDistance = distance;
                closestStation = station;
            }
        }
        return closestStation;
    }

    public Station getStationAt(int x, int y) {
        for (Station station : stations) {
            if (station.getX() == x && station.getY() == y) {
                return station;
            }
        }
        return null;
    }

    public BoardDistrict findDistrict(Station station) {
        // pick the smallest one that contains the station
        int smallestArea = Integer.MAX_VALUE;
        BoardDistrict smallestDistrict = null;

        for (BoardDistrict district : districts) {
            if (district.containsGeometrically(station)) {
                final int area = district.area();
                if (area < smallestArea) {
                    smallestArea = area;
                    smallestDistrict = district;
                }
            }
        }

        return smallestDistrict;
    }

    public Map<BoardDistrict, Set<Station>> computeStationsPerDistrict() {
        final Map<BoardDistrict, Set<Station>> stationsPerDistrict = new LinkedHashMap<>();
        for (BoardDistrict district : districts) {
            stationsPerDistrict.put(district, new HashSet<>());
        }
        for (Station station : stations) {
            final BoardDistrict district = findDistrict(station);
            if (district != null) {
                stationsPerDistrict.get(district).add(station);
            }
        }
        return stationsPerDistrict;
    }

    public BoardDistrict getClosestDistrict(int centerX, int centerY) {
        BoardDistrict closestDistrict = null;
        double closestDistance = Double.MAX_VALUE;
        for (BoardDistrict district : districts) {
            final int x = district.getX() + district.getWidth() / 2;
            final int y = district.getY() + district.getHeight() / 2;
            final double distance = Math.sqrt(Math.pow(x - centerX, 2) + Math.pow(y - centerY, 2));
            if (distance < closestDistance) {
                closestDistance = distance;
                closestDistrict = district;
            }
        }
        return closestDistrict;
    }
}
