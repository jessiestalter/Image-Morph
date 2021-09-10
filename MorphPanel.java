import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import javax.swing.*;
import Jama.*;

public class MorphPanel extends JPanel {

    private Grid grid1, grid2;
    private Grid leftTransitionGrid, rightTransitionGrid;

    private int numSeconds;
    private int fps;

    public int currentFrame, currentRightFrame;
    public int totalFrames;

    public BufferedImage currentLeftImage, currentRightImage;
    public boolean showWarp;

    private BufferedImage combinedImage;
    public BufferedImage[] rightImages;
    private BufferedImage[] tweenImages;

    private float rightAlpha;

    public MorphPanel(Grid givenGrid1, Grid givenGrid2, int givenSeconds, int givenfps) {
        grid1 = givenGrid1;
        grid2 = givenGrid2;
        numSeconds = givenSeconds;
        fps = givenfps;

        leftTransitionGrid = new Grid(grid1);
        rightTransitionGrid = new Grid(grid2);

        showWarp = false;

        rightAlpha = 0.0f;

        currentLeftImage = null;
        currentRightImage = null;

        currentRightFrame = 1;

        currentFrame = 1;
        totalFrames = numSeconds * fps;

        tweenImages = new BufferedImage[totalFrames];
    }

    // function for changing the right alpha value
    public void changeRightAlpha() {
        double percent = currentFrame / (double)totalFrames; // percent of morph completed

        rightAlpha = (float)((percent * 1.0));
    }

    // function to change the transition grid for the left image
    public void alterLeftTransitionGrid() {

        int numPoints = leftTransitionGrid.getNumPoints(); // find number of control points

        // create 2D arrays for difference in x & y values
        double xdiff[][] = new double[numPoints][numPoints];
        double ydiff[][] = new double[numPoints][numPoints];

        // find difference between grid2's points & grid1's points
        for (int y = 0; y < numPoints; y++) {
            for (int x = 0; x < numPoints; x++) {
                xdiff[x][y] = grid2.points[x][y].x - grid1.points[x][y].x;
                ydiff[x][y] = grid2.points[x][y].y - grid1.points[x][y].y;
            }
        }

        double percent = currentFrame / (double)totalFrames; // percent of morph completed
        for (int y = 0; y < numPoints; y++) {
            for (int x = 0; x < numPoints; x++) {
                int xdifference = (int)(percent * xdiff[x][y]);
                int ydifference = (int)(percent * ydiff[x][y]);
                leftTransitionGrid.points[x][y].x = grid1.points[x][y].x + xdifference;
                leftTransitionGrid.points[x][y].y = grid1.points[x][y].y + ydifference;
            }
        }

        leftTransitionGrid.generateTriangles(); // generate new triangles with the new points
    }

    // function to change the transition grid for the right image
    public void alterRightTransitionGrid() {
        int numPoints = rightTransitionGrid.getNumPoints(); // find number of control points

        // create 2D arrays for difference in x & y values
        double xdiff[][] = new double[numPoints][numPoints];
        double ydiff[][] = new double[numPoints][numPoints];

        // find difference between grid2's points & grid1's points
        for (int y = 0; y < numPoints; y++) {
            for (int x = 0; x < numPoints; x++) {
                xdiff[x][y] = grid1.points[x][y].x - grid2.points[x][y].x;
                ydiff[x][y] = grid1.points[x][y].y - grid2.points[x][y].y;
            }
        }

        double percent = currentRightFrame / (double)totalFrames; // percent of morph completed
        for (int y = 0; y < numPoints; y++) {
            for (int x = 0; x < numPoints; x++) {
                int xdifference = (int)(percent * xdiff[x][y]);
                int ydifference = (int)(percent * ydiff[x][y]);
                rightTransitionGrid.points[x][y].x = grid2.points[x][y].x + xdifference;
                rightTransitionGrid.points[x][y].y = grid2.points[x][y].y + ydifference;
            }
        }

        rightTransitionGrid.generateTriangles(); // generate new triangles with the new points
    }

    // function for one step of warping photo 1
    public void warp1() {
        currentLeftImage = new BufferedImage (leftTransitionGrid.getWidth(), leftTransitionGrid.getHeight(), BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < grid1.triangles.length; i++) {
            warpTriangle(grid1.getImage(), currentLeftImage, grid1.triangles[i], leftTransitionGrid.triangles[i],null,null,false);
        }
    }

    // function to put all of the "tween" images of the right image into an array
    public void warp2() {
        rightImages = new BufferedImage[totalFrames];

        BufferedImage rightImage;
        for (int i = 0; i < totalFrames; i++) {
            rightImage = new BufferedImage (rightTransitionGrid.getWidth(), rightTransitionGrid.getHeight(), BufferedImage.TYPE_INT_RGB);
            for (int j = 0; j < grid1.triangles.length; j++) {
                warpTriangle(grid2.getImage(), rightImage, grid2.triangles[j], rightTransitionGrid.triangles[j], null, null, false);
            }
            rightImages[i] = rightImage;

            alterRightTransitionGrid();
            currentRightFrame++;
        }
    }

    // function to render & output tween images as jpeg files
    public void renderTweenImages() {
        // create combined images
        int w = Math.max(currentLeftImage.getWidth(), currentRightImage.getWidth());
        int h = Math.max(currentLeftImage.getHeight(), currentRightImage.getHeight());
        combinedImage = (BufferedImage)createImage(w, h);
        paint(combinedImage.getGraphics());

        Graphics2D g2 = (Graphics2D)combinedImage.getGraphics();
        g2.drawImage(currentLeftImage, 0, 0, this);
        AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, rightAlpha);
        g2.setComposite(ac);
        g2.drawImage(currentRightImage, 0, 0, this);

        tweenImages[currentFrame - 1] = combinedImage;
    }

    // function to write images to output files that can be used with ffmpeg to create an mp4 file
    public void writeImages() {
        for (int i = 0; i < totalFrames; i++) {
            try {
                ImageIO.write(tweenImages[i], "jpeg", new File("combined" + (i + 1) + ".jpeg"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void warpTriangle (BufferedImage src, BufferedImage dest, ControlTriangle S, ControlTriangle D, Object ALIASING, Object INTERPOLATION, Boolean clearBackground) {

        if (ALIASING == null)
            ALIASING = RenderingHints.VALUE_ANTIALIAS_ON;
        if (INTERPOLATION == null)
            INTERPOLATION = RenderingHints.VALUE_INTERPOLATION_BICUBIC;

        double [][] Aarray = new double [3][3];
        double [][] BdestX = new double [3][1];
        double [][] BdestY = new double [3][1];

        // fill Aarray
        Aarray[0][0] = S.getC1().getX();
        Aarray[0][1] = S.getC1().getY();
        Aarray[0][2] = 1.0;
        Aarray[1][0] = S.getC2().getX();
        Aarray[1][1] = S.getC2().getY();
        Aarray[1][2] = 1.0;
        Aarray[2][0] = S.getC3().getX();
        Aarray[2][1] = S.getC3().getY();
        Aarray[2][2] = 1.0;

        // fill BdestX
        BdestX[0][0] = D.getC1().getX();
        BdestX[1][0] = D.getC2().getX();
        BdestX[2][0] = D.getC3().getX();

        // fill BdestY
        BdestY[0][0] = D.getC1().getY();
        BdestY[1][0] = D.getC2().getY();
        BdestY[2][0] = D.getC3().getY();

        Matrix A = new Matrix(Aarray);
        Matrix bx = new Matrix(BdestX);
        Matrix by = new Matrix(BdestY);

        Matrix affineRow1 = A.solve(bx);
        Matrix affineRow2 = A.solve(by);

        AffineTransform af = new
                AffineTransform(affineRow1.get(0,0), affineRow2.get(0,0),
                affineRow1.get(1,0), affineRow2.get(1,0),
                affineRow1.get(2,0), affineRow2.get(2,0));

        Graphics2D g2 = dest.createGraphics();

        // Set the aliasing and interpolation settings
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, ALIASING);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, INTERPOLATION);

        if (clearBackground) {
            g2.setColor(Color.BLACK);
            g2.fill(new Rectangle(0, 0, dest.getWidth(), dest.getHeight()));
        }

        GeneralPath destPath = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
        destPath.moveTo((float)D.getC1().getX(), (float)D.getC1().getY());
        destPath.lineTo((float)D.getC2().getX(), (float)D.getC2().getY());
        destPath.lineTo((float)D.getC3().getX(), (float)D.getC3().getY());
        destPath.lineTo((float)D.getC1().getX(), (float)D.getC1().getY());

        g2.clip(destPath);

        g2.setTransform(af);

        g2.drawImage(src, 0, 0, null);
        g2.dispose();
    }

    // paint method
    public void paint (Graphics g) {
        super.paintComponent(g);
        setBackground(Color.BLACK);

        Graphics2D big = (Graphics2D) g;

        if (showWarp) {
            big.drawImage(currentLeftImage, 0, 0, this);
            AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, rightAlpha);
            big.setComposite(ac);
            big.drawImage(currentRightImage, 0, 0, this);
        }
        else
            big.drawImage(grid1.getImage(), 0, 0, this);
    }
}
