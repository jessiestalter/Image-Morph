import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.imageio.*;

public class JMorph extends JFrame implements ActionListener, ChangeListener {

    // set up grids
    private Grid grid1, grid2;

    // set up panels
    private JPanel leftPanel, middlePanel, rightPanel, buttonPanel, inputPanel;
    private JPanel gridPanel, bottomPanel;

    // set up menu items
    private JFileChooser fc;
    private JMenuBar menuBar;
    private JMenu fileMenu, helpMenu;
    private JMenuItem fileOpenLeft, fileOpenRight;
    private JMenuItem helpMenuItem, exit;

    // set up buttons
    private JButton previewButton, morphButton, resetButton;

    // set up sliders
    private JSlider leftBrightSlider, rightBrightSlider;
    private JSlider pointsSlider;

    // set up labels
    private JLabel leftBrightLabel, rightBrightLabel;
    private JLabel pointsLabel;
    private JLabel secondsLabel, FPSLabel;

    // set up text fields
    private JTextField inputSeconds, inputFPS;

    // constructor
    public JMorph() {
        super("JMorph"); // add title

        // create grids
        grid1 = new Grid(5); // left grid
        grid2 = new Grid(5); // right grid

        // setup panels
        leftPanel = new JPanel();
        middlePanel = new JPanel();
        rightPanel = new JPanel();
        gridPanel = new JPanel();
        buttonPanel = new JPanel();
        bottomPanel = new JPanel();
        inputPanel = new JPanel();

        // setup buttons
        previewButton = new JButton("Preview Morph");
        previewButton.addActionListener(this);
        morphButton = new JButton("Render Morph");
        morphButton.addActionListener(this);
        resetButton = new JButton("Reset Morph");
        resetButton.addActionListener(this);

        // setup sliders
        leftBrightSlider = new JSlider(-100, 100);
        leftBrightSlider.addChangeListener(this);
        leftBrightSlider.setPaintTicks(true);
        leftBrightSlider.setPaintLabels(true);
        leftBrightSlider.setMajorTickSpacing(50);
        pointsSlider = new JSlider(5, 20,5);
        pointsSlider.addChangeListener(this);
        rightBrightSlider = new JSlider(-100, 100);
        rightBrightSlider.addChangeListener(this);
        rightBrightSlider.setPaintTicks(true);
        rightBrightSlider.setPaintLabels(true);
        rightBrightSlider.setMajorTickSpacing(50);

        // setup labels
        leftBrightLabel = new JLabel("Left Brightness: +0", SwingConstants.CENTER);
        pointsLabel = new JLabel("Grid Resolution: 5 x 5", SwingConstants.CENTER);
        rightBrightLabel = new JLabel("Right Brightness: +0", SwingConstants.CENTER);
        secondsLabel = new JLabel("Number of seconds: ", SwingConstants.RIGHT);
        FPSLabel = new JLabel("Number of frames per second: ", SwingConstants.RIGHT);

        // setup text fields
        inputSeconds = new JTextField();
        inputFPS = new JTextField();

        // setup menu items
        fc = new JFileChooser(".");
        menuBar = new JMenuBar();
        this.setJMenuBar(menuBar);
        fileMenu = new JMenu("File");
        helpMenu = new JMenu("Help");
        fileOpenLeft = new JMenuItem("Set Left Image");
        fileOpenRight = new JMenuItem("Set Right Image");
        helpMenuItem = new JMenuItem("Help");
        exit = new JMenuItem("Exit");

        // add actionListeners for menu items
        fileOpenLeft.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int returnVal = fc.showOpenDialog(JMorph.this);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();
                    try {
                        grid1.setImage(ImageIO.read(file));
                    } catch (IOException e1){};

                    // scale image if necessary
                    int w, h;
                    if (grid2.hasImage) {
                        w = grid2.getImage().getWidth();
                        h = grid2.getImage().getHeight();

                        BufferedImage newImage = new BufferedImage(w, h, grid1.getImage().getType());
                        Graphics2D g = newImage.createGraphics();
                        g.drawImage(grid1.getImage(), 0, 0, w, h, null);
                        g.dispose();
                        grid1.setImage(newImage);
                    }
                }
                leftBrightSlider.setValue(0); // reset brightness slider
            }
        });
        fileOpenRight.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int returnVal = fc.showOpenDialog(JMorph.this);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();
                    try {
                        grid2.setImage(ImageIO.read(file));
                    } catch (IOException e1){};

                    // scale image if necessary
                    int w, h;
                    if (grid1.hasImage) {
                        w = grid1.getImage().getWidth();
                        h = grid1.getImage().getHeight();

                        BufferedImage newImage = new BufferedImage(w, h, grid2.getImage().getType());
                        Graphics2D g = newImage.createGraphics();
                        g.drawImage(grid2.getImage(), 0, 0, w, h, null);
                        g.dispose();
                        grid2.setImage(newImage);
                    }
                }
                rightBrightSlider.setValue(0); // reset brightness slider
            }
        });
        exit.addActionListener(new ActionListener() {
            public void actionPerformed (ActionEvent e) {
                System.exit(0);
            }
        });
        helpMenuItem.addActionListener (new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // creates & displays a help menu
                JFrame helpFrame = new JFrame("Help Menu");
                JTextArea helpText = new JTextArea();
                helpText.setText("\n      Set the left & right images & move the control points to get started & begin the morph!");
                helpText.setEditable(false);
                helpFrame.add(helpText);
                helpFrame.setLocationRelativeTo(null);
                helpFrame.setSize(new Dimension(500,80));
                helpFrame.setVisible(true);
            }
        });

        // add to menu bar
        fileMenu.add(fileOpenLeft);
        fileMenu.add(fileOpenRight);
        helpMenu.add(helpMenuItem);
        helpMenu.add(exit);
        menuBar.add(fileMenu);
        menuBar.add(helpMenu);

        // add items to panels
        gridPanel.setLayout(new GridLayout(1,2));
        gridPanel.add(grid1);
        gridPanel.add(grid2);

        inputPanel.setLayout(new GridLayout(2,2));
        inputPanel.add(secondsLabel);
        inputPanel.add(inputSeconds);
        inputPanel.add(FPSLabel);
        inputPanel.add(inputFPS);

        buttonPanel.add(previewButton);
        buttonPanel.add(morphButton);
        buttonPanel.add(resetButton);

        leftPanel.setLayout(new GridLayout(2,1));
        leftPanel.add(leftBrightLabel);
        leftPanel.add(leftBrightSlider);

        middlePanel.setLayout(new GridLayout(4,1));
        middlePanel.add(pointsLabel);
        middlePanel.add(pointsSlider);
        middlePanel.add(buttonPanel);
        middlePanel.add(inputPanel);

        rightPanel.setLayout(new GridLayout(2, 1));
        rightPanel.add(rightBrightLabel);
        rightPanel.add(rightBrightSlider);

        bottomPanel.setLayout(new GridLayout(1,3));
        bottomPanel.add(leftPanel);
        bottomPanel.add(middlePanel);
        bottomPanel.add(rightPanel);

        Container c = getContentPane();
        c.setLayout(new BorderLayout());
        c.add(gridPanel, BorderLayout.CENTER);
        c.add(bottomPanel, BorderLayout.SOUTH);

        setSize(new Dimension(1100, 650));
        setLocationRelativeTo(null);
        setVisible(true);

        timer.start(); // start timer that allows for correspondence between the two grids
    }

    public static void main(String[] args) {
        JMorph J = new JMorph();

        J.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
    }

    // function to determine if a string is an integer
    public boolean isInteger(String givenString) {
        if (givenString == null)
            return false;
        try {
            int i = Integer.parseInt(givenString);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource().equals(previewButton)) {
            // start morph preview!!
            int fps = 30;

            if (isInteger(inputFPS.getText()))
                fps = Integer.parseInt(inputFPS.getText());

            int seconds = 2;
            if (isInteger(inputSeconds.getText()))
                seconds = Integer.parseInt(inputSeconds.getText());

            TransitionFrame transitionFrame = new TransitionFrame(grid1, grid2, seconds, fps);
        }

        if (e.getSource().equals(morphButton)) {
            // start morph!!
            int fps = 30;

            if (isInteger(inputFPS.getText()))
                fps = Integer.parseInt(inputFPS.getText());

            int seconds = 2;
            if (isInteger(inputSeconds.getText()))
                seconds = Integer.parseInt(inputSeconds.getText());

            MorphFrame morphFrame = new MorphFrame(grid1, grid2, seconds, fps);
        }
        if (e.getSource().equals(resetButton)) {
            leftBrightSlider.setValue(0);
            leftBrightLabel.setText("Left Brightness: +0");
            rightBrightSlider.setValue(0);
            rightBrightLabel.setText("Right Brightness: +0");
            grid1.resetGrid();
            grid2.resetGrid();
        }
    }

    public void stateChanged(ChangeEvent e) {
        if (e.getSource().equals(leftBrightSlider)) {
            if (leftBrightSlider.getValue() >= 0)
                leftBrightLabel.setText("Left Brightness: +" + leftBrightSlider.getValue());
            else
                leftBrightLabel.setText("Left Brightness: " + leftBrightSlider.getValue());

            if (grid1.hasImage) {
                grid1.adjustBrightness(leftBrightSlider.getValue());
                grid1.repaint();
            }
        }

        if (e.getSource().equals(pointsSlider)) {
            pointsLabel.setText("Grid Resolution: " + pointsSlider.getValue() + " x " + pointsSlider.getValue());
            grid1.setNumPoints(pointsSlider.getValue());
            grid2.setNumPoints(pointsSlider.getValue());
            grid1.resetGrid();
            grid2.resetGrid();
        }

        if (e.getSource().equals(rightBrightSlider)) {
            if (rightBrightSlider.getValue() >= 0)
                rightBrightLabel.setText("Right Brightness: +" + rightBrightSlider.getValue());
            else
                rightBrightLabel.setText("Right Brightness: " + rightBrightSlider.getValue());

            if (grid2.hasImage) {
                grid2.adjustBrightness(rightBrightSlider.getValue());
                grid2.repaint();
            }
        }
    }

    // implement timer
    ActionListener taskPerformer = new ActionListener() {
        public void actionPerformed(ActionEvent t) {
            for (int y = 0; y < grid1.getNumPoints(); y++) {
                for (int x = 0; x < grid1.getNumPoints(); x++) {

                    if (grid1.points[x][y].isActive)
                        grid2.points[x][y].isAlsoActive = true;
                    else if (grid2.points[x][y].isActive)
                        grid1.points[x][y].isAlsoActive = true;
                    else {
                        grid1.points[x][y].isActive = false;
                        grid1.points[x][y].isAlsoActive = false;
                        grid2.points[x][y].isActive = false;
                        grid2.points[x][y].isAlsoActive = false;
                    }

                    grid1.repaint();
                    grid2.repaint();
                }
            }
        }
    };
    Timer timer = new Timer(10, taskPerformer);
}
