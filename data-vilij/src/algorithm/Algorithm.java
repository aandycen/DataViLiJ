package algorithm;

/**
 * This interface provides a way to run an algorithm on a thread as a
 * {@link java.lang.Runnable} object.
 *
 * @author Ritwik Banerjee
 */
public interface Algorithm extends Runnable {

    void setMaxIterations(int iterations);

    void setUpdateInterval(int interval);

    int getMaxIterations();

    int getUpdateInterval();

    boolean tocontinue();

}
