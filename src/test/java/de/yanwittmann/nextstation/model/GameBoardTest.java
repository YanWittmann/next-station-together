package de.yanwittmann.nextstation.model;

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
                        BoardOptimizer.STATION_SPREAD, true, 200)
                .stationsPickStartingLocations()
                .stationStartingRedistributeTypes()
                .monumentPickRandomStationPerDistrict(2, 0.4f)
                .pickJokerStations(List.of(BoardTemplates.LooseDistrictDefinition.CENTERMOST))
                .connectionsConnectNeighbors()
                .connectionsPruneMaxDistance(5)

                .intersectionsAddRandomPerDistrict(3)
                .connectionsPruneInvalidIntersections()

                .riverGenerateRandomly(100, 5)
                // .scoreLondon()
                .scoreParis()
                .getBoard();

        final File outDir = new File("src/main/resources/web/boards/" + board.getRandomId());
        board.writeSerialized(outDir);

        Frame frame = BoardRendererTest.display(outDir);
        /*try {
            Thread.sleep(TimeUnit.MINUTES.toMillis(10));
        } catch (InterruptedException e) {
            log.error("Thread interrupted", e);
        }*/
        frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
    }

    @Test
    public void manyTest() throws IOException {
        for (int i = 0; i < 10; i++) {
            createBoardOptimizerTest();
        }
    }

}