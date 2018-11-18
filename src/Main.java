/**
 * Main class to run the program
 */
public class Main {

    /**
     * Main method, creates model and the delegate.
     * @param args None
     */
    public static void main(String[] args) {
        Model model = new Model();
        Delegate delegate = new Delegate(model);
    }
}
