import java.awt.*;
import java.awt.geom.*;

public class ControlPoint extends Point {

    private int pointSize; // size of the point
    private Shape shape;

    public boolean isMovable; // variable to determine if the point is movable
    public boolean isActive; // variable to determine if it is active point
    public boolean isShown; // variable to determine if it is shown

    public boolean isAlsoActive;

    // constructor
    public ControlPoint(int givenX, int givenY) {
        super(givenX, givenY); // set location of point

        pointSize = 5; // initialize size
        isActive = false;
        isShown = false;
        isMovable = false;
        isAlsoActive = false;

        // create shape around the point
        shape = new Ellipse2D.Float(x - pointSize, y - pointSize, pointSize * 2, pointSize * 2);
    }

    public void setSize(int givenSize) {
        pointSize = givenSize; // set size of point

        // create new shape with new point size
        Point currPoint = super.getLocation();
        shape = new Ellipse2D.Float(currPoint.x, currPoint.y, pointSize * 2, pointSize * 2);
    }

    public void setLocation(Point givenPoint) {
        // set location to a given point & create a new shape at new location
        setLocation(givenPoint.x, givenPoint.y);
        shape = new Ellipse2D.Float(givenPoint.x - pointSize, givenPoint.y - pointSize, pointSize * 2, pointSize * 2);
    }

    // return if the shape contains a given  point
    public boolean contains(Point givenPoint) {
        return shape.contains(givenPoint);
    }
}
