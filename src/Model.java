import java.awt.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class Model {

    private PropertyChangeSupport notifier;
    private MandelbrotCalculator mandelCalc;

    //Initial values
    private double min_real;
    private double max_real;
    private double min_imaginary;
    private double max_imaginary;
    private int max_iterations;
    public int resolution = 800;

    public Model(){
        mandelCalc = new MandelbrotCalculator();
        notifier = new PropertyChangeSupport(this);
        resetToDefault();
    }

    public int getMax_iterations() {
        return max_iterations;
    }

    public void setMax_iterations(int max_iterations) {
        this.max_iterations = max_iterations;
        notifier.firePropertyChange("updateIterations", 0, max_iterations);

    }

    public void addObserver(PropertyChangeListener listener){
        notifier.addPropertyChangeListener(listener);
    }

    public void setZoom(Point startPoint, Point endPoint){
        //here get passed the zoom coordinates and recalculate the Mandelbrot
        int startX = startPoint.x;
        int startY = startPoint.y;
        int finishX = endPoint.x;
        int finishY = endPoint.y;

        //map startx to mandlebrot
        // newValue = (((oldValue - oldMin) * (newMax - newMin)) / (oldMax - oldMin)) + newMin
        double newMinReal = (startX) * ((max_real-(min_real))/resolution) + (min_real);

        //map starty to mandlebrot
        double newMinImaginary = (((startY) * (max_imaginary - (min_imaginary))) / (resolution)) + (min_imaginary);

        //map endx to mandlebrot
        double newMaxReal = (finishX) * ((max_real-(min_real))/resolution) + (min_real);

        //map endy to mandlebrot
        double newMaxImaginary = (((finishY) * (max_imaginary - (min_imaginary))) / (resolution)) + (min_imaginary);

        System.out.println("NewMinReal = " + newMinReal);
        System.out.println("NewMaxReal = " + newMaxReal);
        System.out.println("NewMinImaginary = " + newMinImaginary);
        System.out.println("NewMaxImaginary = " + newMaxImaginary);

        min_real = newMinReal;
        max_real = newMaxReal;
        min_imaginary = newMinImaginary;
        max_imaginary = newMaxImaginary;

        notifier.firePropertyChange("theText", "test", "test");

    }

    public void resetToDefault(){
        min_real = MandelbrotCalculator.INITIAL_MIN_REAL;
        max_real = MandelbrotCalculator.INITIAL_MAX_REAL;
        min_imaginary = MandelbrotCalculator.INITIAL_MIN_IMAGINARY;
        max_imaginary = MandelbrotCalculator.INITIAL_MAX_IMAGINARY;
        max_iterations = MandelbrotCalculator.INITIAL_MAX_ITERATIONS;

        notifier.firePropertyChange("theText", "test", "test");
    }

    public int[][] getPoints() {

        int[][] madelbrotData = mandelCalc.calcMandelbrotSet(resolution, resolution,
                min_real,
                max_real,
                min_imaginary,
                max_imaginary,
                max_iterations,
                MandelbrotCalculator.DEFAULT_RADIUS_SQUARED);

        return madelbrotData;
    }

}
