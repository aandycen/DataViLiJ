package ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import settings.AppPropertyTypes;
import actions.AppActions;
import algorithm.Algorithm;
import algorithm.Classifier;
import algorithm.Clusterer;
import algorithm.DataSet;
import algorithms.RandomClassifier;
import algorithms.RandomClusterer;
import algorithms.KMeansClusterer;
import dataprocessors.AppData;
import java.io.File;
import vilij.propertymanager.PropertyManager;
import vilij.templates.ApplicationTemplate;
import vilij.templates.UITemplate;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import static java.io.File.separator;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;
import vilij.components.ConfirmationDialog;
import vilij.components.ErrorDialog;
import static vilij.settings.PropertyTypes.GUI_RESOURCE_PATH;
import static vilij.settings.PropertyTypes.ICONS_RESOURCE_PATH;
import static vilij.settings.PropertyTypes.SAVE_ERROR_MSG;
import static vilij.settings.PropertyTypes.SAVE_ERROR_TITLE;
import static vilij.settings.PropertyTypes.SAVE_WORK_TITLE;

/**
 * This is the application's user interface implementation.
 *
 * @author Andy Cen
 */
public final class AppUI extends UITemplate {

    /**
     * The application to which this class of actions belongs.
     */
    ApplicationTemplate applicationTemplate;

    @SuppressWarnings("FieldCanBeLocal")
    public Button scrnshotButton;
    private ArrayList<String> listOfAlgorithms;
    private LineChart<Number, Number> chart;
    private Button displayButton;
    private TextArea textArea;       // main text area for new data input
    private boolean hasNewText;      // any new data since last display?
    private ArrayList<String> lines; // excess data 
    private ArrayList<Object> algorithms;
    private HBox split;
    private HBox leftLabelBox;
    private HBox rightLabelBox;
    private NumberAxis xAxis;
    private NumberAxis yAxis;
    private VBox left;
    private VBox right;
    private Label leftLabel;
    private Label rightLabel;
    private PropertyManager manager;
    private ErrorDialog errorDialog;
    private ConfirmationDialog confirmationDialog;
    private String currentText;     // restricts multiple display clicks for same data
    private String errorLine;
    private CheckBox readOnly;
    private Boolean checked;       // readOnly button listener
    private Path sourceOfData;
    private ToggleButton doneBtn;
    private ToggleButton editBtn;
    private HBox toggleButtonSpace;
    private ComboBox algoBox;
    private String algoType;
    private Label iterationLabel;
    private TextArea iterationField;
    private Label intervalLabel;
    private TextArea intervalField;
    private Label continuousRunLabel;
    private CheckBox continuousRun;
    private Button doneConfig;
    private Button backBtn;
    private String currAlgoType;
    private Button run;
    private Button cancelRun;      // cancels continuous algorithm
    public Button updateRun;       // non-continuous runs
    private TextArea numberOfClusters;
    private Thread thread;
    public Label displayIterationLabel;
    private int algorithmNumber;
    private boolean loadError;     // check if user prompt load window but clicked "X"
    private int numLabels;         // number of unique labels
    RandomClassifier classifierAlgo;
    RandomClusterer clustererAlgo;
    KMeansClusterer kMeansAlgo;
    private ArrayList<Class> Clusterer;
    private ArrayList<Class> Classification;
    private ArrayList<Algorithm> AlgorithmR;

    public boolean haveNewText(String currentText, String textAreaText) {
        if (currentText.equals(textAreaText) && !chart.getData().isEmpty()) {
            return false;
        }
        return true;
    }

    public void setCurrentText(String str) {
        currentText = str;
    }

    public void addOverflowLine(String str) {
        this.lines.add(str);
    }

    public ArrayList getOverflowLines() {
        return this.lines;
    }

    public String removeOverflowLineToTextArea() {
        return this.lines.remove(0);
    }

    public String extractLinesToSaveFile() {
        String str = "";
        while (!lines.isEmpty()) {
            str += lines.remove(0) + "\n";
        }
        return str;
    }

    public void infoSave() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, manager.getPropertyValue(AppPropertyTypes.SAVED.name()), ButtonType.OK);
        alert.setTitle(manager.getPropertyValue(SAVE_WORK_TITLE.name()));
        alert.showAndWait();
    }

    public LineChart<Number, Number> getChart() {
        return chart;
    }

    public String getTextContent() {
        return textArea.getText();
    }

    public void setTextContent(String str) {
        textArea.setText(str);
    }

    public boolean isValidText(String txt) {
        String[] text = txt.split("\n");
        ArrayList<String> names = new ArrayList<String>();
        for (int i = 0; i < text.length; i++) {
            String[] line = text[i].split("\t");
            if (!line[0].startsWith("@") || line.length != 3) {
                errorLine = manager.getPropertyValue(AppPropertyTypes.INVALID_DATA_MSG.name()) + (i + 1);
                errorDialog.show(manager.getPropertyValue(AppPropertyTypes.INVALID_INPUT_TITLE.name()), errorLine);
                ((AppActions) applicationTemplate.getActionComponent()).resetDataFilePath();
                return false;
            }
            if (line[1].isEmpty()) {
                errorDialog.show(manager.getPropertyValue(AppPropertyTypes.INVALID_LABEL_TITLE.name()), manager.getPropertyValue(AppPropertyTypes.INVALID_LABEL.name()));
                return false;
            }
            if (names.contains(line[0])) {
                errorDialog.show(manager.getPropertyValue(AppPropertyTypes.INVALID_NAME.name()), manager.getPropertyValue(AppPropertyTypes.DUPLICATE_NAME.name()) + line[0]);
                return false;
            } else {
                names.add(line[0]);
            }
            try {
                String[] pair = line[2].split(",");
                Double p1 = Double.parseDouble(pair[0]);
                Double p2 = Double.parseDouble(pair[1]);
            } catch (Exception e) {
                errorDialog.show(manager.getPropertyValue(AppPropertyTypes.INVALID_INPUT_TITLE.name()), manager.getPropertyValue(AppPropertyTypes.INVALID_NUM_DATA.name()));
                return false;
            }
        }
        return true;
    }

    public AppUI(Stage primaryStage, ApplicationTemplate applicationTemplate) {
        super(primaryStage, applicationTemplate);
        this.applicationTemplate = applicationTemplate;
    }

    @Override
    protected void setResourcePaths(ApplicationTemplate applicationTemplate) {
        super.setResourcePaths(applicationTemplate);
    }

    @Override
    protected void setToolBar(ApplicationTemplate applicationTemplate) {
        super.setToolBar(applicationTemplate);
        manager = applicationTemplate.manager;

        String scrnPath = separator + String.join(separator, manager.getPropertyValue(GUI_RESOURCE_PATH.name()), manager.getPropertyValue(ICONS_RESOURCE_PATH.name()));
        Image img = new Image(String.join(separator, scrnPath, manager.getPropertyValue(AppPropertyTypes.SCREENSHOT_ICON.name())));

        scrnshotButton = new Button();
        scrnshotButton.setDisable(true);
        scrnshotButton.setTooltip(new Tooltip(manager.getPropertyValue(AppPropertyTypes.SCREENSHOT_TOOLTIP.name())));
        scrnshotButton.setGraphic(new ImageView(img));

        super.toolBar.getItems().add(scrnshotButton);
    }

    @Override
    protected void setToolbarHandlers(ApplicationTemplate applicationTemplate) {
        scrnshotButton.setOnAction(e -> {
            try {
                ((AppActions) applicationTemplate.getActionComponent()).handleScreenshotRequest();
            } catch (IOException ex) {
                ErrorDialog errorDialog = ErrorDialog.getDialog();
                errorDialog.show(manager.getPropertyValue(SAVE_ERROR_TITLE.name()), manager.getPropertyValue(SAVE_ERROR_MSG.name()));
            }

        });

        newButton.setOnAction(e -> {
            try {
                if (!textArea.getText().isEmpty() && ((AppActions) applicationTemplate.getActionComponent()).existingFile()) { // Data loaded from file
                    getOverflowLines().clear();
                    clear();
                    ((AppData) applicationTemplate.getDataComponent()).clear();
                    ((AppActions) applicationTemplate.getActionComponent()).resetDataFilePath();
                    addToggles();
                    if (left.getChildren().size() > 3) {
                        left.getChildren().remove(3, left.getChildren().size());
                    }
                    editBtn.fire();
                } else if (textArea.getText().isEmpty()) {
                    errorDialog.show(manager.getPropertyValue(AppPropertyTypes.CREATE_DATA_ERROR_ONE.name()), manager.getPropertyValue(AppPropertyTypes.CREATE_DATA_ERROR_TWO.name()));
                } else { // Data is manually entered
                    if (((AppActions) applicationTemplate.getActionComponent()).promptToSave()) {
                        if (isValidText(textArea.getText())) {
                            ((AppActions) applicationTemplate.getActionComponent()).handleNewRequest();
                            ((AppData) applicationTemplate.getDataComponent()).clear();
                            getOverflowLines().clear();
                            clear();
                            editBtn.fire();
                        }
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
         
//            if (!textArea.getText().isEmpty()) {
//                try {
//                    if (((AppActions) applicationTemplate.getActionComponent()).promptToSave()) {
//                        if (isValidText(getTextContent())) {
//                            ((AppActions) applicationTemplate.getActionComponent()).handleNewRequest();
//                            ((AppActions) applicationTemplate.getActionComponent()).resetDataFilePath();
//                            ((AppData) applicationTemplate.getDataComponent()).clear();
//                            clear();
//                        }
//                    }
//                } catch (IOException ex) {
//                    errorDialog.show(manager.getPropertyValue(SAVE_ERROR_TITLE.name()), manager.getPropertyValue(SAVE_ERROR_MSG.name()));
//                }
//            }        
        });

        saveButton.setOnAction(e -> {
            if ((!textArea.getText().isEmpty()) && (isValidText(getTextContent()))) {
                if (((AppActions) applicationTemplate.getActionComponent()).existingFile()) {
                    Path p = ((AppActions) applicationTemplate.getActionComponent()).getDataFilePath();
                    ((AppData) applicationTemplate.getDataComponent()).saveData(p);
                } else {
                    ((AppActions) applicationTemplate.getActionComponent()).handleSaveRequest();
                }
                if (((AppActions) applicationTemplate.getActionComponent()).existingFile()) {
                    infoSave();
                    saveButton.setDisable(true);
                }
            } else {
                saveButton.setDisable(true);
            }
        });

        loadButton.setOnAction(e -> {
            String previousLoadedData = textArea.getText();
            ((AppData) applicationTemplate.getDataComponent()).clear();
            Path tempPath = ((AppActions) applicationTemplate.getActionComponent()).getDataFilePath();
            lines.clear();
            clear();
            ((AppActions) applicationTemplate.getActionComponent()).handleLoadRequest();
            saveButton.setDisable(true);
            if (textArea.getText().isEmpty() && !previousLoadedData.isEmpty()) { // Loading data was attempted but user may have cancelled
                loadError = true;
                try {
                    if (!((AppActions) applicationTemplate.getActionComponent()).existingFile()) {
                        ((AppActions) applicationTemplate.getActionComponent()).setDataFilePath(tempPath);
                        ((AppData) applicationTemplate.getDataComponent()).loadData(tempPath); // Restore previous data (if there was any) and its info
                    }
                } catch (Exception ex) {
                }
                if (textArea.getText().isEmpty()) { // No previous loaded data, restore any previous data user may have left behind unsaved
                    textArea.setText(previousLoadedData);
                    editBtn.fire();
                }
                saveButton.setDisable(true);
                loadError = false;
            }
        });

        /*
        Exits Application
         */
        exitButton.setOnAction(e -> {
            if (thread != null && thread.isAlive()) {
                if (thread.isAlive()) {
                    confirmationDialog.show(manager.getPropertyValue(AppPropertyTypes.EXIT_WHILE_RUNNING_TITLE.name()), manager.getPropertyValue(AppPropertyTypes.EXIT_WHILE_RUNNING_WARNING.name()));
                }
                if (confirmationDialog.getSelectedOption().name().equals(manager.getPropertyValue(AppPropertyTypes.CONFIRMATION_YES.name()))) {
                    Platform.exit();
                }
            } else if (!((AppActions) applicationTemplate.getActionComponent()).existingFile()) {
                try {
                    boolean bool = false;
                    if (!textArea.getText().isEmpty()) {
                        bool = ((AppActions) applicationTemplate.getActionComponent()).promptToSave();
                    } else if (!bool && textArea.getText().isEmpty()) {
                        ((AppActions) applicationTemplate.getActionComponent()).handleExitRequest();
                    }
                    if (bool) {
                        if (isValidText(textArea.getText())) {
                            ((AppActions) applicationTemplate.getActionComponent()).handleNewRequest();
                            ((AppActions) applicationTemplate.getActionComponent()).handleExitRequest();
                        }
                    }
                } catch (IOException ex) {
                    errorDialog.show(manager.getPropertyValue(SAVE_ERROR_TITLE.name()), manager.getPropertyValue(SAVE_ERROR_MSG.name()));
                }
            } else {
                ((AppActions) applicationTemplate.getActionComponent()).handleExitRequest();
            }
        });

    }

    @Override
    public void initialize() {
        appPane.getStylesheets().add(manager.getPropertyValue(AppPropertyTypes.DATA_VILIJ_CSS.name()));
        newButton.setDisable(false);
        manager = applicationTemplate.manager;
        currentText = manager.getPropertyValue(AppPropertyTypes.EMPTY_SPACE.name());
        errorLine = manager.getPropertyValue(AppPropertyTypes.EMPTY_SPACE.name());
        split = new HBox();
        readOnly = new CheckBox(manager.getPropertyValue(AppPropertyTypes.READ_ONLY.name())); // Doesn't really work for some reason
        leftLabelBox = new HBox();
        rightLabelBox = new HBox();
        left = new VBox();
        left.setPadding(new Insets(10));
        left.setSpacing(10);
        right = new VBox();
        right.setPadding(new Insets(5));
        right.setSpacing(10);
        leftLabel = new Label(manager.getPropertyValue(AppPropertyTypes.DATA_TEXT_AREA.name()));
        rightLabel = new Label(manager.getPropertyValue(AppPropertyTypes.DATA_VISUALIZE.name()));
        textArea = new TextArea();
        textArea.setPrefWidth(340);
        textArea.setPrefHeight(220);
        textArea.setWrapText(true);
        textArea.setMinSize(340, 220);
        textArea.setMaxSize(340, 220);
        displayButton = new Button(manager.getPropertyValue(AppPropertyTypes.DISPLAY_NAME.name()));
        xAxis = new NumberAxis();
        yAxis = new NumberAxis();
        chart = new LineChart(xAxis, yAxis);
        chart.setMaxSize(650, 550);
        chart.setMinSize(650, 500);
        hasNewText = true;
        errorDialog = ErrorDialog.getDialog();
        confirmationDialog = ConfirmationDialog.getDialog();
        lines = new ArrayList();
        checked = false;
        sourceOfData = null;
        algoType = "";
        doneBtn = new ToggleButton(manager.getPropertyValue(AppPropertyTypes.DONE_TOGGLE_NAME.name()));
        editBtn = new ToggleButton(manager.getPropertyValue(AppPropertyTypes.EDIT_TOGGLE_NAME.name()));
        toggleButtonSpace = new HBox();
        toggleButtonSpace.setSpacing(10);
        algoBox = new ComboBox<>();
        algoBox.setPromptText(manager.getPropertyValue(AppPropertyTypes.SELECTION_BOX_TITLE.name()));
        algoBox.getItems().addAll(manager.getPropertyValue(AppPropertyTypes.CLASSIFICATION.name()), manager.getPropertyValue(AppPropertyTypes.CLUSTERING.name()));
        doneConfig = new Button(manager.getPropertyValue(AppPropertyTypes.DONE_TOGGLE_NAME.name()));
        backBtn = new Button(manager.getPropertyValue(AppPropertyTypes.BACK_BUTTON_NAME.name()));
        algorithms = new ArrayList();
        loadError = false;
        RandomClassifier classifier = new RandomClassifier(new DataSet(), -1, -1, false, applicationTemplate);
        RandomClusterer clustering = new RandomClusterer(new DataSet(), -1, -1, -1, false, applicationTemplate);
        KMeansClusterer kMeansCluster = new KMeansClusterer(new DataSet(), -1, -1, -1, false, applicationTemplate);
        algorithms.add(classifier);
        algorithms.add(clustering);
        algorithms.add(kMeansCluster);
        classifierAlgo = (RandomClassifier) algorithms.get(0);
        clustererAlgo = (RandomClusterer) algorithms.get(1);
        kMeansAlgo = (KMeansClusterer) algorithms.get(2);
        algorithmNumber = -1;
        currAlgoType = "";
        run = new Button();
        displayIterationLabel = new Label(manager.getPropertyValue(AppPropertyTypes.ALGO_ITERATION_LABEL.name()));
        cancelRun = new Button(manager.getPropertyValue(AppPropertyTypes.CANCEL_RUN.name()));
        updateRun = new Button(manager.getPropertyValue(AppPropertyTypes.UPDATE_BUTTON_RUNNING.name()));
        listOfAlgorithms = new ArrayList<>();
        File algorithmFolder = new File(manager.getPropertyValue(AppPropertyTypes.ALGORITHMS_FOLDER.name()));
        File[] a = algorithmFolder.listFiles();
        for (File algorithm : a) {
            if (algorithm.isFile()) {
                listOfAlgorithms.add(algorithm.getName().substring(0, algorithm.getName().lastIndexOf(".")));
            }
        }
        getAlgorithms();
        layout();
        setWorkspaceActions();
        editBtn.setSelected(true);
    }

    private void getAlgorithms() {
        File algorithmFolder = new File(manager.getPropertyValue(AppPropertyTypes.ALGORITHMS_FOLDER.name()));
        try {
            ClassLoader classLoader = new URLClassLoader(new URL[]{algorithmFolder.toURI().toURL()});
            File[] files = algorithmFolder.listFiles();
            Clusterer = new ArrayList<>();
            Classification = new ArrayList<>();
            AlgorithmR = new ArrayList<>();
            for (File file : files) {
                String className = "algorithms." + file.getName().substring(0, file.getName().lastIndexOf("."));
                Class<?> cls = classLoader.loadClass(className);
                if (algorithm.Clusterer.class.isAssignableFrom(cls)) {
                    //System.out.println(file.getName().substring(0, file.getName().lastIndexOf(".")) + " Added To List of Clusterers");
                    Clusterer.add(cls);
                } else if (algorithm.Classifier.class.isAssignableFrom(cls)) {
                    //System.out.println(file.getName().substring(0, file.getName().lastIndexOf(".")) + " Added To List of Classification");
                    Classification.add(cls);
                }
            }
        } catch (MalformedURLException | ClassNotFoundException e) {
        }
        for (Class Classifier : Classification) {
            Class<?> cls = Classifier;
            try {
                Constructor<?> ctor = cls.getConstructor(DataSet.class, int.class, int.class, boolean.class, ApplicationTemplate.class);
                Algorithm a = (Classifier) ctor.newInstance(new DataSet(), -1, -1, false, applicationTemplate);
                AlgorithmR.add(a);
                //System.out.println("New Instance of Class Classifier");
            } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            }
        }
        for (Class Clust : Clusterer) {
            Class<?> cls = Clust;
            try {
                Constructor<?> ctor = cls.getConstructor(DataSet.class, int.class, int.class, int.class, boolean.class, ApplicationTemplate.class);
                Algorithm a = (Clusterer) ctor.newInstance(new DataSet(), -1, -1, -1, false, applicationTemplate);
                AlgorithmR.add(a);
                //System.out.println("New Instance of Class Clusterer");
            } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            }
        }
    }

    public boolean loadError() {
        return this.loadError;
    }

    @Override
    public void clear() {
        textArea.clear();
        lines.clear();
        chart.getData().clear();
    }

    private void layout() {
        leftLabelBox.getChildren().add(leftLabel);
        leftLabelBox.setAlignment(Pos.CENTER);
        rightLabelBox.getChildren().add(rightLabel);
        rightLabelBox.setAlignment(Pos.CENTER);
        toggleButtonSpace.getChildren().addAll(doneBtn, editBtn);
        left.getChildren().addAll(leftLabelBox, textArea, toggleButtonSpace);
        right.getChildren().addAll(rightLabelBox, chart);
        split.getChildren().addAll(left, right);
        appPane.getChildren().add(split);

    }

    private void setWorkspaceActions() {
        scrnshotButton.disableProperty().bind(Bindings.isEmpty(chart.getData()));

        doneBtn.selectedProperty().addListener((obs, oldValue, newValue) -> saveButton.setDisable(!newValue));

        algoBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                algoType = newValue;
                if (newValue != null) {
                    if (algoType.equals(manager.getPropertyValue(AppPropertyTypes.CLASSIFICATION.name()))) {
                        left.getChildren().add(4, createClassificationAlgorithmMenu());
                        currAlgoType = manager.getPropertyValue(AppPropertyTypes.CLASSIFICATION.name());
                    } else {
                        left.getChildren().add(4, createClusteringAlgorithmMenu());
                        currAlgoType = manager.getPropertyValue(AppPropertyTypes.CLUSTERING.name());
                    }
                    left.getChildren().remove(algoBox);
                }
            }
        });

        backBtn.setOnAction(e -> {
            if (left.getChildren().contains(updateRun)) {
                left.getChildren().remove(4, left.getChildren().size() - 1);
                algoBox.getSelectionModel().clearSelection();
                left.getChildren().add(4, algoBox);
                left.getChildren().add(5, updateRun);
            } else if (left.getChildren().contains(cancelRun)) {
                left.getChildren().remove(4, left.getChildren().size() - 1);
                algoBox.getSelectionModel().clearSelection();
                left.getChildren().add(4, algoBox);
                left.getChildren().add(5, cancelRun);
            } else if (left.getChildren().contains(displayIterationLabel)) {
                left.getChildren().remove(4);
                algoBox.getSelectionModel().clearSelection();
                left.getChildren().add(4, algoBox);
            } else {
                left.getChildren().remove(4, left.getChildren().size());
                algoBox.getSelectionModel().clearSelection();
                left.getChildren().add(4, algoBox);
            }

        });

        //setDisplayAction();
        setTextAreaOverflowAction();
        setToggleButtonsAction();
        setReadOnlyAction();

    }

    private void displayAction() {
        ((AppData) applicationTemplate.getDataComponent()).clear();
        chart.getData().clear();
        String str = textArea.getText();
        if (!lines.isEmpty()) {
            str += "\n";
            for (String data : lines) {
                str += data + "\n";
            }
        }
        ((AppData) applicationTemplate.getDataComponent()).loadData(str);
        ((AppData) applicationTemplate.getDataComponent()).displayData();
        currentText = textArea.getText();
        hasNewText = false;
        //chart.getData().add(createLine());
    }

//    private void setDisplayAction() {
//        displayButton.setOnAction(e -> {
//            if (doneBtn.isSelected() || !left.getChildren().contains(doneBtn)) {
//                try {
//                    hasNewText = haveNewText(currentText, textArea.getText());
//                    if ((hasNewText) && (isValidText(getTextContent())) && doneBtn.isSelected()) {
//                        displayAction();
//                    } else if ((hasNewText) && (isValidText(getTextContent())) && !left.getChildren().contains(doneBtn)) {
//                        displayAction();
//                    }
//                } catch (Exception ex) {
//                    errorDialog.show(manager.getPropertyValue(AppPropertyTypes.INVALID_INPUT_TITLE.name()), manager.getPropertyValue(AppPropertyTypes.INVALID_INPUT_MESSAGE.name()));
//                }
//            }
//        });
//    }
    private void setToggleButtonsAction() {
        doneBtn.setOnAction(e -> {
            if (editBtn.isSelected()) {
                editBtn.setSelected(false);
            }
            doneBtn.setSelected(true);
            textArea.setEditable(false);
            textArea.setStyle(manager.getPropertyValue(AppPropertyTypes.CHECKBOX_GRAY.name()));
            try {
                if (isValidText(textArea.getText())) {
                    ((AppData) applicationTemplate.getDataComponent()).loadData(textArea.getText());
                } else {
                    editBtn.fire();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        editBtn.setOnAction(e -> {
            if (doneBtn.isSelected()) {
                doneBtn.setSelected(false);
            }
            if (left.getChildren().size() > 3) {
                left.getChildren().remove(3, left.getChildren().size());
            }
            editBtn.setSelected(true);
            textArea.setEditable(true);
            textArea.setStyle(manager.getPropertyValue(AppPropertyTypes.CHECKBOX_WHITE.name()));
        });
    }

    private void setTextAreaOverflowAction() {
        textArea.textProperty().addListener(new ChangeListener<String>() {
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (newValue.split("\n").length < 10 && !lines.isEmpty()) {
                    String lastLine = oldValue.substring(oldValue.lastIndexOf('@'), oldValue.length());
                    if (newValue.contains(lastLine)) {
                        textArea.appendText("\n" + removeOverflowLineToTextArea());
                    } else {
                        textArea.appendText(removeOverflowLineToTextArea());
                    }
                }
            }
        });
    }

    private void setReadOnlyAction() {
        readOnly.selectedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean old_Val, Boolean new_Val) -> {
            textArea.setEditable(!new_Val);
            if (!checked) {
                textArea.setStyle(manager.getPropertyValue(AppPropertyTypes.CHECKBOX_GRAY.name()));
                checked = true;
            } else {
                textArea.setStyle(manager.getPropertyValue(AppPropertyTypes.CHECKBOX_WHITE.name()));
                checked = false;
            }

        });
    }

    public boolean toggleShown() {
        return !toggleButtonSpace.getChildren().isEmpty();
    }

    // Used only when data is loaded, loaded data should not be editable
    public void removeToggles() {
        if (((AppActions) applicationTemplate.getActionComponent()).existingFile()) {
            toggleButtonSpace.getChildren().remove(0, 2);
        }
    }

    public void addToggles() {
        toggleButtonSpace.getChildren().addAll(doneBtn, editBtn);
    }

    public Button createConfigButton() {
        Button configButton = new Button();
        manager = applicationTemplate.manager;

        String configPath = separator + String.join(separator, manager.getPropertyValue(GUI_RESOURCE_PATH.name()), manager.getPropertyValue(ICONS_RESOURCE_PATH.name()));
        Image img = new Image(String.join(separator, configPath, manager.getPropertyValue(AppPropertyTypes.CONFIG_ICON.name())));

        configButton.setGraphic(new ImageView(img));

        return configButton;
    }

    public void setConfigButtonActions(Algorithm e) {
        Stage configWindow = new Stage();
        BorderPane main = new BorderPane();
        VBox content = new VBox();
        configWindow.setTitle(manager.getPropertyValue(AppPropertyTypes.CONFIG_WINDOW_TITLE.name()));
        configWindow.setScene(new Scene(main));
        main.setCenter(content);
        HBox iteration = new HBox();
        iteration.setPadding(new Insets(10));
        HBox interval = new HBox();
        interval.setPadding(new Insets(10));
        HBox continuous = new HBox();
        continuous.setPadding(new Insets(10));

        iterationLabel = new Label(manager.getPropertyValue(AppPropertyTypes.ITERATION_LABEL.name()));
        iterationField = new TextArea();
        iterationField.setPrefSize(200, 50);
        intervalLabel = new Label(manager.getPropertyValue(AppPropertyTypes.INTERVAL_LABEL.name()));
        intervalField = new TextArea();
        intervalField.setPrefSize(200, 50);
        continuousRunLabel = new Label(manager.getPropertyValue(AppPropertyTypes.CONTINUOUS_LABEL.name()));
        continuousRun = new CheckBox();

        iteration.getChildren().addAll(iterationLabel, iterationField);

        interval.getChildren().addAll(intervalLabel, intervalField);

        continuous.getChildren().addAll(continuousRunLabel, continuousRun);

        setConfigUIActions(iterationField, intervalField, continuousRun, doneConfig);

        content.getChildren().addAll(iteration, interval, continuous, doneConfig);
        content.setPadding(new Insets(90));

        if (e == clustererAlgo || e == kMeansAlgo) {
            HBox clusterBox = new HBox();
            Label numberOfClustersLabel = new Label(manager.getPropertyValue(AppPropertyTypes.CLUSTERING_CLUSTER_LABEL.name()));
            numberOfClusters = new TextArea();
            numberOfClusters.setPrefSize(200, 50);
            numberOfClusters.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    if (!newValue.equals(oldValue)) {
                        try {
                            double numCluster = Double.parseDouble(newValue);
                            if (numCluster < 0) {
                                numberOfClusters.setText(manager.getPropertyValue(AppPropertyTypes.DEFAULT_CLUSTER_MIN.name()));
                            } else {
                                numberOfClusters.setText(String.valueOf(Math.round(numCluster)));
                            }
                        } catch (NumberFormatException e) {
                        }
                    }
                }
            });
            clusterBox.getChildren().addAll(numberOfClustersLabel, numberOfClusters);
            content.getChildren().add(2, clusterBox);
        }

        if (algoType.equals(manager.getPropertyValue(AppPropertyTypes.CLASSIFICATION.name()))) {
            if (e.getMaxIterations() != -1 || e.getUpdateInterval() != -1) {
                if (e.getMaxIterations() == -1) {
                    e.setMaxIterations(1);
                }
                if (e.getUpdateInterval() == -1) {
                    e.setUpdateInterval(1);
                }
                iterationField.setText(e.getMaxIterations() + "");
                intervalField.setText(e.getUpdateInterval() + "");
                continuousRun.setSelected(e.tocontinue());
            }
        } else if (algoType.equals(manager.getPropertyValue(AppPropertyTypes.CLUSTERING.name())) && e == clustererAlgo) {
            if (e.getMaxIterations() != -1 || e.getUpdateInterval() != -1 || clustererAlgo.getNumberOfClusters() != -1) {
                if (e.getMaxIterations() == -1) {
                    e.setMaxIterations(1);
                }
                if (e.getUpdateInterval() == -1) {
                    e.setUpdateInterval(1);
                }
                if (clustererAlgo.getNumberOfClusters() == -1) {
                    clustererAlgo.setNumClusters(2);
                }
                iterationField.setText(e.getMaxIterations() + "");
                intervalField.setText(e.getUpdateInterval() + "");
                continuousRun.setSelected(e.tocontinue());
                numberOfClusters.setText(String.valueOf(clustererAlgo.getNumberOfClusters()));
            }
        } else {
            if (e.getMaxIterations() != -1 || e.getUpdateInterval() != -1 || kMeansAlgo.getNumberOfClusters() != -1) {
                if (e.getMaxIterations() == -1) {
                    e.setMaxIterations(1);
                }
                if (e.getUpdateInterval() == -1) {
                    e.setUpdateInterval(1);
                }
                if (kMeansAlgo.getNumberOfClusters() == -1) {
                    kMeansAlgo.setNumberOfClusters(2);
                }
                iterationField.setText(e.getMaxIterations() + "");
                intervalField.setText(e.getUpdateInterval() + "");
                continuousRun.setSelected(((KMeansClusterer) e).continuous());
                numberOfClusters.setText(String.valueOf(kMeansAlgo.getNumberOfClusters()));
            }
        }
        configWindow.show();
    }

    public void setConfigUIActions(TextArea t, TextArea tt, CheckBox c, Button d) {
        t.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!newValue.equals(oldValue)) {
                    try {
                        double iteration = Double.parseDouble(newValue);
                        if (iteration <= 0) {
                            t.setText(manager.getPropertyValue(AppPropertyTypes.CONFIGURATION_DEFAULT_VALUE.name()));
                        } else {
                            t.setText(String.valueOf(Math.round(iteration)));
                        }
                    } catch (NumberFormatException e) {
                    }
                }
            }
        });

        tt.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!newValue.equals(oldValue)) {
                    try {
                        double interval = Double.parseDouble(newValue);
                        if (interval <= 0) {
                            tt.setText(manager.getPropertyValue(AppPropertyTypes.CONFIGURATION_DEFAULT_VALUE.name()));
                        } else {
                            tt.setText(String.valueOf(Math.round(interval)));
                        }
                    } catch (NumberFormatException e) {
                    }
                }
            }
        });

        d.setOnAction(e -> {
            if (algoType.equals(manager.getPropertyValue(AppPropertyTypes.CLASSIFICATION.name()))) {
                if (!t.getText().isEmpty()) {
                    classifierAlgo.setMaxIterations(t.getText().chars().allMatch(Character::isDigit) ? Integer.parseInt(t.getText()) : 1);
                }
                if (!tt.getText().isEmpty()) {
                    classifierAlgo.setUpdateInterval(tt.getText().chars().allMatch(Character::isDigit) ? Integer.parseInt(tt.getText()) : 1);
                }
                classifierAlgo.setToContinue(c.isSelected());
                try {
                    if (((AppActions) applicationTemplate.getActionComponent()).getDataFilePath() != null) {
                        classifierAlgo.setDataSet(DataSet.fromTSDFile(((AppActions) applicationTemplate.getActionComponent()).getDataFilePath()));
                    } else {
                        String[] arrOfLines = textArea.getText().split("\n");
                        classifierAlgo.setDataSet(DataSet.fromTextArea(arrOfLines));
                    }
                } catch (IOException | DataSet.InvalidDataNameException ex) {
                }
            } else if (algoType.equals(manager.getPropertyValue(AppPropertyTypes.CLUSTERING.name()))) {
                if (algorithmNumber == 1) {
                    if (!t.getText().isEmpty()) {
                        clustererAlgo.setMaxIterations(t.getText().chars().allMatch(Character::isDigit) ? Integer.parseInt(t.getText()) : 1);
                    }
                    if (!tt.getText().isEmpty()) {
                        clustererAlgo.setUpdateInterval(tt.getText().chars().allMatch(Character::isDigit) ? Integer.parseInt(tt.getText()) : 1);
                    }
                    clustererAlgo.setToContinue(c.isSelected());
                    if (!numberOfClusters.getText().isEmpty()) {
                        clustererAlgo.setNumClusters(numberOfClusters.getText().chars().allMatch(Character::isDigit) ? Integer.parseInt(numberOfClusters.getText()) : 1);
                    } else if (!t.getText().isEmpty() || !tt.getText().isEmpty()) {
                        clustererAlgo.setNumberOfClusters(2);
                    }
                    try {
                        if (((AppActions) applicationTemplate.getActionComponent()).getDataFilePath() != null) {
                            clustererAlgo.setDataSet(DataSet.fromTSDFile(((AppActions) applicationTemplate.getActionComponent()).getDataFilePath()));
                        } else {
                            String[] arrOfLines = textArea.getText().split("\n");
                            clustererAlgo.setDataSet(DataSet.fromTextArea(arrOfLines));
                        }
                    } catch (IOException | DataSet.InvalidDataNameException ex) {
                    }
                } else if (algorithmNumber == 2) {
                    if (!t.getText().isEmpty()) {
                        kMeansAlgo.setMaxIterations(t.getText().chars().allMatch(Character::isDigit) ? Integer.parseInt(t.getText()) : 1);
                    }
                    if (!tt.getText().isEmpty()) {
                        kMeansAlgo.setUpdateInterval(tt.getText().chars().allMatch(Character::isDigit) ? Integer.parseInt(tt.getText()) : 1);
                    }
                    kMeansAlgo.setToContinue(c.isSelected());
                    if (!numberOfClusters.getText().isEmpty()) {
                        kMeansAlgo.setNumberOfClusters(numberOfClusters.getText().chars().allMatch(Character::isDigit) ? Integer.parseInt(numberOfClusters.getText()) : 1);
                    } else if (!t.getText().isEmpty() || !tt.getText().isEmpty()) {
                        kMeansAlgo.setNumberOfClusters(2);
                    }
                    try {
                        if (((AppActions) applicationTemplate.getActionComponent()).getDataFilePath() != null) {
                            kMeansAlgo.setDataSet(DataSet.fromTSDFile(((AppActions) applicationTemplate.getActionComponent()).getDataFilePath()));
                        } else {
                            String[] arrOfLines = textArea.getText().split("\n");
                            kMeansAlgo.setDataSet(DataSet.fromTextArea(arrOfLines));
                        }
                    } catch (IOException | DataSet.InvalidDataNameException ex) {
                    }
                }
            }
            ((Stage) d.getScene().getWindow()).close();
        });
    }

    public VBox createClusteringAlgorithmMenu() {
        VBox clustering = new VBox();
        clustering.setSpacing(10);

        // Random Clustering
        Label clusteringTypeLabel = new Label(manager.getPropertyValue(AppPropertyTypes.CLUSTERING.name()));
        clusteringTypeLabel.setId(manager.getPropertyValue(AppPropertyTypes.ALGORITHM_LABEL_CSS_ID.name()));

        HBox algorithmsBox = new HBox();
        algorithmsBox.setSpacing(10);

        RadioButton algo = new RadioButton(manager.getPropertyValue(AppPropertyTypes.RADIO_CLUSTERING.name()));
        RadioButton kMeansAlgoBtn = new RadioButton((manager.getPropertyValue(AppPropertyTypes.K_MEANS_CLUSTERING.name())));

        Button algoConfig = createConfigButton();
        algoConfig.setId(manager.getPropertyValue(AppPropertyTypes.CONFIG_BUTTON_CSS_ID.name()));

        algoConfig.setOnAction(e -> {
            setConfigButtonActions(clustererAlgo);
            algorithmNumber = 1;
        });

        algo.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean prevNotSelected, Boolean isNowSelected) {
                if (isNowSelected) {
                    kMeansAlgoBtn.setSelected(!isNowSelected);
                    if (!(clustererAlgo.getMaxIterations() <= 0 && clustererAlgo.getUpdateInterval() <= 0)) {
                        algorithmNumber = 1;
                        if (left.getChildren().size() > 5) {
                            left.getChildren().remove(5, left.getChildren().size());
                        }
                        left.getChildren().add(generateRunButton());
                    }
                } else if (!isNowSelected) {
                    left.getChildren().remove(run);
                }
            }
        });

        algorithmsBox.getChildren().addAll(algo, algoConfig);

        // K Means Clustering
        Label kMeansClusteringTypeLabel = new Label(manager.getPropertyValue(AppPropertyTypes.K_MEANS_CLUSTERING.name()));
        kMeansClusteringTypeLabel.setId(manager.getPropertyValue(AppPropertyTypes.ALGORITHM_LABEL_CSS_ID.name()));
        HBox kMeansBox = new HBox();
        kMeansBox.setSpacing(10);

        Button kMeansAlgoConfig = createConfigButton();
        kMeansAlgoConfig.setId(manager.getPropertyValue((AppPropertyTypes.CONFIG_BUTTON_CSS_ID.name())));

        kMeansAlgoConfig.setOnAction(e -> {
            setConfigButtonActions(kMeansAlgo);
            algorithmNumber = 2;
        });

        kMeansAlgoBtn.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean prevNotSelected, Boolean isNowSelected) {
                if (isNowSelected) {
                    algo.setSelected(!isNowSelected);
                    if (!(kMeansAlgo.getMaxIterations() <= 0 && kMeansAlgo.getUpdateInterval() <= 0)) {
                        algorithmNumber = 2;
                        if (left.getChildren().size() > 5) {
                            left.getChildren().remove(5, left.getChildren().size());
                        }
                        left.getChildren().add(generateRunButton());
                    }
                } else if (!isNowSelected) {
                    left.getChildren().remove(run);
                }
            }
        });

        kMeansBox.getChildren().addAll(kMeansAlgoBtn, kMeansAlgoConfig);

        if (listOfAlgorithms.contains(manager.getPropertyValue(AppPropertyTypes.RClusterer.name())) && listOfAlgorithms.contains(manager.getPropertyValue(AppPropertyTypes.KClusterer.name()))) {
            clustering.getChildren().addAll(clusteringTypeLabel, algorithmsBox, kMeansBox, backBtn);
        } else if (listOfAlgorithms.contains(manager.getPropertyValue(AppPropertyTypes.RClusterer.name()))) {
            clustering.getChildren().addAll(clusteringTypeLabel, algorithmsBox, backBtn);
        } else if (listOfAlgorithms.contains(manager.getPropertyValue(AppPropertyTypes.KClusterer.name()))) {
            clustering.getChildren().addAll(clusteringTypeLabel, kMeansBox, backBtn);
        } else {
            clustering.getChildren().addAll(clusteringTypeLabel, backBtn);
        }

        return clustering;

    }

    public VBox createClassificationAlgorithmMenu() {
        VBox classification = new VBox();
        classification.setSpacing(10);

        Label classifierTypeLabel = new Label(manager.getPropertyValue(AppPropertyTypes.CLASSIFICATION.name()));
        classifierTypeLabel.setId(manager.getPropertyValue(AppPropertyTypes.ALGORITHM_LABEL_CSS_ID.name()));

        HBox algorithmsBox = new HBox();
        algorithmsBox.setSpacing(10);

        RadioButton algo = new RadioButton(manager.getPropertyValue(AppPropertyTypes.RADIO_CLASSIFICATION.name()));

        algo.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean prevNotSelected, Boolean isNowSelected) {
                if (isNowSelected) {
                    if (!(classifierAlgo.getMaxIterations() <= 0 && classifierAlgo.getUpdateInterval() <= 0)) {
                        algorithmNumber = 0;
                        if (left.getChildren().size() > 5) {
                            left.getChildren().remove(5, left.getChildren().size());
                        }
                        left.getChildren().add(generateRunButton());
                    }
                } else if (!isNowSelected) {
                    left.getChildren().remove(run);
                }
            }
        });

        Button algoConfig = createConfigButton();
        algoConfig.setId(manager.getPropertyValue(AppPropertyTypes.CONFIG_BUTTON_CSS_ID.name()));

        algoConfig.setOnAction(e -> {
            setConfigButtonActions(classifierAlgo);
            algorithmNumber = 0;
        });

        algorithmsBox.getChildren().addAll(algo, algoConfig);

        if (listOfAlgorithms.contains(manager.getPropertyValue(AppPropertyTypes.RClassifier.name()))) {
            classification.getChildren().addAll(classifierTypeLabel, algorithmsBox, backBtn);
        } else {
            classification.getChildren().addAll(classifierTypeLabel, backBtn);
        }

        return classification;
    }

    public void updateGUI() {
        if (left.getChildren().size() > 3) {
            left.getChildren().remove(3, left.getChildren().size());
        }
        algoBox.getSelectionModel().clearSelection();
        textArea.setEditable(false);
        sourceOfData = ((AppActions) applicationTemplate.getActionComponent()).getDataFilePath();
        ArrayList<String> instanceNames = ((AppData) applicationTemplate.getDataComponent()).getProcessor().getUniqueInstances();
        ArrayList<String> labelNames = ((AppData) applicationTemplate.getDataComponent()).getProcessor().getUniqueLabels();
        int numInstances = instanceNames.size();
        numLabels = 0;
        for (int i = 0; i < labelNames.size(); i++) {
            if (!labelNames.get(i).equals(manager.getPropertyValue(AppPropertyTypes.NULL.name()))) {
                numLabels++;
            }
        }
        String labelNameInfo = "";
        for (String label : labelNames) {
            labelNameInfo += "- " + label + "\n";
        }
        // Add Info About Loaded Data 
        Label fileInfo = new Label(numInstances + manager.getPropertyValue(AppPropertyTypes.FILE_INFO_ONE.name()) + numLabels
                + manager.getPropertyValue(AppPropertyTypes.FILE_INFO_TWO.name()) + (sourceOfData == null ? manager.getPropertyValue(AppPropertyTypes.SOURCEDATA_NULL.name()) : sourceOfData)
                + manager.getPropertyValue(AppPropertyTypes.FILE_INFO_THREE.name()) + "\n" + labelNameInfo);
        fileInfo.setWrapText(true);
        left.getChildren().add(fileInfo);

        // Add Algorithm Type Drop-Down Menu If Valid Data Exists
        left.getChildren().addAll(algoBox);
        if (!isClassification(textArea.getText())) {
            algoBox.getItems().remove(manager.getPropertyValue(AppPropertyTypes.CLASSIFICATION.name()));
        } else if (isClassification(textArea.getText()) && !algoBox.getItems().contains(manager.getPropertyValue(AppPropertyTypes.CLASSIFICATION.name()))) {
            algoBox.getItems().add(0, manager.getPropertyValue(AppPropertyTypes.CLASSIFICATION.name()));
        }

    }

    public boolean isClassification(String data) {
        ArrayList<String> labelNames = new ArrayList<>();
        String[] dataAsLines = data.split("\n");
        for (int i = 0; i < dataAsLines.length; i++) {
            String[] s = dataAsLines[i].split("\t");
            String label = s[1];
            if (!labelNames.contains(label) && !label.equals("null")) {
                labelNames.add(label);
            }
        }
        for (int j = 0; j < lines.size(); j++) {
            String[] s = lines.get(j).split("\t");
            String label = s[1];
            if (!labelNames.contains(label) && !label.equals("null")) {
                labelNames.add(label);
            }
        }
        return labelNames.size() == 2;
    }

    public void setIteration(int iteration) {
        Platform.runLater(() -> {
            this.displayIterationLabel.setText(manager.getPropertyValue(AppPropertyTypes.ALGO_ITERATION_LABEL.name()) + iteration);
        });
    }

    public Button generateRunButton() {
        if (left.getChildren().contains(run)) {
            left.getChildren().remove(run);
        }
        try {
            thread.interrupt();
            disableButtons(false);
        } catch (Exception e) {
        }
        manager = applicationTemplate.manager;

        String configPath = separator + String.join(separator, manager.getPropertyValue(GUI_RESOURCE_PATH.name()), manager.getPropertyValue(ICONS_RESOURCE_PATH.name()));
        Image img = new Image(String.join(separator, configPath, manager.getPropertyValue(AppPropertyTypes.RUN_ICON.name())));

        run.setText(manager.getPropertyValue(AppPropertyTypes.RUN_NAME.name()));
        run.setTooltip(new Tooltip(manager.getPropertyValue(AppPropertyTypes.RUN_TOOLTIP.name())));
        run.setGraphic(new ImageView(img));

        Algorithm algorithm = (Algorithm) algorithms.get(algorithmNumber);
        thread = new Thread(algorithm);
        thread.setDaemon(true);
        boolean cont = true;
        if (algorithm == kMeansAlgo) {
            cont = ((KMeansClusterer) algorithm).continuous();
        } else {
            cont = algorithm.tocontinue();
        }

        if (cont) {
            if (thread.isAlive()) {
                thread.interrupt();
            }
            run.setOnAction(e -> {
                resetDataSet();
                disableButtons(true);
                chart.getData().clear();
                fixChart();
                displayAction();
                chart.setAnimated(false);
                cancelRun.setOnAction(ee -> {
                    thread.interrupt();
                    left.getChildren().remove(cancelRun);
                    disableButtons(false);
                });
                left.getChildren().add(cancelRun);
                left.getChildren().add(displayIterationLabel);
                Platform.runLater(() -> {
                    thread.start();
                });
            });
        } else {
            if (thread.isAlive()) {
                thread.interrupt();
            }
            run.setOnAction(e -> {
                resetDataSet();
                chart.getData().clear();
                fixChart();
                displayAction();
                chart.setAnimated(false);
                left.getChildren().add(updateRun);
                left.getChildren().add(displayIterationLabel);
                updateRun.setDisable(true);
                scrnshotButton.setVisible(false);
                updateRun.setOnAction(ee -> {
                    scrnshotButton.setVisible(false);
                    updateRun.setDisable(true);
                    updateRun.setText(manager.getPropertyValue(AppPropertyTypes.UPDATE_BUTTON_RUNNING.name()));
                    synchronized (thread) {
                        thread.notify();
                    }
                });
                Platform.runLater(() -> {
                    thread.start();
                });
            });
        }
        return run;
    }

    public void disableButtons(boolean bool) {
        if (bool) {
            newButton.setDisable(true);
            loadButton.setDisable(true);
            scrnshotButton.setVisible(false);
        } else {
            newButton.setDisable(false);
            loadButton.setDisable(false);
            scrnshotButton.setVisible(true);
        }
    }

    public void algorithmOnFinish() {
        Platform.runLater(() -> {
            algoBox.getSelectionModel().clearSelection();
            if (!left.getChildren().get(4).equals(algoBox)) {
                left.getChildren().remove(4, left.getChildren().size() - 1);
                left.getChildren().add(4, algoBox);
            } else {
                left.getChildren().remove(4, left.getChildren().size() - 1);
                left.getChildren().add(4, algoBox);
            }
        });
    }

    private void fixChart() {
        chart.getXAxis().setAutoRanging(false);
        chart.getYAxis().setAutoRanging(false);
        double xMin = ((AppData) applicationTemplate.getDataComponent()).getProcessor().getXMin() - 1;
        double xMax = ((AppData) applicationTemplate.getDataComponent()).getProcessor().getXMax() + 1;
        double yMin = ((AppData) applicationTemplate.getDataComponent()).getProcessor().getYMin() - 1;
        double yMax = ((AppData) applicationTemplate.getDataComponent()).getProcessor().getYMax() + 1;
        ((NumberAxis) chart.getXAxis()).setUpperBound(xMax);
        ((NumberAxis) chart.getXAxis()).setLowerBound(xMin);
        ((NumberAxis) chart.getYAxis()).setUpperBound(yMax);
        ((NumberAxis) chart.getYAxis()).setLowerBound(yMin);
        ((NumberAxis) chart.getXAxis()).setTickUnit(1);
        ((NumberAxis) chart.getYAxis()).setTickUnit(1);
    }

    private void resetDataSet() {
        try {
            if (((AppActions) applicationTemplate.getActionComponent()).getDataFilePath() != null) {
                classifierAlgo.setDataSet(DataSet.fromTSDFile(((AppActions) applicationTemplate.getActionComponent()).getDataFilePath()));
                clustererAlgo.setDataSet(DataSet.fromTSDFile(((AppActions) applicationTemplate.getActionComponent()).getDataFilePath()));
                kMeansAlgo.setDataSet(DataSet.fromTSDFile(((AppActions) applicationTemplate.getActionComponent()).getDataFilePath()));
            } else {
                String[] arrOfLines = textArea.getText().split("\n");
                classifierAlgo.setDataSet(DataSet.fromTextArea(arrOfLines));
                clustererAlgo.setDataSet(DataSet.fromTextArea(arrOfLines));
                kMeansAlgo.setDataSet(DataSet.fromTextArea(arrOfLines));
            }
        } catch (DataSet.InvalidDataNameException | IOException e) {
        }
    }
}
