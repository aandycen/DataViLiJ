package dataprocessors;

import javafx.geometry.Point2D;
import javafx.scene.chart.XYChart;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;
import javafx.scene.control.Tooltip;
import settings.AppPropertyTypes;
import static vilij.propertymanager.PropertyManager.getManager;

/**
 * The data files used by this data visualization applications follow a
 * tab-separated format, where each data point is named, labeled, and has a
 * specific location in the 2-dimensional X-Y plane. This class handles the
 * parsing and processing of such data. It also handles exporting the data to a
 * 2-D plot.
 * <p>
 * A sample file in this format has been provided in the application's
 * <code>resources/data</code> folder.
 *
 * @author Andy Cen
 * @see XYChart
 */
public final class TSDProcessor {

    public static class InvalidDataNameException extends Exception {

        private static final String NAME_ERROR_MSG = "All data instance names must start with the @ character.";

        public InvalidDataNameException(String name) {
            super(String.format("Invalid name '%s'." + NAME_ERROR_MSG, name));
        }
    }

    private Map<String, String> dataLabels;
    private Map<String, Point2D> dataPoints;
    private ArrayList<String> names = new ArrayList();
    
    public ArrayList<String> getUniqueInstances() {
        ArrayList<String> uniqueInstances = new ArrayList();

        for (Map.Entry<String, String> pair : dataLabels.entrySet()) {
            if (!uniqueInstances.contains(pair.getKey().substring(1))) {
                uniqueInstances.add(pair.getKey().substring(1)); // ignores '@'
            }
        }
        return uniqueInstances;
    }
    
    public ArrayList<String> getUniqueLabels() {
        ArrayList<String> uniqueLabels = new ArrayList();
        for (Map.Entry<String, String> pair : dataLabels.entrySet()) {
            if (!uniqueLabels.contains(pair.getValue())) {
                uniqueLabels.add(pair.getValue());
            }
        }
        return uniqueLabels;
    }
    
    public TSDProcessor() {
        dataLabels = new HashMap<>();
        dataPoints = new HashMap<>();
    }

    public double getXMin() {
        double xMin = Double.POSITIVE_INFINITY;
        for (Point2D data : dataPoints.values()) {
            if (data.getX() < xMin) {
                xMin = (double) data.getX();
            }
        }
        return xMin;
    }

    public double getXMax() {
        double xMax = Double.NEGATIVE_INFINITY;
        for (Point2D data : dataPoints.values()) {
            if (data.getX() > xMax) {
                xMax = (double) data.getX();
            }
        }
        return xMax;
    }
    
   public double getYMin() {
        double yMin = Double.POSITIVE_INFINITY;
        for (Point2D data : dataPoints.values()) {
            if (data.getY() < yMin) {
                yMin = (double) data.getY();
            }
        }
        return yMin;
    }

    public double getYMax() {
        double yMax = Double.NEGATIVE_INFINITY;
        for (Point2D data : dataPoints.values()) {
            if (data.getY() > yMax) {
                yMax = (double) data.getY();
            }
        }
        return yMax;
    }
    /**
     * Processes the data and populated two {@link Map} objects with the data.
     *
     * @param tsdString the input data provided as a single {@link String}
     * @throws Exception if the input string does not follow the
     * <code>.tsd</code> data format
     */
    public void processString(String tsdString) throws Exception {
        names.clear();
        AtomicBoolean hadAnError = new AtomicBoolean(false);
        StringBuilder errorMessage = new StringBuilder();
        Stream.of(tsdString.split("\n"))
                .map(line -> Arrays.asList(line.split("\t")))
                .forEach(list -> {
                    try {
                        String name = checkedname(list.get(0));
                        String label = list.get(1);
                        String[] pair = list.get(2).split(",");
                        Point2D point = new Point2D(Double.parseDouble(pair[0]), Double.parseDouble(pair[1]));
                        dataLabels.put(name, label);
                        dataPoints.put(name, point);
                        names.add(name + " " + point.toString());
                    } catch (Exception e) {
                        errorMessage.setLength(0);
                        errorMessage.append(e.getClass().getSimpleName()).append(": ").append(e.getMessage());
                        hadAnError.set(true);
                    }
                });
        if (errorMessage.length() > 0) {
            throw new Exception(errorMessage.toString());
        }
    }

    /**
     * Exports the data to the specified 2-D chart.
     *
     * @param chart the specified chart
     */
    void toChartData(XYChart<Number, Number> chart) {
        Set<String> labels = new HashSet<>(dataLabels.values());
        for (String label : labels) {
            XYChart.Series<Number, Number> series = new XYChart.Series<>();    
            series.setName(label);
            dataLabels.entrySet().stream().filter(entry -> entry.getValue().equals(label)).forEach(entry -> {
                Point2D point = dataPoints.get(entry.getKey());
                String dataPointName = null;
                for (String name : names) {
                    if (name.contains(point.toString())) {
                        dataPointName = name.substring(0, name.indexOf(" "));
                    }
                }
                series.getData().add(new XYChart.Data<>(point.getX(), point.getY(), dataPointName));
            });
            chart.getData().add(series);
            series.getNode().lookup(getManager().getPropertyValue(AppPropertyTypes.LINE_LOOKUP.name()));
            series.getNode().setStyle(getManager().getPropertyValue(AppPropertyTypes.INVIS_LINE.name()));
            for (XYChart.Series<Number, Number> serie : chart.getData()) {
                for (XYChart.Data<Number, Number> data : serie.getData()) {
                    data.getNode().setStyle(getManager().getPropertyValue(AppPropertyTypes.CURSOR_STYLE.name()));
                    Tooltip tooltip = new Tooltip(data.getExtraValue().toString());
                    Tooltip.install(data.getNode(), tooltip);
                }
            }
        }
    }

    void clear() {
        dataPoints.clear();
        dataLabels.clear();
    }

    private String checkedname(String name) throws InvalidDataNameException {
        if (!name.startsWith("@")) {
            throw new InvalidDataNameException(name);
        }
        return name;
    }
}
