package de.yanwittmann.nextstation.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.yanwittmann.nextstation.model.board.*;
import de.yanwittmann.nextstation.model.card.GameCard;
import de.yanwittmann.nextstation.model.score.ScoreContributor;
import de.yanwittmann.nextstation.model.score.progress.ProgressScoreContributor;
import de.yanwittmann.nextstation.util.TextureAccess;
import de.yanwittmann.nextstation.util.TextureProviderAdapterFactory;
import de.yanwittmann.nextstation.util.TmpIntersection;
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
    private RiverLayout riverLayout = new RiverLayout();

    // cards
    private final List<GameCard> stationCards = new ArrayList<>();
    private final List<GameCard> sharedObjectiveCards = new ArrayList<>();
    private final List<GameCard> bonusPerPenCards = new ArrayList<>();

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

    // scoring progress:
    private ProgressScoreContributor progressScoreContributor;

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

        // iterate over all elements that provide an icon and write them to the directory
        final File imgDir = new File(dir, "img");
        imgDir.mkdirs();

        for (Station station : stations) {
            TextureAccess.TextureData.write(station, imgDir);
        }
        for (GameCard card : stationCards) {
            TextureAccess.TextureData.write(card, imgDir);
        }
        for (GameCard card : sharedObjectiveCards) {
            TextureAccess.TextureData.write(card, imgDir);
        }
        for (GameCard card : bonusPerPenCards) {
            TextureAccess.TextureData.write(card, imgDir);
        }
        for (RailwayConnectionIntersection intersection : intersections) {
            TextureAccess.TextureData.write(intersection, imgDir);
        }

        TextureAccess.TextureData.write(turnWiseScoreContributorA, imgDir);
        TextureAccess.TextureData.write(turnWiseScoreContributorB, imgDir);
        TextureAccess.TextureData.write(turnWiseScoreContributorC, imgDir);
        TextureAccess.TextureData.write(endGameScoreContributorA, imgDir);
        TextureAccess.TextureData.write(endGameScoreContributorB, imgDir);
        TextureAccess.TextureData.write(endGameScoreContributorC, imgDir);
        TextureAccess.TextureData.write(sharedObjectiveScoreContributorA, imgDir);
        TextureAccess.TextureData.write(sharedObjectiveScoreContributorB, imgDir);
        if (progressScoreContributor != null) {
            TextureAccess.TextureData.write(progressScoreContributor.getTextures(), imgDir);
        }

        final String serialized = serialize();
        Files.writeString(new File(dir, "board-data.json").toPath(), serialized);
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
        return findDistrict(station.getX(), station.getY());
    }

    public BoardDistrict findDistrict(int x, int y) {
        // pick the smallest one that contains the station
        int smallestArea = Integer.MAX_VALUE;
        BoardDistrict smallestDistrict = null;

        for (BoardDistrict district : districts) {
            if (district.containsGeometrically(x, y)) {
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

    public List<TmpIntersection> findTrueConnectionIntersectionsInDistrict(BoardDistrict district) {
        final Map<Map.Entry<Integer, Integer>, TmpIntersection> intersections = new HashMap<>();
        // iterate over all connections to check if the lines intersect, do not use the "intersections" list for this, this is only for visual representation
        for (int i = 0; i < connections.size(); i++) {
            final RailwayConnection connectionA = connections.get(i);
            for (int j = i + 1; j < connections.size(); j++) {
                final RailwayConnection connectionB = connections.get(j);
                final Map.Entry<Integer, Integer> intersection = connectionA.intersection(connectionB);
                if (intersection != null && district.containsGeometrically(intersection.getKey(), intersection.getValue())) {
                    // intersection must not be a station
                    if (getStationAt(intersection.getKey(), intersection.getValue()) != null) {
                        continue;
                    }
                    final TmpIntersection tmpIntersection = intersections.computeIfAbsent(intersection, e -> new TmpIntersection(intersection.getKey(), intersection.getValue()));
                    // add directions
                    tmpIntersection.getDirections().add(connectionA.getDirection());
                    tmpIntersection.getDirections().add(connectionB.getDirection());
                    tmpIntersection.getConnections().add(connectionA);
                    tmpIntersection.getConnections().add(connectionB);
                }
            }
        }
        return new ArrayList<>(intersections.values());
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

    public String getRandomId() {
        return UUID.nameUUIDFromBytes(serialize().getBytes()).toString().substring(0, 8);
    }

    public Station addStation(int x, int y, Station.StationType type) {
        final Station e = new Station(x, y, type, false, -1);
        stations.add(e);
        return e;
    }
}
