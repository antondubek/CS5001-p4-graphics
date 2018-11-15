import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
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
    public int[][] points;

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

        points = model.getPoints();

    }


    private void setupToolbar(){
        drawBtn = new JButton("Draw");
        drawBtn.addActionListener(new ActionListener(){     // to translate event for this button into appropriate model method call
            public void actionPerformed(ActionEvent e){
                // should  call method in model class if you want it to affect model
                panel.paintComponent(panel.getGraphics());
            }
        });

        // add buttons, label, and textfield to the toolbar
        toolbar.add(drawBtn);

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

        Panel(Delegate delegate){
            this.delegate = delegate;
            setPreferredSize(new Dimension(1000,1000));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            int[][] points = delegate.points;

            System.out.println("Redrawn!!");

            for(int y = 0; y< 800; y++){
                for(int x=0; x<800; x++){
                    if(points[y][x] >= 50){
                        g.drawLine(x,y,x,y);
                    }
                }
            }
        }
    }
}
