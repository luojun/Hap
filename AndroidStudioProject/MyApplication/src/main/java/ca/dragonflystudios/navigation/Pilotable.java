package ca.dragonflystudios.navigation;

/**
 *
 * User's egocentric state and possible actions. The metaphor here is an airplane. What is pilotable is the
 * airplane. This corresponds to the localization part.
 *
 */

public class Pilotable
{
    public enum Maneuver
    {
        FORWARD, BACKWARD, LEFT, RIGHT, UP, DOWN;
    }
}
