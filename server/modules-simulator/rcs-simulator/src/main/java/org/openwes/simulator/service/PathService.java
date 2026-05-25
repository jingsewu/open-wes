package org.openwes.simulator.service;

import org.openwes.simulator.domain.Position;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PathService {

    private static final double REACH_THRESHOLD = 0.15;

    public List<Position> calculatePath(Position from, Position to) {
        List<Position> path = new ArrayList<>();
        path.add(from.copy());

        double currentX = from.getX();
        double currentY = from.getY();
        double targetX = to.getX();
        double targetY = to.getY();

        double stepX = currentX < targetX ? 1.0 : -1.0;
        while (Math.abs(currentX - targetX) > 0.01) {
            double moveX = Math.min(Math.abs(targetX - currentX), 1.0);
            currentX += stepX * moveX;
            double rotation = stepX > 0 ? 0 : 180;
            path.add(new Position(currentX, currentY, rotation));
        }

        double stepY = currentY < targetY ? 1.0 : -1.0;
        while (Math.abs(currentY - targetY) > 0.01) {
            double moveY = Math.min(Math.abs(targetY - currentY), 1.0);
            currentY += stepY * moveY;
            double rotation = stepY > 0 ? 90 : 270;
            path.add(new Position(currentX, currentY, rotation));
        }

        return path;
    }

    public Position moveAlongPath(Position current, List<Position> path, double distance) {
        int currentIdx = findClosestWaypointIndex(current, path);

        double remaining = distance;
        Position pos = current.copy();

        for (int i = currentIdx; i < path.size() && remaining > 0.01; i++) {
            Position waypoint = path.get(i);
            double segmentDist = Math.abs(pos.getX() - waypoint.getX()) + Math.abs(pos.getY() - waypoint.getY());

            if (segmentDist <= remaining) {
                pos = waypoint.copy();
                remaining -= segmentDist;
            } else {
                double ratio = remaining / segmentDist;
                double newX = pos.getX() + (waypoint.getX() - pos.getX()) * ratio;
                double newY = pos.getY() + (waypoint.getY() - pos.getY()) * ratio;
                pos = new Position(newX, newY, waypoint.getRotation());
                remaining = 0;
            }
        }

        return pos;
    }

    public boolean hasReached(Position current, Position target) {
        double dist = Math.abs(current.getX() - target.getX()) + Math.abs(current.getY() - target.getY());
        return dist < REACH_THRESHOLD;
    }

    private int findClosestWaypointIndex(Position current, List<Position> path) {
        int bestIdx = 0;
        double bestDist = Double.MAX_VALUE;
        for (int i = 0; i < path.size(); i++) {
            double dist = current.distanceTo(path.get(i));
            if (dist < bestDist) {
                bestDist = dist;
                bestIdx = i;
            }
        }
        return Math.min(bestIdx + 1, path.size() - 1);
    }
}
