package daniel.stadtgeschichten.model;

import java.util.Arrays;

/**
 * This class represents a circle.
 */
public class Circle
{
    /**
     * Circle radius in meters
     */
    private int radius;

    /**
     * Title of the circle; null or empty string if not specified
     */
    private String title;

    /**
     * Statements to execute in this circle
     */
    private AbsStatement[] statements;

    /**
     * Spot used as center
     */
    private Spot spot;

    public Circle(int radius, String title, AbsStatement[] statements)
    {
        this.radius = radius;
        this.title = title;
        this.statements = statements;
    }

    /**
     * @return {@link Circle#radius}
     */
    public int getRadius()
    {
        return radius;
    }

    /**
     * @return {@link Circle#title}
     */
    public String getTitle()
    {
        return title;
    }

    /**
     * @return {@link Circle#statements}
     */
    public AbsStatement[] getStatements()
    {
        return statements;
    }

    /**
     * @return {@link Circle#spot}
     */
    public Spot getSpot()
    {
        return spot;
    }

    public void setSpot(Spot spot)
    {
        this.spot = spot;
    }

    @Override
    public String toString()
    {
        return "Circle{" +
                "radius=" + radius +
                ", title='" + title + '\'' +
                ", statements=" + Arrays.toString(statements) +
                ", spot=" + spot +
                '}';
    }
}
