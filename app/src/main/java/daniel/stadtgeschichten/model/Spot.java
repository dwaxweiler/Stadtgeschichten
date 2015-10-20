package daniel.stadtgeschichten.model;

import java.util.Arrays;
import java.util.Comparator;

/**
 * This class represents a spot.
 */
public class Spot
{
    /**
     * Latitude of the spot
     */
    private double latitude;

    /**
     * Longitude of the spot
     */
    private double longitude;

    /**
     * Circles having the coordinates of this spot as center, ordered by the radius
     */
    private Circle[] circles;

    public Spot(double latitude, double longitude, Circle[] circles)
    {
        this.latitude = latitude;
        this.longitude = longitude;
        this.circles = circles;

        // Sort circles by radius.
        Arrays.sort(this.circles, new Comparator<Circle>()
        {
            @Override
            public int compare(Circle lhs, Circle rhs)
            {
                if (lhs.getRadius() < rhs.getRadius())
                    return -1;
                else if (lhs.getRadius() == rhs.getRadius())
                    return 0;
                else
                    return 1;
            }
        });
    }

    /**
     * @return {@link Spot#latitude}
     */
    public double getLatitude()
    {
        return latitude;
    }

    /**
     * @return {@link Spot#longitude}
     */
    public double getLongitude()
    {
        return longitude;
    }

    /**
     * @return {@link Spot#circles}
     */
    public Circle[] getCircles()
    {
        return circles;
    }

    @Override
    public String toString()
    {
        return "Spot{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                ", circles=" + Arrays.toString(circles) +
                '}';
    }
}