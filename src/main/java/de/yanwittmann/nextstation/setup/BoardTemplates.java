package de.yanwittmann.nextstation.setup;

import de.yanwittmann.nextstation.model.GameBoard;
import de.yanwittmann.nextstation.model.board.BoardDistrict;
import de.yanwittmann.nextstation.model.board.Station;
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

    public GameBoard build() {
        return gameBoard;
    }

    // size

    public BoardTemplates sizeDefault() {
        gameBoard.setWidth(10);
        gameBoard.setHeight(10);
        return this;
    }

    // district layout

    public BoardTemplates districtsLondon() {
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

    // stations

    public BoardTemplates stationsFullyFillRandom() {
        for (int x = 0; x < gameBoard.getWidth(); x++) {
            for (int y = 0; y < gameBoard.getHeight(); y++) {
                gameBoard.getStations().add(Station.randomType(x, y));
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

            Station stationToRemove = candidates.get(random.nextInt(candidates.size()));
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

    public BoardTemplates stationRedistributeTypes(int maxIterations) {
        Random random = new Random();
        List<Station> stations = gameBoard.getStations();
        List<Station> startingPositions = stations.stream()
                .filter(station -> station.getStartingPosition() != -1)
                .collect(Collectors.toList());

        Map<Station, Set<Station.StationType>> reachabilityMap = new HashMap<>();
        for (Station start : startingPositions) {
            reachabilityMap.put(start, findReachableTypes(start));
        }

        for (int iteration = 0; iteration < maxIterations; iteration++) {
            if (iteration % 100 == 0) {
                log.info("stationRedistributeTypes iteration: {} / {}", iteration, maxIterations);
            }

            boolean allTypesReachableFromStart = startingPositions.stream()
                    .allMatch(start -> reachabilityMap.get(start).containsAll(Arrays.asList(Station.StationType.values())));

            if (allTypesReachableFromStart) {
                break;
            }

            for (Station start : startingPositions) {
                Set<Station.StationType> reachableTypes = reachabilityMap.get(start);
                if (!reachableTypes.containsAll(Arrays.asList(Station.StationType.values()))) {
                    for (Station neighbor : findNeighbors(start)) {
                        if (neighbor.getStartingPosition() == -1) {
                            Station.StationType originalType = neighbor.getType();
                            Station.StationType newType = Station.StationType.random();
                            neighbor.setType(newType);

                            Set<Station.StationType> newReachableTypes = findReachableTypes(start);
                            if (newReachableTypes.containsAll(Arrays.asList(Station.StationType.values()))) {
                                reachabilityMap.put(start, newReachableTypes);
                            } else {
                                neighbor.setType(originalType); // Revert if it doesn't improve reachability
                            }
                        }
                    }
                }
            }
        }

        return this;
    }

    private Set<Station.StationType> findReachableTypes(Station startStation) {
        Set<Station.StationType> reachableTypes = new HashSet<>();
        Queue<Station> queue = new LinkedList<>();
        Set<Station> visited = new HashSet<>();

        queue.add(startStation);
        visited.add(startStation);

        while (!queue.isEmpty()) {
            Station current = queue.poll();
            reachableTypes.add(current.getType());

            for (Station neighbor : findNeighbors(current)) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    queue.add(neighbor);
                }
            }
        }

        return reachableTypes;
    }
}
