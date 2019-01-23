package algorithms;

import algorithm.Classifier;
import algorithm.DataSet;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import dataprocessors.AppData;
import javafx.application.Platform;
import javafx.scene.chart.XYChart;
import javafx.scene.shape.Rectangle;
import settings.AppPropertyTypes;
import ui.AppUI;
import vilij.templates.ApplicationTemplate;

/**
 * @author Andy Cen
 */
public class RandomClassifier extends Classifier {

    private ApplicationTemplate applicationTemplate;

    private static final Random RAND = new Random();

    @SuppressWarnings("FieldCanBeLocal")
    private DataSet dataset;

    private XYChart.Series<Number, Number> line;
    private int maxIterations;
    private int updateInterval;
    private int numClusters; // only for clustering algorithms

    private AtomicBoolean tocontinue;

    private List<Integer> output;

    // Unused
    public void setNumClusters(int numClusters) {
        this.numClusters = numClusters;
    }

    public int getNumClusters() {
        return this.numClusters;
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

    public void setDataSet(DataSet dataset) {
        this.dataset = dataset;
    }

    public RandomClassifier(DataSet dataset,
            int maxIterations,
            int updateInterval,
            boolean tocontinue, ApplicationTemplate applicationTemplate) {
        this.applicationTemplate = applicationTemplate;
        this.dataset = dataset;
        this.maxIterations = maxIterations;
        this.updateInterval = updateInterval;
        this.tocontinue = new AtomicBoolean(tocontinue);
    }

    @Override
    public void run() {
        for (int i = 1; i <= maxIterations; i++) {
            ((AppUI) applicationTemplate.getUIComponent()).setIteration(i);
            //int xCoefficient =  new Long(-1 * Math.round((2 * RAND.nextDouble() - 1) * 10)).intValue();
            int xCoefficient = RAND.nextInt(11) * (-1);
            int yCoefficient = 10;
            int constant = RAND.nextInt(11);

            output = Arrays.asList(xCoefficient, yCoefficient, constant);

            if (tocontinue()) {
                if (i == maxIterations) {
                    Platform.runLater(() -> {
                        createLine(xCoefficient, yCoefficient, constant);
                    });
                    Thread.currentThread().interrupt();
                    //System.out.printf("Iteration number %d: ", i);
                    //flush();
                    ((AppUI) applicationTemplate.getUIComponent()).disableButtons(false);
                    ((AppUI) applicationTemplate.getUIComponent()).scrnshotButton.setVisible(true);
                    ((AppUI) applicationTemplate.getUIComponent()).algorithmOnFinish();
                    Platform.runLater(() -> {
                        ((AppUI) applicationTemplate.getUIComponent()).displayIterationLabel.setText(((AppUI) applicationTemplate.getUIComponent()).displayIterationLabel.getText().concat(" (Finished)"));
                    });
                }
                if (i % updateInterval == 0) {
                    Platform.runLater(() -> {
                        createLine(xCoefficient, yCoefficient, constant);
                    });
                    //System.out.printf("Iteration number %d: ", i);
                    //flush();
                    try {
                        Thread.sleep(750);
                    } catch (InterruptedException ex) {
                        return;
                    }
                }
                if (i > maxIterations * .6 && RAND.nextDouble() < 0.05) {
                    Platform.runLater(() -> {
                        createLine(xCoefficient, yCoefficient, constant);
                    });
                    ((AppUI) applicationTemplate.getUIComponent()).algorithmOnFinish();
                    //System.out.printf("Iteration number %d: ", i);
                    //flush();
                    //System.out.println("Break");
                    Platform.runLater(() -> {
                        ((AppUI) applicationTemplate.getUIComponent()).displayIterationLabel.setText(((AppUI) applicationTemplate.getUIComponent()).displayIterationLabel.getText().concat(" (Self-Stop)"));
                    });
                    ((AppUI) applicationTemplate.getUIComponent()).disableButtons(false);
                    ((AppUI) applicationTemplate.getUIComponent()).scrnshotButton.setVisible(true);
                    break;
                }
            } else {
                if (i == maxIterations) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ex) {
                    }
                    Platform.runLater(() -> {
                        createLine(xCoefficient, yCoefficient, constant);
                    });
                    Thread.currentThread().interrupt();
                    //System.out.printf("Iteration number %d: ", i);
                    //flush();
                    ((AppUI) applicationTemplate.getUIComponent()).disableButtons(false);
                    ((AppUI) applicationTemplate.getUIComponent()).scrnshotButton.setVisible(true);
                    ((AppUI) applicationTemplate.getUIComponent()).algorithmOnFinish();
                    Platform.runLater(() -> {
                        ((AppUI) applicationTemplate.getUIComponent()).displayIterationLabel.setText(((AppUI) applicationTemplate.getUIComponent()).displayIterationLabel.getText().concat(" (Finished)"));
                    });
                }
                if (i % updateInterval == 0) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ex) {
                    }
                    Platform.runLater(() -> {
                        createLine(xCoefficient, yCoefficient, constant);
                    });
                    //System.out.printf("Iteration number %d: ", i);
                    //flush();
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
                    } catch (InterruptedException ex) {
                    }
                    Platform.runLater(() -> {
                        createLine(xCoefficient, yCoefficient, constant);
                    });
                    ((AppUI) applicationTemplate.getUIComponent()).algorithmOnFinish();
                    //System.out.printf("Iteration number %d: ", i);
                    //flush();
                    //System.out.println("Break");
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

    public void createLine(int xCoefficient, int yCoefficient, int constant) {
        try {
            ((AppUI) applicationTemplate.getUIComponent()).getChart().getData().remove(line);
        } catch (Exception e) {
        }

        double xMin = ((AppData) applicationTemplate.getDataComponent()).getProcessor().getXMin();
        double xMax = ((AppData) applicationTemplate.getDataComponent()).getProcessor().getXMax();
        // Ax + By + C = 0
        // y = ((-C - Ax)/B)
        // C = constant, A = xCoefficient, B = yCoefficient
//        if (xCoefficient > 0) {
//            xCoefficient *= -1;
//        }
        double yMinOfX = ((constant * (-1)) - (xCoefficient * xMin)) / yCoefficient;
        double yMaxOfX = ((constant * (-1)) - (xCoefficient * xMax)) / yCoefficient;
        line = new XYChart.Series();
        line.setName(applicationTemplate.manager.getPropertyValue(AppPropertyTypes.CLASSIFICATION.name()));
        Rectangle rect1 = new Rectangle(0, 0);
        Rectangle rect2 = new Rectangle(0, 0);
        XYChart.Data data1 = new XYChart.Data(xMin, yMinOfX);
        data1.setNode(rect1);
        XYChart.Data data2 = new XYChart.Data(xMax, yMaxOfX);
        data2.setNode(rect2);
        line.getData().add(data1);
        line.getData().add(data2);

        ((AppUI) applicationTemplate.getUIComponent()).getChart().getData().add(line);
    }

    // For internal viewing only
    protected void flush() {
        System.out.printf("%d\t%d\t%d%n", output.get(0), output.get(1), output.get(2));
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
}
