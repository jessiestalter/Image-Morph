import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.*;
import javax.swing.*;

public class Grid extends JLabel {

    private int numPoints; // define the control point resolution
    private int radius; // size of control points

    private boolean isDragging, isMultiplePointDragging;
    private boolean isSelecting;
    private boolean hasMovedMultiplePoints;

    private ControlPoint activePoint;

    private Vector<ControlPoint> activePoints;

    private Point selectionStart;
    private Point multiplePointDragStart;

    // 2D array of control points
    public ControlPoint[][] points;
    // array of control triangles
    public ControlTriangle[] triangles;

    private BufferedImage Image;
    private BufferedImage alteredImage;
    private boolean isAltered;
    public boolean hasImage;
    private float brightnessValue;

    private int[] ActiveXPoints;
    private int[] ActiveYPoints;

    // constructor
    public Grid(int givenNumPoints) {
        numPoints = givenNumPoints + 2; // there are 2 more control points in height and width direction

        // initialize variables
        isDragging = false;
        isMultiplePointDragging = false;
        hasMovedMultiplePoints = false;
        isSelecting = false;
        activePoint = null;
        selectionStart = null;
        multiplePointDragStart = null;
        radius = 5;
        isAltered = false;
        hasImage = false;

        activePoints = new Vector<>();

        setSize(525, 425);

        generatePoints();
        generateTriangles();

        this.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                Point clickPoint = e.getPoint();

                if (activePoints.size() > 1) { // multiple points have been selected
                    isMultiplePointDragging = true;
                    hasMovedMultiplePoints = true;

                    // get current active points' locations
                    ActiveXPoints = getActivePointsX();
                    ActiveYPoints = getActivePointsY();

                    for (int i = 0; i < activePoints.size(); i++) {
                        if (activePoints.get(i).contains(clickPoint)) {
                            multiplePointDragStart = clickPoint;
                        }
                    }
                }

                // multiple points have not been selected
                if (activePoints.size() < 1) {
                    // get new active point & set it to be active
                    for (int y = 0; y < numPoints; y++) {
                        for (int x = 0; x < numPoints; x++) {
                            if (points[x][y].contains(clickPoint)) {
                                isDragging = true;

                                activePoint = points[x][y];
                                activePoint.isActive = true;

                                repaint();
                            } else { // if they didn't select a point, they must be selecting multiple points
                                selectionStart = e.getPoint();
                                isSelecting = true;
                            }
                        }
                    }
                }
            }

            public void mouseReleased(MouseEvent e) {
                isDragging = false;
                isSelecting = false;
                isMultiplePointDragging = false;

                if (hasMovedMultiplePoints) {
                    // get rid of multiple active points
                    for (int i = 0; i < activePoints.size(); i++) {
                        activePoints.get(i).isActive = false;
                    }
                    activePoints.clear();
                    hasMovedMultiplePoints = false;
                }

                if (activePoint != null) {
                    activePoint.isActive = false; // get rid of active point
                    activePoint = null;
                }
            }
        });

        this.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                if (isMultiplePointDragging) {
                    Point currentPoint = e.getPoint();

                    // get previous point locations
                    Point[] previousPoints = new Point[activePoints.size()];
                    for (int i = 0; i < activePoints.size(); i++) {
                        previousPoints[i] = new Point(activePoints.get(i).x, activePoints.get(i).y);
                    }

                    // keep point within bounds
                    if (currentPoint.x < 0)
                        currentPoint.x = 0;
                    if (currentPoint.y < 0)
                        currentPoint.y = 0;
                    if (currentPoint.x > getWidth())
                        currentPoint.x = getWidth();
                    if (currentPoint.y > getHeight())
                        currentPoint.y = getHeight();

                    int xDiff = currentPoint.x - multiplePointDragStart.x;
                    int yDiff = currentPoint.y - multiplePointDragStart.y;

                    for (int i = 0; i < activePoints.size(); i++) {
                        Point newPoint = new Point(ActiveXPoints[i] + xDiff, ActiveYPoints[i] + yDiff);
                        activePoints.get(i).setLocation(newPoint);
                    }

                    for (int i = 0; i < activePoints.size(); i++) {
                        if (!isContained(activePoints.get(i)))
                            activePoints.get(i).setLocation(previousPoints[i]);
                    }

                    generateTriangles();
                    repaint();
                }
                else if (isDragging) {
                    if (activePoint != null && activePoint.isMovable) {
                        Point currentPoint = e.getPoint();

                        Point previousPoint = new Point(activePoint.x, activePoint.y);

                        // keep point within bounds
                        if (currentPoint.x < 0)
                            currentPoint.x = 0;
                        if (currentPoint.y < 0)
                            currentPoint.x = 0;
                        if (currentPoint.x > getWidth())
                            currentPoint.x = getWidth();
                        if (currentPoint.y > getHeight())
                            currentPoint.y = getHeight();

                        activePoint.setLocation(currentPoint);
                        if(!isContained(currentPoint)) {
                            activePoint.setLocation(previousPoint);
                        }

                        generateTriangles();
                        repaint();
                    }
                }

                else if (isSelecting) {
                    Point currentPoint = e.getPoint();
                    for (int y = 0; y < numPoints; y++) {
                        for (int x = 0; x < numPoints; x++) {
                            if ((points[x][y].x <= selectionStart.x && points[x][y].x >= currentPoint.x) || (points[x][y].x >= selectionStart.x && points[x][y].x <= currentPoint.x)) {
                                if ((points[x][y].y <= selectionStart.y && points[x][y].y >= currentPoint.y) || (points[x][y].y >= selectionStart.y && points[x][y].y <= currentPoint.y)) {
                                    points[x][y].isActive = true;
                                    activePoints.add(points[x][y]);
                                }
                            }
                        }
                    }
                    repaint();
                }
            }
        });
    }

    // another constructor, this time using a previously defined grid
    public Grid (Grid copyGrid) {
        this(copyGrid.numPoints - 2);
        points = copyGrid.copyPoints();
        triangles = copyGrid.copyTriangles();
    }

    public void resetGrid() {
        if (hasImage)
            setSize(Image.getWidth(), Image.getHeight());
        else
            setSize(525, 425);
        generatePoints();
        generateTriangles();
    }

    public void setNumPoints(int givenNumPoints) {
        numPoints = givenNumPoints + 2;
    }

    public int getNumPoints() {
        return numPoints;
    }

    public int[] getActivePointsX() {
        int[] array = new int[activePoints.size()];
        for (int i = 0; i < activePoints.size(); i++) {
            array[i] = activePoints.get(i).x;
        }
        return array;
    }

    public int[] getActivePointsY() {
        int[] array = new int[activePoints.size()];
        for (int i = 0; i < activePoints.size(); i++) {
            array[i] = activePoints.get(i).y;
        }
        return array;
    }

    public boolean isContained(Point currentPoint) {

        for (int i = 0; i < triangles.length; i++) {
            if (currentPoint.x == triangles[i].getC1().x && currentPoint.y == triangles[i].getC1().y) {}

            else if (currentPoint.x == triangles[i].getC2().x && currentPoint.y == triangles[i].getC2().y) {}

            else if (currentPoint.x == triangles[i].getC3().x && currentPoint.y == triangles[i].getC3().y) {}

            else {
                if (triangles[i].triangle.contains(currentPoint))
                    return false;
            }
        }
        return true;
    }

    public void generatePoints() {
        points = new ControlPoint[numPoints][numPoints];

        int separationW = getWidth() / (numPoints - 1);
        int separationH = getHeight() / (numPoints - 1);

        for (int y = 0; y < numPoints; y++) {
            for (int x = 0; x < numPoints; x++) {
                points[x][y] = new ControlPoint(separationW * x, separationH * y);
                // make control points on the edges not movable, others movable
                if (x == 0 || y == 0 || x == numPoints - 1 || y == numPoints - 1) {
                    points[x][y].isMovable = false;
                }
                else {
                    points[x][y].isMovable = true;
                    points[x][y].isShown = true;
                }
            }
        }
        repaint();
    }

    public void generateTriangles() {
        triangles = new ControlTriangle[((numPoints - 1) * (numPoints - 1)) * 2];

        int index = 0;
        for (int y = 0; y < numPoints - 1; y++) {
            for (int x = 0; x < numPoints - 1; x ++) {
                triangles[index] = new ControlTriangle(points[x][y], points[x+1][y], points[x+1][y+1]);
                index++;

                triangles[index] = new ControlTriangle(points[x][y], points[x][y+1], points[x+1][y+1]);
                index++;
            }
        }
        repaint();
    }

    public ControlPoint[][] copyPoints() {
        ControlPoint[][] copyPoints = new ControlPoint[numPoints][numPoints];

        for (int y = 0; y < numPoints; y++) {
            for (int x = 0; x < numPoints; x++) {
                ControlPoint copyPoint = points[x][y];
                copyPoints[x][y] = new ControlPoint(copyPoint.x, copyPoint.y);
                copyPoints[x][y].isMovable = copyPoint.isMovable;
                copyPoints[x][y].isShown = copyPoint.isShown;
            }
        }

        return copyPoints;
    }

    public ControlTriangle[] copyTriangles() {
        ControlTriangle[] copyTriangles = new ControlTriangle[((numPoints - 1) * (numPoints - 1)) * 2];

        for (int i = 0; i < copyTriangles.length; i++) {
            ControlTriangle copyTriangle = triangles[i];
            copyTriangles[i] = new ControlTriangle(copyTriangle.getC1(), copyTriangle.getC2(), copyTriangle.getC3());
        }

        return copyTriangles;
    }

    // method to change if the grid's points are able to be moved or not
    public void setGridMobility(boolean moveStatus) {
        for (int y = 1; y < numPoints - 1; y++) {
            for (int x = 1; x < numPoints - 1; x++) {
                points[x][y].isMovable = moveStatus;
            }
        }
    }

    public void setRadius(int givenRadius) {
        radius = givenRadius;
        for (int y = 0; y < numPoints; y++) {
            for (int x = 0; x < numPoints; x++) {
                points[x][y].setSize(givenRadius);
            }
        }
        repaint();
    }

    // method to set the image for the grid
    public void setImage(BufferedImage image) {
        Image = image;
        hasImage = true;
        isAltered = false;

        setSize(image.getWidth(), image.getHeight());

        generatePoints();
        generateTriangles();
    }

    public BufferedImage getImage() {
        if (isAltered)
            return alteredImage;
        else
            return Image;
    }

    // method to change the brightness of image (using values from the slider)
    public void adjustBrightness(float value) {
        isAltered = true;
        brightnessValue = (value / 100) + 1;
        RescaleOp brightnessOp = new RescaleOp(brightnessValue, 0, null);
        alteredImage = brightnessOp.filter(Image, null);
    }

    // paint method
    public void paint(Graphics g) {
        // show image
        if (hasImage) {
            Graphics2D big = (Graphics2D) g;
            if (isAltered)
                big.drawImage(alteredImage, 0, 0, this);
            else
                big.drawImage(Image, 0, 0, this);
        }

        // draw lines
        for (int i = 0; i < triangles.length; i++) {
            g.setColor(Color.BLACK);
            g.drawLine(triangles[i].getC1().x,triangles[i].getC1().y, triangles[i].getC2().x, triangles[i].getC2().y);
            g.drawLine(triangles[i].getC1().x,triangles[i].getC1().y, triangles[i].getC3().x, triangles[i].getC3().y);
            g.drawLine(triangles[i].getC3().x,triangles[i].getC3().y, triangles[i].getC2().x, triangles[i].getC2().y);
        }

        // draw points
        for (int y = 0; y < numPoints; y++) {
            for (int x = 0; x < numPoints; x++) {
                if (points[x][y].isShown) {
                    g.setColor(Color.BLACK);
                    g.fillOval(points[x][y].x - radius, points[x][y].y - radius, radius * 2, radius * 2);

                    if (points[x][y].isActive || points[x][y].isAlsoActive) {
                        g.setColor(Color.RED);
                        g.fillOval(points[x][y].x - radius, points[x][y].y - radius, radius * 2, radius * 2);
                    }
                }
            }
        }
    }
}
