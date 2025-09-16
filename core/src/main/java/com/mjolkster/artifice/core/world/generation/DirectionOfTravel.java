package com.mjolkster.artifice.core.world.generation;

public enum DirectionOfTravel {
    NORTH(0, 1),
    EAST(1, 0),
    SOUTH(0, -1),
    WEST(-1, 0),
    NORTH_EAST(1, 1),
    NORTH_WEST(-1, 1),
    SOUTH_EAST(1, -1),
    SOUTH_WEST(-1, -1);

    final int dx, dy;

    DirectionOfTravel(int dx, int dy) {
        this.dx = dx;
        this.dy = dy;
    }

}
