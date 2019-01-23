package algorithms;

import algorithm.Clusterer;
import algorithm.DataSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.scene.chart.XYChart;
import settings.AppPropertyTypes;
import ui.AppUI;
import static vilij.propertymanager.PropertyManager.getManager;
import vilij.templates.ApplicationTemplate;

/**
 *
 * @author Andy Cen
 */
public class RandomClusterer extends Clusterer {

    private ApplicationTemplate applicationTemplate;

    private static final Random RAND = new Random();

    @SuppressWarnings("FieldCanBeLocal")
    private DataSet dataset;

    private int maxIterations;
    private int updateInterval;
    private String[] labels;
    private List<String> instanceNames;

    private AtomicBoolean tocontinue;

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

    public void setDataSet(DataSet dataset) {
        this.dataset = dataset;
    }

    public RandomClusterer(DataSet dataset,
            int maxIterations,
            int updateInterval, int numberOfClusters,
            boolean tocontinue, ApplicationTemplate applicationTemplate) {
        super(numberOfClusters);
        this.applicationTemplate = applicationTemplate;
        this.dataset = dataset;
        this.maxIterations = maxIterations;
        this.updateInterval = updateInterval;
        this.tocontinue = new AtomicBoolean(tocontinue);
    }

    @Override
    public void run() {
        generateRandomLabels();
        for (int i = 1; i <= maxIterations; i++) {
            ((AppUI) applicationTemplate.getUIComponent()).setIteration(i);
            if (tocontinue()) {
                try {
                    assignRandomLabels(i - 1);
                } catch (Exception e) {
                }
                if (i == maxIterations || i == dataset.getLabels().size()) {
                    Platform.runLater(() -> {
                        toChartData();
                    });
                    Thread.currentThread().interrupt();
//                    System.out.printf("Iteration number %d: ", i);
//                    System.out.println();
                    ((AppUI) applicationTemplate.getUIComponent()).disableButtons(false);
                    ((AppUI) applicationTemplate.getUIComponent()).scrnshotButton.setVisible(true);
                    ((AppUI) applicationTemplate.getUIComponent()).algorithmOnFinish();
                    Platform.runLater(() -> {
                        ((AppUI) applicationTemplate.getUIComponent()).displayIterationLabel.setText(((AppUI) applicationTemplate.getUIComponent()).displayIterationLabel.getText().concat(" (Finished)"));
                    });
                    break;
                }
                if (i % updateInterval == 0) {
//                    System.out.printf("Iteration number %d: ", i);
//                    System.out.println();
                    Platform.runLater(() -> {
                        toChartData();
                    });
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ex) {
                        return;
                    }
                }
                if (i > maxIterations * .6 && RAND.nextDouble() < 0.05) {
                    Platform.runLater(() -> {
                        toChartData();
                    });
                    ((AppUI) applicationTemplate.getUIComponent()).algorithmOnFinish();
//                    System.out.printf("Iteration number %d: ", i);
//                    System.out.println();
//                    System.out.print("Break");
                    Platform.runLater(() -> {
                        ((AppUI) applicationTemplate.getUIComponent()).displayIterationLabel.setText(((AppUI) applicationTemplate.getUIComponent()).displayIterationLabel.getText().concat(" (Self-Stop)"));
                    });
                    ((AppUI) applicationTemplate.getUIComponent()).disableButtons(false);
                    ((AppUI) applicationTemplate.getUIComponent()).scrnshotButton.setVisible(true);
                    break;
                }
            } else {
                try {
                    assignRandomLabels(i - 1);
                } catch (Exception e) {
                }
                if (i == maxIterations || i == dataset.getLabels().size()) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                    }
                    Platform.runLater(() -> {
                        toChartData();
                    });
                    Thread.currentThread().interrupt();
//                    System.out.printf("Iteration number %d: ", i);
//                    System.out.println();
                    ((AppUI) applicationTemplate.getUIComponent()).disableButtons(false);
                    ((AppUI) applicationTemplate.getUIComponent()).scrnshotButton.setVisible(true);
                    ((AppUI) applicationTemplate.getUIComponent()).algorithmOnFinish();
                    Platform.runLater(() -> {
                        ((AppUI) applicationTemplate.getUIComponent()).displayIterationLabel.setText(((AppUI) applicationTemplate.getUIComponent()).displayIterationLabel.getText().concat(" (Finished)"));
                    });
                    break;
                }
                if (i % updateInterval == 0) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                    }
//                    System.out.printf("Iteration number %d: ", i);
//                    System.out.println();
                    Platform.runLater(() -> {
                        toChartData();
                    });
                    try {
                        ((AppUI) applicationTemplate.getUIComponent()).updateRun.setDisable(false);
                        Platform.runLater(() -> {
                            ((AppUI) applicationTemplate.getUIComponent()).updateRun.setText(applicationTemplate.manager.getPropertyValue(AppPropertyTypes.UPDATE_BUTTON_RESUME.name()));
                            ((AppUI) applicationTemplate.getUIComponent()).scrnshotButton.setVisible(true);
                        });
                        synchronized (Thread.currentThread()) {
                            Thread.currentThread().wait();
                        }
                    } catch (InterruptedException ex) {
                        return;
                    }
                }
                if (i > maxIterations * .6 && RAND.nextDouble() < 0.05) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                    }
                    Platform.runLater(() -> {
                        toChartData();
                    });
                    ((AppUI) applicationTemplate.getUIComponent()).algorithmOnFinish();
//                    System.out.printf("Iteration number %d: ", i);
//                    System.out.println();
//                    System.out.print("Break");
                    Platform.runLater(() -> {
                        ((AppUI) applicationTemplate.getUIComponent()).displayIterationLabel.setText(((AppUI) applicationTemplate.getUIComponent()).displayIterationLabel.getText().concat(" (Self-Stop)"));
                    });
                    ((AppUI) applicationTemplate.getUIComponent()).disableButtons(false);
                    ((AppUI) applicationTemplate.getUIComponent()).scrnshotButton.setVisible(true);
                    break;
                }
            }
        }
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

    public void generateRandomLabels() {
        labels = new String[getNumberOfClusters()];
        for (int i = 0; i < labels.length; i++) {
            labels[i] = "label" + (i + 1);
        }
        instanceNames = new ArrayList<>(dataset.getLabels().keySet());
    }

    public void assignRandomLabels(int i) {
        dataset.getLabels().put(instanceNames.get(i), labels[RAND.nextInt(labels.length)]);
    }

    @Override
    public void setMaxIterations(int iterations) {
        this.maxIterations = iterations;
    }

    @Override
    public void setUpdateInterval(int interval) {
        this.updateInterval = interval;
    }

    public void setToContinue(boolean tocontinue) {
        this.tocontinue = new AtomicBoolean(tocontinue);
    }

    public void setNumClusters(int i) {
        super.setNumberOfClusters(i);
    }
}
