package de.yanwittmann.nextstation.setup;

import de.yanwittmann.nextstation.model.GameBoard;
import de.yanwittmann.nextstation.model.board.*;
import de.yanwittmann.nextstation.util.TmpIntersection;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
public class BoardTemplates {
    private final GameBoard gameBoard;

    protected BoardTemplates(GameBoard gameBoard) {
        this.gameBoard = gameBoard;
    }

    public static BoardTemplates start() {
        return new BoardTemplates(new GameBoard());
    }

    public static BoardTemplates start(GameBoard gameBoard) {
        return new BoardTemplates(gameBoard);
    }

    public GameBoard getBoard() {
        return gameBoard;
    }

    // district layout

    public BoardTemplates districtsLondon() {
        gameBoard.setWidth(10);
        gameBoard.setHeight(10);

        // top row
        gameBoard.getDistricts().add(new BoardDistrict(0, 0, 1, 1));
        gameBoard.getDistricts().add(new BoardDistrict(0, 0, 3, 3));
        gameBoard.getDistricts().add(new BoardDistrict(3, 0, 4, 3));
        gameBoard.getDistricts().add(new BoardDistrict(7, 0, 3, 3));
        gameBoard.getDistricts().add(new BoardDistrict(9, 0, 1, 1));
        // middle row
        gameBoard.getDistricts().add(new BoardDistrict(0, 3, 3, 4));
        gameBoard.getDistricts().add(new BoardDistrict(3, 3, 4, 4));
        gameBoard.getDistricts().add(new BoardDistrict(7, 3, 3, 4));
        // bottom row
        gameBoard.getDistricts().add(new BoardDistrict(0, 9, 1, 1));
        gameBoard.getDistricts().add(new BoardDistrict(0, 7, 3, 3));
        gameBoard.getDistricts().add(new BoardDistrict(3, 7, 4, 3));
        gameBoard.getDistricts().add(new BoardDistrict(7, 7, 3, 3));
        gameBoard.getDistricts().add(new BoardDistrict(9, 9, 1, 1));
        return this;
    }

    public BoardTemplates districtsParis() {
        gameBoard.setWidth(10);
        gameBoard.setHeight(10);

        // top row
        gameBoard.getDistricts().add(new BoardDistrict(0, 0, 1, 1));
        gameBoard.getDistricts().add(new BoardDistrict(0, 0, 5, 2));
        gameBoard.getDistricts().add(new BoardDistrict(5, 0, 5, 2));
        gameBoard.getDistricts().add(new BoardDistrict(9, 0, 1, 1));

        // middle row
        gameBoard.getDistricts().add(new BoardDistrict(0, 2, 5, 3));
        gameBoard.getDistricts().add(new BoardDistrict(5, 2, 5, 3));
        gameBoard.getDistricts().add(new BoardDistrict(0, 5, 5, 3));
        gameBoard.getDistricts().add(new BoardDistrict(5, 5, 5, 3));

        // bottom row
        gameBoard.getDistricts().add(new BoardDistrict(0, 9, 1, 1));
        gameBoard.getDistricts().add(new BoardDistrict(0, 8, 5, 2));
        gameBoard.getDistricts().add(new BoardDistrict(5, 8, 5, 2));
        gameBoard.getDistricts().add(new BoardDistrict(9, 9, 1, 1));

        return this;
    }


    // stations

    public BoardTemplates stationsFullyFillRandom() {
        for (int x = 0; x < gameBoard.getWidth(); x++) {
            for (int y = 0; y < gameBoard.getHeight(); y++) {
                gameBoard.getStations().add(Station.randomType(x, y));
            }
        }
        return this;
    }

    public BoardTemplates stationsFullyFillEvenlyDistributed() {
        final int totalStations = gameBoard.getWidth() * gameBoard.getHeight();
        final int stationsPerType = totalStations / (Station.StationType.values().length - 1);
        final List<Station.StationType> distributeTypes = new ArrayList<>();
        for (Station.StationType type : Station.StationType.values()) {
            if (type == Station.StationType.JOKER) continue;
            for (int i = 0; i < stationsPerType; i++) {
                distributeTypes.add(type);
            }
        }
        for (int x = 0; x < gameBoard.getWidth(); x++) {
            for (int y = 0; y < gameBoard.getHeight(); y++) {
                final Station.StationType type = distributeTypes.remove((int) (Math.random() * distributeTypes.size()));
                gameBoard.getStations().add(Station.ofType(x, y, type));
            }
        }
        return this;
    }

    public BoardTemplates stationsPickStartingLocations() {
        // find the center of the board. create a vector that points upwards and rotate it by random 0 - 45 degrees. go forwards 2/5 of the board radius. pick the closest station to that point. rotate the vector by 90 degrees and repeat 3 more times.

        final int centerX = gameBoard.getWidth() / 2;
        final int centerY = gameBoard.getHeight() / 2;
        final double radius = Math.min(gameBoard.getWidth(), gameBoard.getHeight()) * 0.4;

        for (int i = 0; i < 4; i++) {
            final double angle = Math.toRadians((i * 90) + (Math.random() * 45));
            final int targetX = (int) (centerX + radius * Math.cos(angle));
            final int targetY = (int) (centerY + radius * Math.sin(angle));
            final Station closestStation = gameBoard.getClosestStation(targetX, targetY);
            if (closestStation != null) {
                closestStation.setStartingPosition(i);
                closestStation.setType(Station.StationType.values()[(i + 1) % 4]);
            }
        }

        return this;
    }

    public BoardTemplates monumentPickRandomStationPerDistrict(int minStationCount, float maxMonumentPercent) {
        int maxMonumentCount = (int) (gameBoard.getDistricts().size() * maxMonumentPercent);
        final List<Map.Entry<BoardDistrict, Set<Station>>> entries = new ArrayList<>(gameBoard.computeStationsPerDistrict().entrySet());
        Collections.shuffle(entries);
        int processedDistricts = 0;
        for (int i = 0; i < entries.size() && processedDistricts < maxMonumentCount; i++) {
            final Map.Entry<BoardDistrict, Set<Station>> district = entries.get(i);
            if (district.getValue().size() < minStationCount) continue;
            processedDistricts++;

            final List<Station> candidates = district.getValue().stream()
                    .filter(station -> station.getStartingPosition() == -1)
                    .collect(Collectors.toList());
            Collections.shuffle(candidates);
            /*candidates.stream()
                    .skip((int) (Math.random() * district.getValue().size()))
                    .findFirst()
                    .ifPresent(randomStation -> randomStation.setMonument(true));*/
            if (!candidates.isEmpty()) {
                boolean monumentPlaced = false;
                for (Station candidate : candidates) {
                    // check if any neighbor is a starting position or already a monument, if so, skip this station
                    if (findNeighbors(candidate).stream().anyMatch(neighbor -> neighbor.getStartingPosition() != -1 || neighbor.isMonument())) {
                        continue;
                    }
                    candidate.setMonument(true);
                    monumentPlaced = true;
                    break;
                }
                if (!monumentPlaced) {
                    log.warn("Failed to place a monument in district on a station that is not a starting position or a neighbor of one, picking random");
                    candidates.get(0).setMonument(true);
                }
            }
        }
        return this;
    }

    public BoardTemplates pickJokerStations(List<LooseDistrictDefinition> districts) {
        final Map<BoardDistrict, Set<Station>> districtsStations = gameBoard.computeStationsPerDistrict();
        AtomicInteger jokerCount = new AtomicInteger();
        for (LooseDistrictDefinition definition : districts) {
            final BoardDistrict district = definition.getDistrictFinder().apply(gameBoard);
            if (district != null) {
                districtsStations.get(district).stream()
                        .filter(Station::isMonument)
                        .findFirst()
                        .ifPresent(station -> {
                            station.setType(Station.StationType.JOKER);
                            jokerCount.getAndIncrement();
                        });
            }
        }

        if (jokerCount.get() == 0) {
            log.warn("No joker stations were picked, picking random");
            List<Station> stations = new ArrayList<>(gameBoard.getStations());
            Collections.shuffle(stations);
            stations.stream()
                    .filter(Station::isMonument)
                    .findFirst()
                    .ifPresent(station -> station.setType(Station.StationType.JOKER));
        }

        return this;
    }

    @Getter
    public enum LooseDistrictDefinition {
        CENTERMOST(gameBoard -> {
            final int centerX = gameBoard.getWidth() / 2;
            final int centerY = gameBoard.getHeight() / 2;
            return gameBoard.getClosestDistrict(centerX, centerY);
        }),
        LARGEST(gameBoard -> {
            BoardDistrict largestDistrict = null;
            int largestArea = Integer.MIN_VALUE;
            for (BoardDistrict district : gameBoard.getDistricts()) {
                final int area = district.area();
                if (area > largestArea) {
                    largestArea = area;
                    largestDistrict = district;
                }
            }
            return largestDistrict;
        }),
        SMALLEST(gameBoard -> {
            BoardDistrict smallestDistrict = null;
            int smallestArea = Integer.MAX_VALUE;
            for (BoardDistrict district : gameBoard.getDistricts()) {
                final int area = district.area();
                if (area < smallestArea) {
                    smallestArea = area;
                    smallestDistrict = district;
                }
            }
            return smallestDistrict;
        });

        private final Function<GameBoard, BoardDistrict> districtFinder;

        LooseDistrictDefinition(Function<GameBoard, BoardDistrict> districtFinder) {
            this.districtFinder = districtFinder;
        }
    }

    public BoardTemplates stationsRemovePercent(
            float removePercent,
            Map<LooseDistrictDefinition, Integer> minStationsPerDistrictInput, int minStationsPerDistrictFallback
    ) {

        Map<BoardDistrict, Set<Station>> districtStations = gameBoard.computeStationsPerDistrict();

        int totalStations = gameBoard.getStations().size();
        int stationsToRemove = Math.round(totalStations * removePercent);
        Set<Station> removableStations = new HashSet<>(gameBoard.getStations());

        // populate the minStationsPerDistrict using the minStationsPerDistrictInput and the fallback values for the missing districts
        final Map<BoardDistrict, Integer> minStationsPerDistrict = new HashMap<>();
        for (Map.Entry<LooseDistrictDefinition, Integer> entry : minStationsPerDistrictInput.entrySet()) {
            final LooseDistrictDefinition definition = entry.getKey();
            final int minStations = entry.getValue();
            final BoardDistrict district = definition.getDistrictFinder().apply(gameBoard);
            if (district != null) {
                minStationsPerDistrict.put(district, minStations);
            }
        }
        for (BoardDistrict district : districtStations.keySet()) {
            minStationsPerDistrict.putIfAbsent(district, minStationsPerDistrictFallback);
        }

        // Initial filtering based on minStationsPerDistrict
        removableStations.removeIf(station -> {
            BoardDistrict district = gameBoard.findDistrict(station);
            return district != null && districtStations.get(district).size() <= minStationsPerDistrict.get(district);
        });

        Random random = new Random();
        for (int i = 0; i < stationsToRemove; i++) {
            List<Station> candidates = removableStations.stream()
                    .filter(station -> hasLessThanNeighbors(station, 3))
                    .filter(station -> {
                        BoardDistrict district = gameBoard.findDistrict(station);
                        return district == null || districtStations.get(district).size() > minStationsPerDistrict.get(district);
                    })
                    .collect(Collectors.toList());

            if (candidates.isEmpty()) {
                break; // No more removable stations
            }

            // Prioritize stations in districts with more stations
            candidates.sort(Comparator.comparingInt(station -> {
                BoardDistrict district = gameBoard.findDistrict(station);
                return district == null ? 0 : -districtStations.get(district).size();
            }));

            Station stationToRemove = candidates.get(random.nextInt(Math.min(candidates.size(), 3)));
            removableStations.remove(stationToRemove);
            gameBoard.getStations().remove(stationToRemove);

            // Update the district stations map
            BoardDistrict district = gameBoard.findDistrict(stationToRemove);
            if (district != null) {
                districtStations.get(district).remove(stationToRemove);
                if (districtStations.get(district).size() <= minStationsPerDistrict.get(district)) {
                    removableStations.removeAll(districtStations.get(district));
                }
            }
        }

        return this;
    }

    private boolean hasLessThanNeighbors(Station station, int maxNeighbors) {
        Set<Station> neighbors = findNeighbors(station);
        for (Station neighbor : neighbors) {
            if (countConnections(neighbor) <= maxNeighbors) {
                return false;
            }
        }
        return true;
    }

    public Set<Station> findNeighbors(Station station) {
        Set<Station> neighbors = new HashSet<>();
        int[][] directions = {
                {-1, -1}, {-1, 0}, {-1, 1},
                {0, -1}, {0, 1},
                {1, -1}, {1, 0}, {1, 1}
        };

        for (int[] direction : directions) {
            int x = station.getX();
            int y = station.getY();
            while (true) {
                x += direction[0];
                y += direction[1];
                final Station neighbor = gameBoard.getStationAt(x, y);
                if (neighbor != null) {
                    neighbors.add(neighbor);
                    break;
                }
                if (x < 0 || x >= gameBoard.getWidth() || y < 0 || y >= gameBoard.getHeight()) {
                    break;
                }
            }
        }
        return neighbors;
    }

    private int countConnections(Station station) {
        return findNeighbors(station).size();
    }

    public BoardTemplates stationStartingRedistributeTypes() {
        final List<Station> startingPositions = gameBoard.getStations().stream()
                .filter(station -> station.getStartingPosition() != -1)
                .collect(Collectors.toList());

        for (Station startingPosition : startingPositions) {
            // make sure that all starting positions can access at least 3/4 of the types by changing the type of the neighboring positions
            final Set<Station> neighbors = findNeighbors(startingPosition);
            final Set<Station.StationType> types = neighbors.stream()
                    .map(Station::getType)
                    .collect(Collectors.toSet());
            final List<Station.StationType> missingTypes = new ArrayList<>(Arrays.asList(Station.StationType.values()));
            missingTypes.remove(Station.StationType.JOKER);
            missingTypes.removeAll(types);

            if (missingTypes.isEmpty()) {
                continue;
            }

            // find doubles
            final Map<Station.StationType, Long> typeCounts = neighbors.stream()
                    .collect(Collectors.groupingBy(Station::getType, Collectors.counting()));
            final List<Station.StationType> doubles = typeCounts.entrySet().stream()
                    .filter(entry -> entry.getValue() > 1)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
            if (doubles.isEmpty()) {
                continue;
            }

            // retype one of the doubles to the missing types
            Collections.shuffle(doubles);
            Collections.shuffle(missingTypes);
            for (Station.StationType missingType : missingTypes) {
                if (doubles.isEmpty()) {
                    log.warn("Failed to redistribute station types, not enough doubles");
                    break;
                }
                final Station.StationType doubleType = doubles.remove(0);
                neighbors.stream()
                        .filter(neighbor -> neighbor.getType() == doubleType)
                        .findFirst()
                        .ifPresent(neighbor -> {
                            log.info("Retyped station at {} to {}", neighbor, missingType);
                            neighbor.setType(missingType);
                        });
            }
        }

        return this;
    }

    // connections

    public BoardTemplates connectionsConnectNeighbors() {
        // connect stations with connections, compute neighbors
        for (Station station : gameBoard.getStations()) {
            for (Station neighbor : findNeighbors(station)) {
                gameBoard.getConnections().add(new RailwayConnection(station, neighbor));
            }
        }
        return this;
    }

    public BoardTemplates connectionsPruneMaxDistance(int maxDistance) {
        gameBoard.getConnections().removeIf(connection -> {
            final int distance = (int) Math.sqrt(Math.pow(connection.getX1() - connection.getX2(), 2) + Math.pow(connection.getY1() - connection.getY2(), 2));
            return distance > maxDistance;
        });
        return this;
    }

    // intersections

    public BoardTemplates intersectionsAddRandomPerDistrict(int minStationCount) {
        final Map<BoardDistrict, Set<Station>> districtStations = gameBoard.computeStationsPerDistrict();
        final List<BoardDistrict> districts = districtStations.entrySet().stream()
                .filter(entry -> entry.getValue().size() >= minStationCount)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        final Set<RailwayConnection> ignoreConnections = new HashSet<>();

        for (BoardDistrict district : districts) {
            final List<TmpIntersection> districtIntersections = gameBoard.findTrueConnectionIntersectionsInDistrict(district);
            if (districtIntersections.isEmpty()) {
                continue;
            }

            // check if any of the intersections are already in use
            districtIntersections.removeIf(districtIntersection -> {
                for (RailwayConnection checkConnection : districtIntersection.getConnections()) {
                    if (ignoreConnections.contains(checkConnection)) {
                        return true;
                    }
                }
                return false;
            });

            if (districtIntersections.isEmpty()) {
                log.warn("No valid intersections found in district {}", district);
                continue;
            }

            // pick random intersection
            final TmpIntersection intersection = districtIntersections.get((int) (Math.random() * districtIntersections.size()));
            final RailwayConnectionIntersection properIntersection = new RailwayConnectionIntersection(intersection.getX(), intersection.getY());
            // pick two random directions from the intersection
            final List<RailwayConnectionIntersection.Direction> directions = new ArrayList<>(intersection.getDirections());
            Collections.shuffle(directions);
            properIntersection.setTop(directions.get(0));
            properIntersection.setBottom(directions.get(1));

            ignoreConnections.addAll(intersection.getConnections());

            gameBoard.getIntersections().add(properIntersection);
        }

        return this;
    }

    // connections, clear those that hit invalid intersections
    public BoardTemplates connectionsPruneInvalidIntersections() {
        gameBoard.getConnections().removeIf(connection -> {
            for (RailwayConnectionIntersection intersection : gameBoard.getIntersections()) {
                if (intersection.intersects(connection)) {
                    // candidate for removal, now check if direction of connection is valid
                    final RailwayConnectionIntersection.Direction direction = connection.getDirection();
                    if (direction == null || !intersection.containsDirection(direction)) {
                        return true;
                    }
                }
            }
            return false;
        });
        return this;
    }

    // river

    public BoardTemplates riverGenerateRandomly(int attempts, int targetLengthAddition) {
        Random random = new Random();

        final int targetLength = (gameBoard.getWidth() + gameBoard.getHeight()) / 2 + targetLengthAddition;
        int bestScore = Integer.MIN_VALUE;
        RiverLayout bestRiver = null;

        for (int a = 0; a < attempts; a++) {
            RiverLayout riverLayout = new RiverLayout();

            final float padding = 1.0f;

            // start anywhere around the border
            RiverLayout.Direction riverDirection = RiverLayout.Direction.values()[(int) (Math.random() * 4)];
            float x = -padding, y = -padding;
            final float randomX = 3 + random.nextInt(gameBoard.getWidth() - 3);
            final float randomY = 3 + random.nextInt(gameBoard.getHeight() - 3);
            if (riverDirection == RiverLayout.Direction.DOWN) {
                x = randomX;
            } else if (riverDirection == RiverLayout.Direction.UP) {
                x = randomX;
                y = gameBoard.getHeight() + padding;
            } else if (riverDirection == RiverLayout.Direction.LEFT) {
                x = gameBoard.getWidth() + padding;
                y = randomY;
            } else if (riverDirection == RiverLayout.Direction.RIGHT) {
                y = randomY;
            }
            x += 0.5f;
            y += 0.5f;

            riverLayout.getPath().add(new RiverLayout.Point(x, y));

            x += riverDirection.getDx();
            y += riverDirection.getDy();
            riverLayout.getPath().add(new RiverLayout.Point(x, y));

            for (int i = 0; i < 40; i++) {
                final RiverLayout.Direction previousDirection = riverDirection;
                riverDirection = riverDirection.divergeChance(0.4f);

                if (!previousDirection.isDiagonal() && riverDirection.isDiagonal()) {
                    x += previousDirection.getDx() * 0.5f;
                    y += previousDirection.getDy() * 0.5f;
                    riverLayout.getPath().add(new RiverLayout.Point(x, y));
                }

                if (previousDirection.isDiagonal() && !riverDirection.isDiagonal()) {
                    boolean isNextHalfStepOffGridX = Math.abs((x + riverDirection.getDx() * 0.5f) % 1) > 0.1;
                    boolean isNextHalfStepOffGridY = Math.abs((y + riverDirection.getDy() * 0.5f) % 1) > 0.1;

                    if (isNextHalfStepOffGridX && isNextHalfStepOffGridY) {
                        x += riverDirection.getDx() * 0.5f;
                        y += riverDirection.getDy() * 0.5f;
                        riverLayout.getPath().add(new RiverLayout.Point(x, y));
                    } else {
                        x += previousDirection.getDx() * 0.5f;
                        y += previousDirection.getDy() * 0.5f;
                        riverLayout.getPath().add(new RiverLayout.Point(x, y));

                        x += riverDirection.getDx() * 0.5f;
                        y += riverDirection.getDy() * 0.5f;
                        riverLayout.getPath().add(new RiverLayout.Point(x, y));
                    }
                }


                if (riverDirection.isDiagonal()) {
                    x += riverDirection.getDx() * 0.5f;
                    y += riverDirection.getDy() * 0.5f;
                } else {
                    x += riverDirection.getDx();
                    y += riverDirection.getDy();
                }

                riverLayout.getPath().add(new RiverLayout.Point(x, y));

                // check if the river is out of bounds
                if (x < 0 || x >= gameBoard.getWidth() || y < 0 || y >= gameBoard.getHeight()) {
                    break;
                }
            }

            // score calculation
            int score = 0;

            // add points based on how close the river length is to the target length
            int lengthDiff = (int) Math.pow(Math.abs(riverLayout.pathLength() - targetLength), 2);
            score += 100 - lengthDiff;

            // add points based on how many districts are visited
            Set<BoardDistrict> visitedDistricts = new HashSet<>();
            for (RiverLayout.Point point : riverLayout.getPath()) {
                final BoardDistrict district = gameBoard.findDistrict((int) point.getX(), (int) point.getY());
                if (district != null) {
                    visitedDistricts.add(district);
                }
            }
            score += visitedDistricts.size() * 10;

            if (bestRiver == null || score > bestScore) {
                bestScore = score;
                bestRiver = riverLayout;
            }
        }

        log.info("Generated river layout of length [{} - {}]", bestRiver.getPath().size(),  bestRiver.pathLength());

        gameBoard.setRiverLayout(bestRiver);

        return this;
    }
}
