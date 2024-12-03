package de.yanwittmann.nextstation.model;

import de.yanwittmann.nextstation.model.board.RiverLayout;
import de.yanwittmann.nextstation.model.board.Station;
import de.yanwittmann.nextstation.setup.BoardOptimizer;
import de.yanwittmann.nextstation.setup.BoardTemplates;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
class GameBoardTest {

    @Test
    public void createBoardOptimizerTest() throws IOException {
        final GameBoard board = BoardOptimizer.runIterations(() -> BoardTemplates.start()
                                /*.districtsLondon()
                                .stationsFullyFillEvenlyDistributed()
                                .stationsRemovePercent(0.47f, Map.of(BoardTemplates.LooseDistrictDefinition.CENTERMOST, 9), 4),*/
                                .districtsParis()
                                .stationsFullyFillEvenlyDistributed()
                                .stationsRemovePercent(0.52f, Map.of(BoardTemplates.LooseDistrictDefinition.CENTERMOST, 9), 4),
                        BoardOptimizer.STATION_SPREAD, true, 1)
                .stationsRedistributeTypes()
                .stationsPickStartingLocations()
                .stationStartingRedistributeTypes()
                .monumentPickRandomStationPerDistrict(2, 0.4f)
                .pickJokerStations(List.of(BoardTemplates.LooseDistrictDefinition.CENTERMOST))
                .connectionsConnectNeighbors()
                .connectionsPruneMaxDistance(5)

                .intersectionsAddRandomPerDistrict(3)
                .connectionsPruneInvalidIntersections()

                .riverGenerateRandomly(100, 5)

                .cardsStationRegular()

                // .scoreLondon()
                .scoreParis().monumentMakeAllJoker().cardsStationParis()

                .cardsSharedObjectiveAll()

                .getBoard();

        final File outDir = new File("src/main/resources/web/boards/" + board.getRandomId());
        board.writeSerialized(outDir);

        Frame frame = BoardRendererTest.display(outDir);
        try {
            Thread.sleep(TimeUnit.MINUTES.toMillis(10));
        } catch (InterruptedException e) {
            log.error("Thread interrupted", e);
        }
        frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
    }

    @Test
    public void manyTest() throws IOException {
        for (int i = 0; i < 10; i++) {
            createBoardOptimizerTest();
        }
    }

    @Test
    public void createBaseLondon() throws IOException {
        final GameBoard board = BoardTemplates.start().districtsLondon().getBoard();

        board.addStation(0, 0, Station.StationType.PENTAGON);
        board.addStation(1, 0, Station.StationType.TRIANGLE);
        board.addStation(2, 0, Station.StationType.RECTANGLE);
        board.addStation(4, 0, Station.StationType.TRIANGLE);
        board.addStation(5, 0, Station.StationType.CIRCLE);
        board.addStation(7, 0, Station.StationType.TRIANGLE);
        board.addStation(9, 0, Station.StationType.CIRCLE);

        board.addStation(1, 1, Station.StationType.PENTAGON);
        board.addStation(3, 1, Station.StationType.RECTANGLE);
        board.addStation(6, 1, Station.StationType.PENTAGON).setMonument(true);
        board.addStation(8, 1, Station.StationType.RECTANGLE);
        board.addStation(9, 1, Station.StationType.PENTAGON);

        board.addStation(0, 2, Station.StationType.CIRCLE);
        board.addStation(3, 2, Station.StationType.TRIANGLE).setStartingPosition(0);
        board.addStation(6, 2, Station.StationType.RECTANGLE);
        board.addStation(9, 2, Station.StationType.TRIANGLE);

        board.addStation(0, 3, Station.StationType.RECTANGLE).setMonument(true);
        board.addStation(2, 3, Station.StationType.PENTAGON);
        board.addStation(4, 3, Station.StationType.TRIANGLE);
        board.addStation(5, 3, Station.StationType.JOKER).setMonument(true);
        board.addStation(6, 3, Station.StationType.CIRCLE);
        board.addStation(7, 3, Station.StationType.CIRCLE).setStartingPosition(1);
        board.addStation(9, 3, Station.StationType.RECTANGLE);

        board.addStation(1, 4, Station.StationType.TRIANGLE);
        board.addStation(2, 4, Station.StationType.RECTANGLE);
        board.addStation(4, 4, Station.StationType.PENTAGON);
        board.addStation(5, 4, Station.StationType.RECTANGLE);
        board.addStation(8, 4, Station.StationType.PENTAGON);

        board.addStation(0, 5, Station.StationType.PENTAGON);
        board.addStation(2, 5, Station.StationType.RECTANGLE).setStartingPosition(2);
        board.addStation(4, 5, Station.StationType.CIRCLE);
        board.addStation(7, 5, Station.StationType.CIRCLE);

        board.addStation(3, 6, Station.StationType.PENTAGON);
        board.addStation(4, 6, Station.StationType.TRIANGLE);
        board.addStation(6, 6, Station.StationType.RECTANGLE);
        board.addStation(7, 6, Station.StationType.TRIANGLE);
        board.addStation(9, 6, Station.StationType.TRIANGLE).setMonument(true);

        board.addStation(0, 7, Station.StationType.CIRCLE);
        board.addStation(2, 7, Station.StationType.RECTANGLE);
        board.addStation(3, 7, Station.StationType.CIRCLE);
        board.addStation(5, 7, Station.StationType.PENTAGON).setStartingPosition(3);
        board.addStation(8, 7, Station.StationType.CIRCLE);
        board.addStation(9, 7, Station.StationType.PENTAGON);

        board.addStation(1, 8, Station.StationType.CIRCLE);
        board.addStation(6, 8, Station.StationType.PENTAGON);
        board.addStation(8, 8, Station.StationType.TRIANGLE);

        board.addStation(0, 9, Station.StationType.TRIANGLE);
        board.addStation(1, 9, Station.StationType.RECTANGLE);
        board.addStation(3, 9, Station.StationType.PENTAGON);
        board.addStation(4, 9, Station.StationType.CIRCLE).setMonument(true);
        board.addStation(5, 9, Station.StationType.TRIANGLE);
        board.addStation(7, 9, Station.StationType.CIRCLE);
        board.addStation(9, 9, Station.StationType.RECTANGLE);

        RiverLayout riverLayout = board.getRiverLayout();
        riverLayout.addPoints(new RiverLayout.Point(-1.0f, 4.0f));
        riverLayout.addPoint(RiverLayout.Direction.RIGHT, 3.5f);
        riverLayout.addPoint(RiverLayout.Direction.RIGHT_DOWN, 2);
        riverLayout.addPoint(RiverLayout.Direction.RIGHT, 1);
        riverLayout.addPoint(RiverLayout.Direction.UP_RIGHT, 1);
        riverLayout.addPoint(RiverLayout.Direction.RIGHT, 5);

        BoardTemplates.start(board)
                .connectionsConnectNeighbors()
                .connectionsPruneMaxDistance(5)
                .scoreLondon();

        final File outDir = new File("src/main/resources/web/boards/next-station-together-london");
        board.writeSerialized(outDir);

        Frame frame = BoardRendererTest.display(outDir);
        try {
            Thread.sleep(TimeUnit.MILLISECONDS.toMillis(300));
        } catch (InterruptedException e) {
            log.error("Thread interrupted", e);
        }
        frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
    }

    @Test
    public void createBaseParis() throws IOException {
        final GameBoard board = BoardTemplates.start().districtsParis().districtsParisAddCenter().getBoard();

        board.addStation(0, 0, Station.StationType.RECTANGLE);
        board.addStation(1, 0, Station.StationType.CIRCLE);
        board.addStation(3, 0, Station.StationType.TRIANGLE);
        board.addStation(6, 0, Station.StationType.RECTANGLE);
        board.addStation(7, 0, Station.StationType.TRIANGLE);
        board.addStation(9, 0, Station.StationType.CIRCLE);

        board.addStation(0, 1, Station.StationType.JOKER).setMonument(true);
        board.addStation(1, 1, Station.StationType.RECTANGLE);
        board.addStation(4, 1, Station.StationType.PENTAGON);
        board.addStation(5, 1, Station.StationType.CIRCLE);
        board.addStation(8, 1, Station.StationType.JOKER).setMonument(true);
        board.addStation(9, 1, Station.StationType.PENTAGON);

        board.addStation(1, 2, Station.StationType.TRIANGLE);
        board.addStation(3, 2, Station.StationType.CIRCLE).setStartingPosition(0);
        board.addStation(5, 2, Station.StationType.RECTANGLE);
        board.addStation(8, 2, Station.StationType.PENTAGON);

        board.addStation(1, 3, Station.StationType.JOKER).setMonument(true);
        board.addStation(2, 3, Station.StationType.PENTAGON);
        board.addStation(3, 3, Station.StationType.RECTANGLE);
        board.addStation(4, 3, Station.StationType.CIRCLE);
        board.addStation(6, 3, Station.StationType.TRIANGLE);
        board.addStation(7, 3, Station.StationType.PENTAGON).setStartingPosition(1);
        board.addStation(9, 3, Station.StationType.RECTANGLE);

        board.addStation(0, 4, Station.StationType.TRIANGLE);
        board.addStation(6, 4, Station.StationType.CIRCLE);
        board.addStation(9, 4, Station.StationType.JOKER).setMonument(true);

        board.addStation(0, 5, Station.StationType.JOKER).setMonument(true);
        board.addStation(3, 5, Station.StationType.RECTANGLE);
        board.addStation(7, 5, Station.StationType.PENTAGON);
        board.addStation(9, 5, Station.StationType.RECTANGLE);

        board.addStation(1, 6, Station.StationType.CIRCLE);
        board.addStation(2, 6, Station.StationType.TRIANGLE).setStartingPosition(2);
        board.addStation(4, 6, Station.StationType.TRIANGLE);
        board.addStation(5, 6, Station.StationType.JOKER).setMonument(true);
        board.addStation(6, 6, Station.StationType.TRIANGLE);

        board.addStation(2, 7, Station.StationType.PENTAGON);
        board.addStation(4, 7, Station.StationType.PENTAGON);
        board.addStation(5, 7, Station.StationType.CIRCLE);
        board.addStation(7, 7, Station.StationType.RECTANGLE).setStartingPosition(3);
        board.addStation(8, 7, Station.StationType.CIRCLE);

        board.addStation(0, 8, Station.StationType.RECTANGLE);
        board.addStation(1, 8, Station.StationType.TRIANGLE);
        board.addStation(3, 8, Station.StationType.PENTAGON);
        board.addStation(4, 8, Station.StationType.CIRCLE);
        board.addStation(8, 8, Station.StationType.PENTAGON);
        board.addStation(9, 8, Station.StationType.CIRCLE);

        board.addStation(0, 9, Station.StationType.PENTAGON);
        board.addStation(2, 9, Station.StationType.JOKER).setMonument(true);
        board.addStation(5, 9, Station.StationType.TRIANGLE);
        board.addStation(6, 9, Station.StationType.JOKER).setMonument(true);
        board.addStation(7, 9, Station.StationType.RECTANGLE);
        board.addStation(9, 9, Station.StationType.TRIANGLE);

        board.addStation(4, 4, Station.StationType.JOKER);
        board.addStation(5, 4, Station.StationType.JOKER);
        board.addStation(4, 5, Station.StationType.JOKER);
        board.addStation(5, 5, Station.StationType.JOKER);

        RiverLayout riverLayout = board.getRiverLayout();

        riverLayout.addPoints(new RiverLayout.Point(2, board.getHeight() + 1));
        riverLayout.addPoint(RiverLayout.Direction.UP, 3.5f);
        riverLayout.addPoint(RiverLayout.Direction.UP_RIGHT, 1.5f);
        riverLayout.addPoint(RiverLayout.Direction.RIGHT, 3f);
        riverLayout.addPoint(RiverLayout.Direction.RIGHT_DOWN, 6f);

        riverLayout.addPoints(new RiverLayout.Point(2, board.getHeight() + 1));
        riverLayout.addPoint(RiverLayout.Direction.UP, 3.5f);
        riverLayout.addPoint(RiverLayout.Direction.UP_RIGHT, 1.5f);
        riverLayout.addPoint(RiverLayout.Direction.RIGHT, 1f);
        riverLayout.addPoint(RiverLayout.Direction.RIGHT_DOWN, 1f);
        riverLayout.addPoint(RiverLayout.Direction.RIGHT, 2f);

        BoardTemplates.start(board)
                .connectionsConnectNeighbors()
                .connectionsPruneMaxDistance(5)
                .connectionsPruneInvalidIntersections()
                .scoreParis();

        final File outDir = new File("src/main/resources/web/boards/next-station-together-paris");
        board.writeSerialized(outDir);

        Frame frame = BoardRendererTest.display(outDir);
        try {
            Thread.sleep(TimeUnit.MILLISECONDS.toMillis(300));
        } catch (InterruptedException e) {
            log.error("Thread interrupted", e);
        }
        frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
    }
}