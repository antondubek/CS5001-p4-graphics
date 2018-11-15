import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Line2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.SQLOutput;

public class Delegate implements PropertyChangeListener {

    private static final int FRAME_HEIGHT = 1000;
    private static final int FRAME_WIDTH = 1000;

    private Model model;
    private JFrame mainFrame;
    private Panel panel;
    private JMenuBar menu;
    private JToolBar toolbar;
    private JButton drawBtn;
    private JButton undoBtn;
    private JButton redoBtn;
    public int[][] points;

    public Delegate(Model model){
        this.model = model;
        this.mainFrame = new JFrame();
        menu = new JMenuBar();
        toolbar = new JToolBar();

        points = model.getPoints();

        panel = new Panel(this);
        mainFrame.add(panel);

        setupToolbar();
        mainFrame.setSize (FRAME_WIDTH, FRAME_HEIGHT);
        mainFrame.setVisible(true);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


        model.addObserver(this);
    }


    private void setupToolbar(){
        drawBtn = new JButton("ReDraw");
        drawBtn.addActionListener(new ActionListener(){     // to translate event for this button into appropriate model method call
            public void actionPerformed(ActionEvent e){
                // should  call method in model class if you want it to affect model
                panel.paintComponent(panel.getGraphics());
            }
        });

        undoBtn = new JButton("Undo");
        undoBtn.addActionListener(new ActionListener(){     // to translate event for this button into appropriate model method call
            public void actionPerformed(ActionEvent e){
                // should  call method in model class if you want it to affect model
                panel.paintComponent(panel.getGraphics());
            }
        });

        redoBtn = new JButton("Redo");
        redoBtn.addActionListener(new ActionListener(){     // to translate event for this button into appropriate model method call
            public void actionPerformed(ActionEvent e){
                // should  call method in model class if you want it to affect model
                panel.paintComponent(panel.getGraphics());
            }
        });

        // add buttons, label, and textfield to the toolbar
        toolbar.add(drawBtn);
        toolbar.add(undoBtn);
        toolbar.add(redoBtn);

        // add toolbar to north of main frame
        mainFrame.add(toolbar, BorderLayout.NORTH);
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                points = model.getPoints();
                panel.paintComponent(panel.getGraphics());
            }
        });
    }


    class Panel extends JPanel{
        private Delegate delegate;
        private Rectangle rect = null;
        private Boolean zoom = false;
        private boolean drawing = false;

        private int x;
        private int y;
        private int width;
        private int height;

        private Color[] colorArray = new Color[48];

        private final Color black = new Color(200, 200, 255);
        private final Color blue = Color.blue;

        Panel(Delegate delegate){
            this.delegate = delegate;
            setPreferredSize(new Dimension(800,800));
            MyMouseAdapter mouseAdapter = new MyMouseAdapter();
            addMouseListener(mouseAdapter);
            addMouseMotionListener(mouseAdapter);
            createColorArray();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            int[][] points = delegate.points;

            System.out.println("Redrawn!!");
            //g.setColor(Color.BLACK);


            for(int y = 0; y< 800; y++){
                for(int x=0; x<800; x++){
                    if(points[y][x] >= 2){
                        g.setColor(colorArray[points[y][x] % colorArray.length]);
                        g.drawLine(x,y,x,y);
                    }
                }
            }

            if (zoom) {
                g.setColor(Color.RED);
                g.drawRect(x,y,width,height);
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


        private class MyMouseAdapter extends MouseAdapter {
            private Point mousePress = null;
            @Override
            public void mousePressed(MouseEvent e) {
                mousePress = e.getPoint();
                zoom = true;
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                drawing = true;
                x = Math.min(mousePress.x, e.getPoint().x);
                y = Math.min(mousePress.y, e.getPoint().y);
                width = Math.abs(mousePress.x - e.getPoint().x);
                height = Math.abs(mousePress.y - e.getPoint().y);

                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                drawing = false;
                zoom = false;
                repaint();
            }

        }
    }
}
