package de.yanwittmann.nextstation.setup;

import de.yanwittmann.nextstation.model.board.Station;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Slf4j
public class BoardOptimizer {

    public static BoardTemplates runIterations(Supplier<BoardTemplates> boardGenerator, Function<BoardTemplates, Integer> boardScorer, boolean maximize, int maxIterations) {
        BoardTemplates bestBoard = null;
        int bestScore = maximize ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        int totalTimeGenerating = 0;
        for (int i = 0; i < maxIterations; i++) {
            final long start = System.currentTimeMillis();
            final BoardTemplates board = boardGenerator.get();
            totalTimeGenerating += (int) (System.currentTimeMillis() - start);
            final int score = boardScorer.apply(board);
            final boolean isBetter = maximize ? score > bestScore : score < bestScore;
            if (isBetter) {
                bestScore = score;
                bestBoard = board;
                log.info("[{}/{}] [{}ms total / ~{}ms avg] new best: {}",
                        String.format("%3d", i + 1), maxIterations, totalTimeGenerating, totalTimeGenerating / (i + 1), score);
            }
        }
        return bestBoard;
    }

    public final static Function<BoardTemplates, Integer> STATION_SPREAD = board -> {
        // 3 closest stations for each station, sum of distances
        AtomicInteger score = new AtomicInteger();
        board.getBoard().getStations().forEach(station -> {
            List<Station> closestStations = board.getBoard().getStations().stream()
                    .filter(otherStation -> !otherStation.equals(station))
                    .sorted(Comparator.comparingInt(otherStation -> (int) Math.pow(otherStation.getX() - station.getX(), 2) + (int) Math.pow(otherStation.getY() - station.getY(), 2)))
                    .limit(3)
                    .collect(Collectors.toList());
            score.addAndGet(closestStations.stream()
                    .mapToInt(otherStation -> (int) Math.pow(otherStation.getX() - station.getX(), 2) + (int) Math.pow(otherStation.getY() - station.getY(), 2))
                    .sum());
        });
        return score.get();
    };
}
