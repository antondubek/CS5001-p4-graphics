import java.awt.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.util.ArrayList;


/**
 * Model class provides all backend functionality to the model and interacts with the MandelbrotCalculator.
 * Will fire PropertyChangeEvents when something has been altered. Implements serializable so that the
 * locations and log can be saved within a file for the user to come back to.
 */
public class Model implements Serializable {

    private PropertyChangeSupport notifier;
    private MandelbrotCalculator mandelCalc;

    //Values for calculating mandelbrot
    private double min_real;
    private double max_real;
    private double min_imaginary;
    private double max_imaginary;
    private int max_iterations;
    public int resolution = 800;

    // Arraylists used for logging the states
    private ArrayList<Double> log_min_real;
    private ArrayList<Double> log_max_real;
    private ArrayList<Double> log_min_imaginary;
    private ArrayList<Double> log_max_imaginary;
    private ArrayList<Integer> log_max_iterations;

    // Counter so know where up to in the logs when undoing and redoing
    private int logCounter;

    /**
     * Constructor, creates new MandelbrotCalculator object, instantiates the notifier and sets
     * all the values for calculating the mandelbrot set to the default within mandlebrot calc.
     */
    public Model() {
        mandelCalc = new MandelbrotCalculator();
        notifier = new PropertyChangeSupport(this);
        resetToDefault();
    }

    /**
     * Provides undo functionality by getting and setting to previous states from arraylists.
     * Fires propertychange when complete
     */
    public void undo() {
        //Error check to make sure doesn't go negative, ie before the program started
        if (logCounter - 1 >= 0) {
            logCounter--;
            setValues(logCounter);
        }

        notifier.firePropertyChange("updateIterations", 0, max_iterations);
    }

    /**
     * Provides redo functionality by getting and setting to further states from arraylists.
     * Fires propertychange when complete
     */
    public void redo() {
        //Error check to make sure user cannot redo to an unknown point
        if (logCounter + 1 < log_min_real.size()) {
            logCounter++;
            setValues(logCounter);
        }

        notifier.firePropertyChange("updateIterations", 0, max_iterations);
    }

    /**
     * Sets each value for the mandelbrot to be calculated, given a previous or forward location in the logs.
     *
     * @param logCounter Int of what index of the logs currently on. Will not be <0 or >listSize
     */
    private void setValues(int logCounter) {
        min_real = log_min_real.get(logCounter);
        max_real = log_max_real.get(logCounter);
        min_imaginary = log_min_imaginary.get(logCounter);
        max_imaginary = log_max_imaginary.get(logCounter);
        max_iterations = log_max_iterations.get(logCounter);
    }

    /**
     * Adds the current values to the logs.
     */
    private void updateLog() {
        logCounter++;
        log_min_real.add(this.min_real);
        log_max_real.add(this.max_real);
        log_min_imaginary.add(this.min_imaginary);
        log_max_imaginary.add(this.max_imaginary);
        log_max_iterations.add(this.max_iterations);
    }

    /**
     * Getter for the max iterations
     *
     * @return int The current max number of iterations mandelbrot is calculated to.
     */
    public int getMax_iterations() {
        return max_iterations;
    }

    /**
     * Setter for max iterations. Updates variable and then updates log before firing propertychangeevents
     *
     * @param max_iterations New max iterations
     */
    public void setMax_iterations(int max_iterations) {
        this.max_iterations = max_iterations;
        updateLog();
        notifier.firePropertyChange("updateIterations", 0, max_iterations);

    }

    /**
     * Getter for the zoom ratio. Calculates ratio of current real values compared to initial real values.
     * Not perfect as only calculating based on the real values but close enough to give a reasonable representation.
     *
     * @return Double Estimated Zoom
     */
    public double getRatio() {
        return (MandelbrotCalculator.INITIAL_MAX_REAL - MandelbrotCalculator.INITIAL_MIN_REAL) / (max_real - min_real);
    }

    /**
     * Simple method to add a listener to notify when events are fired.
     *
     * @param listener Listener to be added to the notifier.
     */
    public void addObserver(PropertyChangeListener listener) {
        notifier.addPropertyChangeListener(listener);
    }

    /**
     * Converts two points drawn on the screen to new parameters to calculate the new mandelbrot set.
     *
     * @param startPoint Point top left point of new mandelbrot
     * @param endPoint   Point bottom right point of new mandelbrot set.
     */
    public void setZoom(Point startPoint, Point endPoint) {

        //Get the x and y values for the 2 points passed.
        int startX = startPoint.x;
        int startY = startPoint.y;
        int finishX = endPoint.x;
        int finishY = endPoint.y;

        //map X points to mandlebrot real
        double newMinReal = convertReal(startX);
        double newMaxReal = convertReal(finishX);

        //map Y points to mandlebrot imaginary
        double newMinImaginary = convertImaginary(startY);
        double newMaxImaginary = convertImaginary(finishY);

        // Set the parameteres to the new defined ones
        // If the box is drawn the other way, flip the values so the image is not flipped.
        if(finishX < startX){
            min_real = newMaxReal;
            max_real = newMinReal;
        } else {
            min_real = newMinReal;
            max_real = newMaxReal;
        }

        if(finishY < startY){
            min_imaginary = newMaxImaginary;
            max_imaginary = newMinImaginary;
        } else {
            min_imaginary = newMinImaginary;
            max_imaginary = newMaxImaginary;
        }

        // Update the log
        updateLog();

        // Send of events so that the model can be re-rendered
        notifier.firePropertyChange("updateIterations", 0, max_iterations);
    }

    /**
     * Maps an X value in range [0,resolution] to range [min_real, max_real]
     *
     * @param pointX value to map in range [0,resolution]
     * @return New mapped value in range [min_real, max_real]
     */
    private double convertReal(int pointX) {
        return (pointX) * ((max_real - (min_real)) / resolution) + (min_real);
    }

    /**
     * Maps a Y value in range [0,resolution] to range [min_imaginary, max_imaginary]
     *
     * @param pointY value to map in range [0,resolution]
     * @return New mapped value in range [min_imaginary, max_imaginary]
     */
    private double convertImaginary(int pointY) {
        return (((pointY) * (max_imaginary - (min_imaginary))) / (resolution)) + (min_imaginary);
    }

    /**
     * Converts the length of 2 points passed to a distance which to then pan the mandelbrot set.
     *
     * @param startPoint Point to pan from
     * @param endPoint   Point to pan too
     */
    public void pan(Point startPoint, Point endPoint) {

        //Get the x and y values for the 2 points passed.
        int startX = startPoint.x;
        int startY = startPoint.y;
        int finishX = endPoint.x;
        int finishY = endPoint.y;

        // Calculate the horizontal and vertical length of the line
        int lengthX = startX - finishX;
        int lengthY = startY - finishY;

        // Convert the lengths to respective real and imaginary values
        double lengthReal = min_real - convertReal(lengthX);
        double lengthImaginary = min_imaginary - convertImaginary(lengthY);

        // Set the new values for the mandelbrot
        min_real -= lengthReal;
        max_real -= lengthReal;
        min_imaginary -= lengthImaginary;
        max_imaginary -= lengthImaginary;

        // Update the log
        updateLog();

        // Fire events so that the mandelbrot is re-rendered
        notifier.firePropertyChange("updateIterations", 0, max_iterations);
    }

    /**
     * Resets all values back the original values and resets the logs.
     */
    public void resetToDefault() {
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

        notifier.firePropertyChange("updateIterations", 0, max_iterations);
    }

    /**
     * Calculates the mandelbrot and returns an array of x, y values going up to the max_iterations
     *
     * @return Array of int[y][x] point values.
     */
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
