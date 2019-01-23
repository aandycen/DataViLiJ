package algorithms;

import algorithm.Clusterer;
import algorithm.DataSet;
import javafx.geometry.Point2D;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javafx.application.Platform;
import javafx.scene.chart.XYChart;
import settings.AppPropertyTypes;
import ui.AppUI;
import static vilij.propertymanager.PropertyManager.getManager;
import vilij.templates.ApplicationTemplate;

/**
 * @author Andy Cen
 */
public class KMeansClusterer extends Clusterer {

    private DataSet dataset;
    private List<Point2D> centroids;

    private ApplicationTemplate applicationTemplate;

    private int maxIterations;
    private int updateInterval;
    private AtomicBoolean tocontinue;
    private AtomicBoolean continuous;

    public KMeansClusterer(DataSet dataset, int maxIterations, int updateInterval, int numberOfClusters, boolean continuous, ApplicationTemplate applicationTemplate) {
        super(numberOfClusters);
        this.dataset = dataset;
        this.maxIterations = maxIterations;
        this.updateInterval = updateInterval;
        this.applicationTemplate = applicationTemplate;
        this.continuous = new AtomicBoolean(continuous);
        this.tocontinue = new AtomicBoolean(false);
    }

    public void setDataSet(DataSet dataset) {
        this.dataset = dataset;
    }

    @Override
    public int getMaxIterations() {
        return maxIterations;
    }

    @Override
    public int getUpdateInterval() {
        return updateInterval;
    }

    @Override
    public boolean tocontinue() {
        return tocontinue.get();
    }

    public boolean continuous() {
        return continuous.get();
    }

    @Override
    public void run() {
        initializeCentroids();
        int iteration = 0;
        while (iteration++ < maxIterations & tocontinue.get()) {
            ((AppUI) applicationTemplate.getUIComponent()).setIteration(iteration);
            assignLabels();
            recomputeCentroids();
            if (!tocontinue.get()) {
                Platform.runLater(() -> {
                    toChartData();
                });
                break;
            }
            if (iteration % updateInterval == 0) {
                Platform.runLater(() -> {
                    toChartData();
                });
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    return;
                }
                if (!continuous()) {
                    try {
                        ((AppUI) applicationTemplate.getUIComponent()).updateRun.setDisable(false);
                        Platform.runLater(() -> {
                            ((AppUI) applicationTemplate.getUIComponent()).updateRun.setText(applicationTemplate.manager.getPropertyValue(AppPropertyTypes.UPDATE_BUTTON_RESUME.name()));
                            ((AppUI) applicationTemplate.getUIComponent()).scrnshotButton.setVisible(true);
                        });
                        synchronized (Thread.currentThread()) {
                            Thread.currentThread().wait();
                        }
                    } catch (InterruptedException e) {
                        return;
                    }
                }
            }
        }
        // End of While
        Thread.currentThread().interrupt();
        ((AppUI) applicationTemplate.getUIComponent()).disableButtons(false);
        ((AppUI) applicationTemplate.getUIComponent()).scrnshotButton.setVisible(true);
        ((AppUI) applicationTemplate.getUIComponent()).algorithmOnFinish();
        Platform.runLater(() -> {
            ((AppUI) applicationTemplate.getUIComponent()).displayIterationLabel.setText(((AppUI) applicationTemplate.getUIComponent()).displayIterationLabel.getText().concat(" (Finished)"));
        });

    }

    void toChartData() {
        ((AppUI) applicationTemplate.getUIComponent()).getChart().getData().clear();
        Set<String> labels = new HashSet<>(dataset.getLabels().values());
        for (String label : labels) {
            XYChart.Series<Number, Number> series = new XYChart.Series<>();
            series.setName(label);
            dataset.getLabels().entrySet().stream().filter(entry -> entry.getValue().equals(label)).forEach(entry -> {
                Point2D point = dataset.getLocations().get(entry.getKey());
                series.getData().add(new XYChart.Data<>(point.getX(), point.getY()));
            });
            ((AppUI) applicationTemplate.getUIComponent()).getChart().getData().add(series);
            series.getNode().lookup(getManager().getPropertyValue(AppPropertyTypes.LINE_LOOKUP.name()));
            series.getNode().setStyle(getManager().getPropertyValue(AppPropertyTypes.INVIS_LINE.name()));
            for (XYChart.Series<Number, Number> serie : ((AppUI) applicationTemplate.getUIComponent()).getChart().getData()) {
                for (XYChart.Data<Number, Number> data : serie.getData()) {
                    data.getNode().setStyle(getManager().getPropertyValue(AppPropertyTypes.CURSOR_STYLE.name()));
                }
            }
        }
    }

    private void initializeCentroids() {
        Set<String> chosen = new HashSet<>();
        List<String> instanceNames = new ArrayList<>(dataset.getLabels().keySet());
        Random r = new Random();
        while (chosen.size() < numberOfClusters) {
            int i = r.nextInt(instanceNames.size());
            while (chosen.contains(instanceNames.get(i))) {
                ++i;
                // i = (++i % instanceNames.size());
            }
            chosen.add(instanceNames.get(i));
        }
        centroids = chosen.stream().map(name -> dataset.getLocations().get(name)).collect(Collectors.toList());
        tocontinue.set(true);
    }

    private void assignLabels() {
        dataset.getLocations().forEach((instanceName, location) -> {
            double minDistance = Double.MAX_VALUE;
            int minDistanceIndex = -1;
            for (int i = 0; i < centroids.size(); i++) {
                double distance = computeDistance(centroids.get(i), location);
                if (distance < minDistance) {
                    minDistance = distance;
                    minDistanceIndex = i;
                }
            }
            dataset.getLabels().put(instanceName, Integer.toString(minDistanceIndex));
        });
    }

    private void recomputeCentroids() {
        tocontinue.set(false);
        IntStream.range(0, numberOfClusters).forEach(i -> {
            AtomicInteger clusterSize = new AtomicInteger();
            Point2D sum = dataset.getLabels()
                    .entrySet()
                    .stream()
                    .filter(entry -> i == Integer.parseInt(entry.getValue()))
                    .map(entry -> dataset.getLocations().get(entry.getKey()))
                    .reduce(new Point2D(0, 0), (p, q) -> {
                        clusterSize.incrementAndGet();
                        return new Point2D(p.getX() + q.getX(), p.getY() + q.getY());
                    });
            Point2D newCentroid = new Point2D(sum.getX() / clusterSize.get(), sum.getY() / clusterSize.get());
            if (!newCentroid.equals(centroids.get(i))) {
                centroids.set(i, newCentroid);
                tocontinue.set(true);
            }
        });
    }

    private static double computeDistance(Point2D p, Point2D q) {
        return Math.sqrt(Math.pow(p.getX() - q.getX(), 2) + Math.pow(p.getY() - q.getY(), 2));
    }

    @Override
    public void setMaxIterations(int iterations) {
        this.maxIterations = iterations;
    }

    @Override
    public void setUpdateInterval(int interval) {
        this.updateInterval = interval;
    }

    public void setToContinue(boolean continuous) {
        this.continuous = new AtomicBoolean(continuous);
    }
}
