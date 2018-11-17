import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class Delegate implements PropertyChangeListener {

    private static final int FRAME_HEIGHT = 1000;
    private static final int FRAME_WIDTH = 1000;

    private Model model;
    private JFrame mainFrame;
    private Panel panel;
    private JMenuBar menu;
    private JToolBar toolbar;
    private JButton drawBtn, undoBtn, redoBtn, changeIterationsBtn;
    private JCheckBox toggleModeBtn, toggleRatio;
    private JTextArea iterationsTV;


    public Delegate(Model model){
        this.model = model;
        this.mainFrame = new JFrame();
        menu = new JMenuBar();
        toolbar = new JToolBar();

        panel = new Panel(this);
        mainFrame.add(panel);

        setupToolbar();
        mainFrame.setSize (FRAME_WIDTH, FRAME_HEIGHT);
        mainFrame.setVisible(true);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


        model.addObserver(this);
    }


    private void setupToolbar(){
        drawBtn = new JButton("Reset");
        drawBtn.addActionListener(new ActionListener(){     // to translate event for this button into appropriate model method call
            public void actionPerformed(ActionEvent e){
                // should  call method in model class if you want it to affect model
                model.resetToDefault();
                panel.repaint();
            }
        });

        undoBtn = new JButton("Undo");
        undoBtn.addActionListener(new ActionListener(){     // to translate event for this button into appropriate model method call
            public void actionPerformed(ActionEvent e){
                // should  call method in model class if you want it to affect model
                model.undo();
                panel.repaint();
            }
        });

        redoBtn = new JButton("Redo");
        redoBtn.addActionListener(new ActionListener(){     // to translate event for this button into appropriate model method call
            public void actionPerformed(ActionEvent e){
                // should  call method in model class if you want it to affect model
                model.redo();
                panel.repaint();
            }
        });

        changeIterationsBtn = new JButton("Iterations: " + model.getMax_iterations());
        changeIterationsBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String input = JOptionPane.showInputDialog("Please input new Max Iterations");
                model.setMax_iterations(Integer.parseInt(input));
                panel.repaint();
            }
        });

//        iterationsTV = new JTextArea("Current Max Iterations = " + model.getMax_iterations());
//        iterationsTV.setEditable(false);
//        iterationsTV.setLineWrap(true);

        toggleModeBtn = new JCheckBox("Pan", false);
        toggleModeBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JCheckBox cb = (JCheckBox) e.getSource();
                if(cb.isSelected()){
                    panel.zoom = false;
                } else {
                    panel.zoom = true;
                }
            }
        });

        toggleRatio = new JCheckBox("Zoom Magnification", false);
        toggleRatio.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JCheckBox cb = (JCheckBox) e.getSource();
                if(cb.isSelected()){
                    panel.displayRatio = true;
                } else {
                    panel.displayRatio = false;
                }
                panel.repaint();
            }
        });


        // add buttons, label, and textfield to the toolbar
        toolbar.add(drawBtn);
        toolbar.add(undoBtn);
        toolbar.add(redoBtn);
        toolbar.add(changeIterationsBtn);
        toolbar.add(toggleModeBtn);
        toolbar.add(toggleRatio);


        // add toolbar to north of main frame
        mainFrame.add(toolbar, BorderLayout.NORTH);
    }

    // Property change listener which is called when events fired from the model
    @Override
    public void propertyChange(PropertyChangeEvent event) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                panel.repaint();
                changeIterationsBtn.setText("Iterations: " + model.getMax_iterations());
                //panel.paintComponent(panel.getGraphics());
            }
        });
    }


    class Panel extends JPanel{
        private Delegate delegate;
        private Rectangle rect = null;
        private boolean zoom = true;
        private boolean drawing = false;
        private boolean displayRatio = false;

        private int clickX;
        private int clickY;
        private int x;
        private int y;
        private int xCurrent;
        private int yCurrent;
        private int width;
        private int height;

        private Color[] colorArray = new Color[model.getMax_iterations()];

        private int[][] points;

        Panel(Delegate delegate){
            this.delegate = delegate;
            //setPreferredSize(new Dimension(600,600));
            MyMouseAdapter mouseAdapter = new MyMouseAdapter();
            addMouseListener(mouseAdapter);
            addMouseMotionListener(mouseAdapter);
            createColorArray();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            int[][] points = model.getPoints();

            System.out.println("Redrawn!!");
            //g.setColor(Color.BLACK);

            if(displayRatio) {
                String ratio = "Zoom x" + model.getRatio();
                g.drawString(ratio, model.resolution / 10, model.resolution / 10);
            }

            for(int y = 0; y< model.resolution; y++){
                for(int x=0; x<model.resolution; x++){
                        g.setColor(getColor(points[y][x]));
                        g.drawLine(x,y,x,y);
                }
            }


            if (drawing && zoom) {
                g.setColor(Color.RED);
                g.drawRect(x,y,width,height);
            } else if (drawing && !zoom) {
                g.setColor(Color.RED);
                g.drawLine(clickX,clickY,xCurrent,yCurrent);
            }


        }

        private void createColorArray(){
            for(int i=0; i<colorArray.length; i++){
                int color = 2 * i *256/ colorArray.length;
                if(color>255){
                    color = 511 - color;
                }
                colorArray[i] = new Color(color, color, color);
            }
        }

        private Color getColor(int value){

            if(value == model.getMax_iterations()){
                return Color.BLACK;
            } else {
                return Color.getHSBColor((float)value * 2.0f / (float)model.getMax_iterations(), 1.0f, 1.0f);
            }



        }


        private class MyMouseAdapter extends MouseAdapter {
            private Point mousePress = null;

            @Override
            public void mousePressed(MouseEvent e) {
                mousePress = e.getPoint();
                clickX = mousePress.x;
                clickY = mousePress.y;
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                drawing = true;
                x = Math.min(mousePress.x, e.getPoint().x);
                y = Math.min(mousePress.y, e.getPoint().y);
                width = Math.abs(mousePress.x - e.getPoint().x);
                height = Math.abs(mousePress.y - e.getPoint().y);
                xCurrent = e.getPoint().x;
                yCurrent = e.getPoint().y;

                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                drawing = false;
                Point mouseReleased = e.getPoint();

                System.out.println("Mouse clicked =" + mousePress);
                System.out.println("Mouse released = "+ mouseReleased);

                //Pass the point clicked and the point released
                if(zoom){
                    model.setZoom(mousePress, mouseReleased);
                } else {
                    model.pan(mousePress, mouseReleased);
                }
                repaint();
            }

        }
    }
}
