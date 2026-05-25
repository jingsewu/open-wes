package org.openwes.simulator.service;

import org.junit.jupiter.api.Test;
import org.openwes.simulator.domain.Position;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PathServiceTest {

    private final PathService pathService = new PathService();

    @Test
    void calculatePath_samePosition_returnsSinglePoint() {
        Position pos = new Position(5, 5, 0);
        List<Position> path = pathService.calculatePath(pos, pos);
        assertEquals(1, path.size());
        assertEquals(5.0, path.get(0).getX());
    }

    @Test
    void calculatePath_horizontalMove_movesXFirst() {
        Position from = new Position(0, 0, 0);
        Position to = new Position(3, 0, 0);
        List<Position> path = pathService.calculatePath(from, to);

        assertTrue(path.size() > 1);
        assertEquals(0.0, path.get(0).getX());
        assertEquals(3.0, path.get(path.size() - 1).getX());
    }

    @Test
    void calculatePath_manhattanPath_movesXThenY() {
        Position from = new Position(0, 0, 0);
        Position to = new Position(3, 4, 0);
        List<Position> path = pathService.calculatePath(from, to);

        Position lastPoint = path.get(path.size() - 1);
        assertEquals(3.0, lastPoint.getX(), 0.01);
        assertEquals(4.0, lastPoint.getY(), 0.01);
    }

    @Test
    void moveAlongPath_calculatesNewPosition() {
        Position from = new Position(0, 0, 0);
        Position to = new Position(10, 0, 0);
        List<Position> path = pathService.calculatePath(from, to);

        Position newPos = pathService.moveAlongPath(from, path, 3.0);
        assertEquals(3.0, newPos.getX(), 0.01);
        assertEquals(0.0, newPos.getY(), 0.01);
    }

    @Test
    void moveAlongPath_reachesEnd_clampsToDestination() {
        Position from = new Position(0, 0, 0);
        Position to = new Position(2, 0, 0);
        List<Position> path = pathService.calculatePath(from, to);

        Position newPos = pathService.moveAlongPath(from, path, 100.0);
        assertEquals(2.0, newPos.getX(), 0.01);
    }

    @Test
    void hasReachedDestination_atTarget_returnsTrue() {
        Position current = new Position(5.0, 5.0, 0);
        Position target = new Position(5.0, 5.0, 0);
        assertTrue(pathService.hasReached(current, target));
    }

    @Test
    void hasReachedDestination_closeEnough_returnsTrue() {
        Position current = new Position(5.05, 4.95, 0);
        Position target = new Position(5.0, 5.0, 0);
        assertTrue(pathService.hasReached(current, target));
    }

    @Test
    void hasReachedDestination_farAway_returnsFalse() {
        Position current = new Position(0, 0, 0);
        Position target = new Position(5, 5, 0);
        assertFalse(pathService.hasReached(current, target));
    }
}
