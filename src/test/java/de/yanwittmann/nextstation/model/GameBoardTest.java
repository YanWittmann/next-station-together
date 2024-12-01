package de.yanwittmann.nextstation.model;

import de.yanwittmann.nextstation.setup.BoardTemplates;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
class GameBoardTest {

    @Test
    public void createBoardTest() throws IOException {
        final GameBoard board = BoardTemplates.start()
                .sizeDefault()
                .districtsLondon()
                .stationsFullyFillRandom()
                .stationsRemovePercent(0.47f, Map.of(BoardTemplates.LooseDistrictDefinition.CENTERMOST, 9), 4)
                .stationsPickStartingLocations()
                .stationRedistributeTypes(50)
                .monumentPickRandomStationPerDistrict(2, 0.4f)
                .pickJokerStations(List.of(BoardTemplates.LooseDistrictDefinition.CENTERMOST))
                .build();

        board.finishSetup();
        board.writeSerialized(new File("target/createBoardTest"));

        BoardRendererTest.main(new String[]{});
        try {
            Thread.sleep(TimeUnit.MINUTES.toMillis(10));
        } catch (InterruptedException e) {
            log.error("Thread interrupted", e);
        }
    }

}