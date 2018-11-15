import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class Model {

    private PropertyChangeSupport notifier;
    private MandelbrotCalculator mandelCalc;

    public Model(){
        mandelCalc = new MandelbrotCalculator();
        notifier = new PropertyChangeSupport(this);
    }

    public void addObserver(PropertyChangeListener listener){
        notifier.addPropertyChangeListener(listener);
    }

    public void setZoom(){
        //here get passed the zoom coordinates and recalculate the Mandelbrot
    }

    public int[][] getPoints() {

        int[][] madelbrotData = mandelCalc.calcMandelbrotSet(800, 800,
                MandelbrotCalculator.INITIAL_MIN_REAL, MandelbrotCalculator.INITIAL_MAX_REAL,
                MandelbrotCalculator.INITIAL_MIN_IMAGINARY, MandelbrotCalculator.INITIAL_MAX_IMAGINARY,
                MandelbrotCalculator.INITIAL_MAX_ITERATIONS, MandelbrotCalculator.DEFAULT_RADIUS_SQUARED);

        return madelbrotData;
    }

}
