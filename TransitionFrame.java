import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class TransitionFrame extends JFrame {

    private Grid grid1, grid2;
    private Grid transitionGrid;

    private int numSeconds;
    private int fps;

    private int currentFrame;
    private int totalFrames;
    private int msPerFrame;

    // constructor
    public TransitionFrame(Grid givenGrid1, Grid givenGrid2, int givenSeconds, int givenfps) {
        grid1 = givenGrid1;
        grid2 = givenGrid2;
        numSeconds = givenSeconds;
        fps = givenfps;

        currentFrame = 1;
        totalFrames = numSeconds * fps;
        msPerFrame = 1000 / fps;

        transitionGrid = new Grid(grid1); // creates a new grid, same as grid1
        transitionGrid.setGridMobility(false); // makes points not able to move

        JButton transitionButton = new JButton("Preview Morph");
        transitionButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                timer.start();
            }
        });

        add(transitionGrid, BorderLayout.CENTER);
        add(transitionButton, BorderLayout.SOUTH);

        setSize(transitionGrid.getWidth() + 15, transitionGrid.getHeight() + 75);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // implement timer
    ActionListener taskPerformer = new ActionListener() {
        public void actionPerformed(ActionEvent t) {
            if (currentFrame <= totalFrames) {

                int numPoints = transitionGrid.getNumPoints(); // find number of control points

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
                        transitionGrid.points[x][y].x = grid1.points[x][y].x + xdifference;
                        transitionGrid.points[x][y].y = grid1.points[x][y].y + ydifference;
                    }
                }

                currentFrame++; // increment frame number

                transitionGrid.generateTriangles(); // generate new triangles with the new points
                transitionGrid.repaint();
            }
        }
    };
    Timer timer = new Timer(msPerFrame, taskPerformer);
}
