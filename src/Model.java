import java.awt.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.util.ArrayList;

public class Model implements Serializable {

    private PropertyChangeSupport notifier;
    private MandelbrotCalculator mandelCalc;

    //Initial values
    private double min_real;
    private double max_real;
    private double min_imaginary;
    private double max_imaginary;
    private int max_iterations;
    public int resolution = 800;

    private ArrayList<Double> log_min_real;
    private ArrayList<Double> log_max_real;
    private ArrayList<Double> log_min_imaginary;
    private ArrayList<Double> log_max_imaginary;
    private ArrayList<Integer> log_max_iterations;

    private int logCounter;



    public Model(){
        mandelCalc = new MandelbrotCalculator();
        notifier = new PropertyChangeSupport(this);
        resetToDefault();

    }

    public void undo(){
        if(logCounter - 1 >= 0) {
            logCounter--;
            min_real = log_min_real.get(logCounter);
            max_real = log_max_real.get(logCounter);
            min_imaginary = log_min_imaginary.get(logCounter);
            max_imaginary = log_max_imaginary.get(logCounter);
            max_iterations = log_max_iterations.get(logCounter);
        }

        notifier.firePropertyChange("theText", "test", "test");
    }

    public void redo(){
        if(logCounter + 1 < log_min_real.size()) {
            logCounter++;
            min_real = log_min_real.get(logCounter);
            max_real = log_max_real.get(logCounter);
            min_imaginary = log_min_imaginary.get(logCounter);
            max_imaginary = log_max_imaginary.get(logCounter);
            max_iterations = log_max_iterations.get(logCounter);
        }

        notifier.firePropertyChange("theText", "test", "test");
    }

    private void updateLog(){
        logCounter++;
        log_min_real.add(this.min_real);
        log_max_real.add(this.max_real);
        log_min_imaginary.add(this.min_imaginary);
        log_max_imaginary.add(this.max_imaginary);
        log_max_iterations.add(this.max_iterations);
    }

    public int getMax_iterations() {
        return max_iterations;
    }

    public void setMax_iterations(int max_iterations) {
        this.max_iterations = max_iterations;
        updateLog();
        notifier.firePropertyChange("updateIterations", 0, max_iterations);

    }

    public double getRatio(){
        return (MandelbrotCalculator.INITIAL_MAX_REAL - MandelbrotCalculator.INITIAL_MIN_REAL)/(max_real-min_real);
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

//        System.out.println("NewMinReal = " + newMinReal);
//        System.out.println("NewMaxReal = " + newMaxReal);
//        System.out.println("NewMinImaginary = " + newMinImaginary);
//        System.out.println("NewMaxImaginary = " + newMaxImaginary);

        min_real = newMinReal;
        max_real = newMaxReal;
        min_imaginary = newMinImaginary;
        max_imaginary = newMaxImaginary;

        updateLog();

        notifier.firePropertyChange("theText", "test", "test");

    }

    public void pan(Point startPoint, Point endPoint){
        int startX = startPoint.x;
        int startY = startPoint.y;
        int finishX = endPoint.x;
        int finishY = endPoint.y;

        System.out.println(startPoint);
        System.out.println(endPoint);

        int lengthX = startX - finishX;
        int lengthY = startY - finishY;

//        System.out.println("LengthX = " + lengthX);
//        System.out.println("LengthY = " + lengthY);

        // newValue = (((oldValue - oldMin) * (newMax - newMin)) / (oldMax - oldMin)) + newMin
        double lengthReal = min_real - ((((lengthX) * (max_real- min_real))/resolution) + min_real);

        // newValue = (((oldValue - oldMin) * (newMax - newMin)) / (oldMax - oldMin)) + newMin
        double lengthImaginary = min_imaginary- (((lengthY-0.0) * (max_imaginary-min_imaginary)) / resolution + min_imaginary);

//        System.out.println("LengthReal = " + lengthReal);
//        System.out.println("LengthImag = " + lengthImaginary);

        min_real -= lengthReal;
        max_real -= lengthReal;
        min_imaginary -= lengthImaginary;
        max_imaginary -= lengthImaginary;

        updateLog();

        notifier.firePropertyChange("theText", "test", "test");
    }

    public void resetToDefault(){
        min_real = MandelbrotCalculator.INITIAL_MIN_REAL;
        max_real = MandelbrotCalculator.INITIAL_MAX_REAL;
        min_imaginary = MandelbrotCalculator.INITIAL_MIN_IMAGINARY;
        max_imaginary = MandelbrotCalculator.INITIAL_MAX_IMAGINARY;
        max_iterations = MandelbrotCalculator.INITIAL_MAX_ITERATIONS;

        log_min_real = new ArrayList<>();
        log_max_real = new ArrayList<>();
        log_min_imaginary = new ArrayList<>();
        log_max_imaginary = new ArrayList<>();
        log_max_iterations = new ArrayList<>();

        logCounter = 0;
        log_min_real.add(this.min_real);
        log_max_real.add(this.max_real);
        log_min_imaginary.add(this.min_imaginary);
        log_max_imaginary.add(this.max_imaginary);
        log_max_iterations.add(this.max_iterations);

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
