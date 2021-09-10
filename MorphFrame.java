import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class MorphFrame extends JFrame {

    private MorphPanel morphPanel;
    private JPanel buttonPanel;

    private Grid grid1, grid2;

    private int numSeconds;
    private int fps;

    private int msPerFrame;

    private boolean hasRendered;

    public MorphFrame(Grid givenGrid1, Grid givenGrid2, int givenSeconds, int givenfps) {
        grid1 = givenGrid1;
        grid2 = givenGrid2;
        numSeconds = givenSeconds;
        fps = givenfps;

        hasRendered = false;

        morphPanel = new MorphPanel(grid1, grid2, numSeconds, fps);
        buttonPanel = new JPanel();

        msPerFrame = 1000 / fps;

        JButton transitionButton = new JButton("View Morph");
        transitionButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                hasRendered = true;
                morphPanel.showWarp = true;
                morphPanel.warp2();
                timer.start();
            }
        });

        JButton renderButton = new JButton("Render to Files");
        renderButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (hasRendered)
                    morphPanel.writeImages();
            }
        });

        buttonPanel.setLayout(new GridLayout(1,2));
        buttonPanel.add(transitionButton);
        buttonPanel.add(renderButton);
        add(morphPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // sets the size to be big enough for width/height of either image
        setSize(Math.max(grid1.getImage().getWidth(), grid2.getImage().getWidth()) + 15, Math.max(grid1.getImage().getHeight(), grid2.getImage().getHeight()) + 75);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // implement timer
    ActionListener taskPerformer = new ActionListener() {
        public void actionPerformed(ActionEvent t) {
            if (morphPanel.currentFrame <= morphPanel.totalFrames) {
                morphPanel.alterLeftTransitionGrid();

                morphPanel.warp1();
                morphPanel.currentRightImage = morphPanel.rightImages[morphPanel.totalFrames - morphPanel.currentFrame];

                morphPanel.renderTweenImages();

                morphPanel.changeRightAlpha();

                morphPanel.currentFrame++;

                morphPanel.repaint();
            }
        }
    };
    Timer timer = new Timer(msPerFrame, taskPerformer);
}
