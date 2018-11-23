import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.util.Random;

/**
 * Delegate class which contains the view and controller connected to the model.
 */
public class Delegate implements PropertyChangeListener {

    // Set the size of the frame
    private static final int FRAME_HEIGHT = 850;
    private static final int FRAME_WIDTH = 800;

    // Define model, buttons, panel etc
    private Model model;
    private JFrame mainFrame;
    private Panel panel;
    private JMenuBar menuBar;
    private JToolBar toolbar;
    private JButton drawBtn, undoBtn, redoBtn, changeIterationsBtn, changeColor;
    private JCheckBox toggleModeBtn, toggleRatio, toggleColor;


    /**
     * Constructor, saves the model passed to it, creates a new frame, menubar and toolbar and panel.
     * Does general setting up.
     *
     * @param model Model to connect to and display data from.
     */
    public Delegate(Model model) {
        this.model = model;
        this.mainFrame = new JFrame();
        menuBar = new JMenuBar();
        toolbar = new JToolBar();

        panel = new Panel();
        mainFrame.add(panel);

        setupToolbar();
        setupMenu();

        mainFrame.setSize(FRAME_WIDTH, FRAME_HEIGHT);
        mainFrame.setVisible(true);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        model.addObserver(this);
    }

    /**
     * Sets up all the objects within the toolbar and adds listeners to them.
     */
    private void setupToolbar() {

        // Reset button, resets the model to default when pressed
        drawBtn = new JButton("Reset");
        drawBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                model.resetToDefault();
                panel.createBufferedImage();
                panel.repaint();
            }
        });

        // Undo button, returns to the previous state of the model
        undoBtn = new JButton("Undo");
        undoBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                model.undo();
                panel.createBufferedImage();
                panel.repaint();
            }
        });

        // Redo button, puts the state forward when undo has been used.
        redoBtn = new JButton("Redo");
        redoBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                model.redo();
                panel.createBufferedImage();
                panel.repaint();
            }
        });

        // Iterations button, displays the current max iterations and launches dialog box to change the value
        // when pressed.
        changeIterationsBtn = new JButton("Iterations: " + model.getMax_iterations());
        changeIterationsBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String input = JOptionPane.showInputDialog("Please input new Max Iterations");
                model.setMax_iterations(Integer.parseInt(input));
                panel.createBufferedImage();
                panel.repaint();
            }
        });

        // Toggle mode checkbox, allows user to switch between zooming and pan functionality.
        toggleModeBtn = new JCheckBox("Pan", false);
        toggleModeBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JCheckBox cb = (JCheckBox) e.getSource();
                if (cb.isSelected()) {
                    panel.zoom = false;
                } else {
                    panel.zoom = true;
                }
            }
        });

        // Toggle ratio checkbox, allows user to toggle whether they want to see the estimated zoom magnification.
        toggleRatio = new JCheckBox("Zoom Magnification", false);
        toggleRatio.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JCheckBox cb = (JCheckBox) e.getSource();
                if (cb.isSelected()) {
                    panel.displayRatio = true;
                } else {
                    panel.displayRatio = false;
                }
                panel.createBufferedImage();
                panel.repaint();
            }
        });

        // Toggle color checkbox, allows user to select whether they want to see the model rendered with color or just B&W.
        toggleColor = new JCheckBox("Color", true);
        toggleColor.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JCheckBox cb = (JCheckBox) e.getSource();
                if (cb.isSelected()) {
                    panel.color = true;
                } else {
                    panel.color = false;
                }
                panel.createBufferedImage();
                panel.repaint();
            }
        });

        // Random color, randomises the hue fixed value so that the color pattern changes
        changeColor = new JButton("Change Colour");
        changeColor.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Random r = new Random();
                float random = 0.0f + r.nextFloat() * (360.0f - 0.0f);
                panel.colorHue = random;

                panel.createBufferedImage();
                panel.repaint();
            }
        });


        // add buttons and checkboxes to the toolbar
        toolbar.add(drawBtn);
        toolbar.add(undoBtn);
        toolbar.add(redoBtn);
        toolbar.add(changeIterationsBtn);
        toolbar.add(toggleModeBtn);
        toolbar.add(toggleRatio);
        toolbar.add(toggleColor);
        toolbar.add(changeColor);


        // add toolbar to the top of the main frame
        mainFrame.add(toolbar, BorderLayout.NORTH);
    }


    /**
     * Sets up all objects within the menuBar bar adding listeners and functionality to them.
     */
    private void setupMenu() {
        // Create the menu
        JMenu file = new JMenu("File");
        // Create the items to put in the menu
        JMenuItem load = new JMenuItem("Load");
        JMenuItem save = new JMenuItem("Save");
        JMenuItem capture = new JMenuItem("Capture");
        // Add menuitems to the menu
        file.add(load);
        file.add(save);
        file.add(capture);
        //Add the menu to the menubar.
        menuBar.add(file);

        // Load button creates a file chooser so the user can select the file from there system to load.
        load.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Specify a file to load");
                int userSelection = fileChooser.showOpenDialog(mainFrame);
                if (userSelection == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    loadFile(selectedFile);

                    panel.createBufferedImage();
                    panel.repaint();
                }
            }
        });

        // Save button creates file chooser with input so user can specify name of file and directory to save there
        // progress within the mandelbrot.
        save.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Specify a file to save location in Mandelbrot");

                int userSelection = fileChooser.showSaveDialog(mainFrame);

                if (userSelection == JFileChooser.APPROVE_OPTION) {
                    File fileToSave = fileChooser.getSelectedFile();
                    saveModel(fileToSave);
                }
            }
        });

        // Capture is used for when user would rather save as an image not the location in the program.
        capture.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Specify a file to save the image of Mandelbrot");

                int userSelection = fileChooser.showSaveDialog(mainFrame);

                if (userSelection == JFileChooser.APPROVE_OPTION) {
                    File fileToSave = fileChooser.getSelectedFile();
                    saveImage(fileToSave);
                }
            }
        });

        // Set the menuBar as the menubar in the main frame.
        mainFrame.setJMenuBar(menuBar);
    }

    /**
     * Given a file object, method will serialize model object and save it to that file as .txt file.
     *
     * @param fileToSave File containing path of location to save to.
     */
    private void saveModel(File fileToSave) {
        // Get the path of the file
        String path = fileToSave.getAbsolutePath();

        // Check it ends with .txt otherwise add .txt to it.
        if (!path.endsWith(".txt")) {
            String newPath = path + ".txt";
            fileToSave = new File(newPath);
        }

        // Try with resources create an object output stream with fileoutputstream to write to.
        try (FileOutputStream fOut = new FileOutputStream(fileToSave);
             ObjectOutputStream oOut = new ObjectOutputStream(fOut)) {

            //Write the object to the file.
            oOut.writeObject(model);

        } catch (Exception e) {
            System.out.println("Delegate saveModel: " + e.getMessage());
        }
    }

    /**
     * Given a file, create an input stream and write the serialized model object back to the model variable.
     *
     * @param selectedFile File containing serialized model object.
     */
    private void loadFile(File selectedFile) {
        try (FileInputStream fIn = new FileInputStream(selectedFile);
             ObjectInputStream oIn = new ObjectInputStream(fIn)) {

            this.model = (Model) oIn.readObject();

        } catch (Exception e) {
            System.out.println("Delegate saveModel: " + e.getMessage());
        }
    }

    /**
     * Given a file path, method will screencapture the currently drawn panel and save it as a .jpeg file.
     *
     * @param fileToSave File containing location to save to.
     */
    private void saveImage(File fileToSave) {
        //Get the path
        String path = fileToSave.getAbsolutePath();

        // Check if ends with .jpeg, if not add it
        if (!path.endsWith(".jpeg")) {
            String newPath = path + ".jpeg";
            fileToSave = new File(newPath);
        }

        // Initiate a buffered image to write to.
        BufferedImage imagebuffer = null;

        try {
            imagebuffer = new Robot().createScreenCapture(panel.getBounds());
        } catch (Exception e) {
            System.out.println("Delegate saveImage1: " + e.getMessage());
        }

        // Draw the panel to the buffered image.
        Graphics2D graphics2D = imagebuffer.createGraphics();
        panel.paint(graphics2D);

        // Write the buffered image to a file.
        try {
            ImageIO.write(imagebuffer, "jpeg", fileToSave);
        } catch (Exception e) {
            System.out.println("Delegate saveImage2: " + e.getMessage());
        }
    }


    /**
     * Property change listener which is called when events are received from the model.
     *
     * @param event Event fired from the model.
     */
    @Override
    public void propertyChange(PropertyChangeEvent event) {

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                System.out.println("Property change event");
                changeIterationsBtn.setText("Iterations: " + event.getNewValue().toString());
                panel.createBufferedImage();
                panel.repaint();
            }
        });
    }

    /**
     * Panel class which is a JPanel, this contains all methods to display the Mandelbrot and allow users to draw
     * the zoom box and the pan line.
     * It uses booleans to determine whether zoom needs to be shown as well as if it needs to use color.
     */
    class Panel extends JPanel {
        private boolean zoom = true;
        private boolean drawing = false;
        private boolean displayRatio = false;
        private boolean color = true;
        private float colorHue = 3.0f;

        private int clickX;
        private int clickY;
        private int x;
        private int y;
        private int xCurrent;
        private int yCurrent;
        private int width;
        private int height;

        private BufferedImage mandelbrotImage;

        /**
         * Constructor to create the panel and add a mouse listener for the zoom and pan.
         */
        Panel() {
            MyMouseAdapter mouseAdapter = new MyMouseAdapter();
            addMouseListener(mouseAdapter);
            addMouseMotionListener(mouseAdapter);
            createBufferedImage();
        }

        /**
         * Paint method which gets the points of the Mandelbrot and draws them as zero length lines.
         * Also displays the zoom and display ratio if needed.
         * @param g
         */
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            g.drawImage(mandelbrotImage, 0,0,this);

            // If the user is drawing and zoom is selected, display the box, otherwise draw the pan line
            if (drawing && zoom) {
                g.setColor(Color.RED);
                g.drawRect(x, y, width, height);
            } else if (drawing && !zoom) {
                g.setColor(Color.RED);
                g.drawLine(clickX, clickY, xCurrent, yCurrent);
            }

            // If ratio is wanted then display the zoom ratio text.
            if (displayRatio) {
                String ratio = "Zoom x" + model.getRatio();
                g.setColor(Color.BLACK);
                g.setFont(new Font("TimesRoman", Font.BOLD, 22));
                g.drawString(ratio, model.resolution / 10, model.resolution / 10);
            }
        }


        /**
         * Creates a buffered image of the mandelbrot, this saves computational power by just having to render the
         * image rather than recalculate the whole image when drawing the zoom and pan lines.
         */
        public void createBufferedImage(){
            Dimension imageDimension = new Dimension(model.resolution, model.resolution);
            mandelbrotImage = new BufferedImage(imageDimension.width, imageDimension.height, BufferedImage.TYPE_INT_RGB);

            Graphics2D g2d = mandelbrotImage.createGraphics();
            g2d.setBackground(Color.black);
            g2d.fillRect(0,0, imageDimension.width, imageDimension.height);

            // Get the points of the Mandelbrot
            int[][] points = model.getPoints();

            // For all the points in the mandelbrot set, draw them as a zero length line or 1px
            for (int y = 0; y < model.resolution; y++) {
                for (int x = 0; x < model.resolution; x++) {
                    //If color is wanted add color otherwise just use black and white
                    if (color) {
                        g2d.setColor(getColor(points[y][x]));
                        g2d.drawLine(x, y, x, y);
                    } else if (!color && points[y][x] >= model.getMax_iterations()) {
                        g2d.setColor(Color.BLACK);
                        g2d.drawLine(x, y, x, y);
                    }
                }
            }

            System.out.println("Buffered image refresh");

        }


        /**
         * Uses HSB color space model to map the mandelbrot value given to a hue color.
         * @param value Mandelbrot no of iterations reached value.
         * @return HSB color object to use for the value given.
         */
        private Color getColor(int value) {
            if (value == model.getMax_iterations()) {
                return Color.BLACK;
            } else {
                return Color.getHSBColor((float) value * colorHue / (float) model.getMax_iterations(), 1.0f, 1.0f);
            }
        }

        /**
         * Mouse adaptor to process the user clicks on the panel.
         */
        private class MyMouseAdapter extends MouseAdapter {
            private Point mousePress = null;

            // If the user presses down on the mouse, get the point and save it
            @Override
            public void mousePressed(MouseEvent e) {
                mousePress = e.getPoint();
                clickX = mousePress.x;
                clickY = mousePress.y;
            }

            // If the user starts to drag the mouse, then calculate the length and width needed to draw a rectangle
            // from the initial point clicked to the current point.
            @Override
            public void mouseDragged(MouseEvent e) {
                drawing = true;
                xCurrent = e.getPoint().x;
                yCurrent = e.getPoint().y;
                x = Math.min(clickX, xCurrent);
                y = Math.min(clickY, yCurrent);
                width = Math.abs(clickX - xCurrent);
                height = Math.abs(clickY - yCurrent);

                repaint(); // Call repaint to make sure the box drawn updates in real time.
            }

            // When the user releases, get the point of release and call the methods in the model.
            @Override
            public void mouseReleased(MouseEvent e) {
                drawing = false;
                Point mouseReleased = e.getPoint();

                //Pass the point clicked and the point released
                if (zoom) {
                    model.setZoom(mousePress, mouseReleased);
                } else {
                    model.pan(mousePress, mouseReleased);
                }
                repaint();
            }

        }
    }
}
