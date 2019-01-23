package algorithm;

/**
 * @author Ritwik Banerjee
 */
public abstract class Clusterer implements Algorithm {

    protected int numberOfClusters;

    public int getNumberOfClusters() {
        return numberOfClusters;
    }

    public void setNumberOfClusters(int k) {
        if (k < 2) {
            k = 2;
        } else if (k > 4) {
            k = 4;
        }
        numberOfClusters = k;
    }

    public Clusterer(int k) {
        if (k == -1) {
            numberOfClusters = -1;
        } else {
            if (k < 2) {
                k = 2;
            } else if (k > 4) {
                k = 4;
            }
            numberOfClusters = k;
        }
    }
}
