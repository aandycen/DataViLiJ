package dataprocessors;

import actions.AppActions;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import ui.AppUI;
import vilij.components.DataComponent;
import vilij.templates.ApplicationTemplate;

import java.nio.file.Path;
import java.util.ArrayList;
import settings.AppPropertyTypes;
import vilij.components.*;
import vilij.propertymanager.*;

/**
 * This is the concrete application-specific implementation of the data
 * component defined by the Vilij framework.
 *
 * @author Andy Cen
 * @see DataComponent
 */
public class AppData implements DataComponent {

    private TSDProcessor processor;
    private ApplicationTemplate applicationTemplate;
    private ErrorDialog errorDialog;
    private PropertyManager manager;

    public TSDProcessor getProcessor() {
        return this.processor;
    }

    public AppData(ApplicationTemplate applicationTemplate) {
        this.processor = new TSDProcessor();
        this.applicationTemplate = applicationTemplate;
        this.manager = applicationTemplate.manager;
    }

    @Override
    public void loadData(Path dataFilePath) {
        File file = dataFilePath.toFile();
        try {
            String text = readFile(file);
            if (((AppUI) applicationTemplate.getUIComponent()).isValidText(text)) {
                ((AppUI) applicationTemplate.getUIComponent()).setTextContent(text);
                ((AppUI) applicationTemplate.getUIComponent()).setCurrentText(text);
                ArrayList<String> lines = (((AppUI) applicationTemplate.getUIComponent()).getOverflowLines());
                if (!lines.isEmpty()) {
                    text += "\n";
                    for (Object data : ((AppUI) applicationTemplate.getUIComponent()).getOverflowLines()) {
                        text += data.toString() + "\n";
                    }
                }
                loadData(text);
                //((AppUI) applicationTemplate.getUIComponent()).getChart().getData().add(((AppUI) applicationTemplate.getUIComponent()).createLine());
            }
            else {
                ((AppActions) applicationTemplate.getActionComponent()).resetDataFilePath();
            }
        } catch (IOException ex) {

        }
    }

    public String readFile(File file) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(file));
        int counter = 0;
        try {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();
            while (line != null && counter < 10) {
                sb.append(line);
                sb.append("\n");
                line = br.readLine();
                counter++;
            }
            while (line != null) {
                ((AppUI) applicationTemplate.getUIComponent()).addOverflowLine(line);
                line = br.readLine();
                counter++;
            }
            return sb.substring(0, sb.length() - 1);
        } finally {
            br.close();
//            if (!((AppUI)applicationTemplate.getUIComponent()).loadError()) {
//                Alert alert = new Alert(Alert.AlertType.INFORMATION, manager.getPropertyValue(AppPropertyTypes.INFO_ONE.name()) + counter + manager.getPropertyValue(AppPropertyTypes.INFO_TWO.name()), ButtonType.OK);
//                alert.setTitle(manager.getPropertyValue(AppPropertyTypes.LOAD_DATA_TITLE.name()));
//                alert.showAndWait();
//            }
        }
    }

    public void loadData(String dataString) {
        errorDialog = ErrorDialog.getDialog();
        try {
            this.processor.processString(dataString);
            ((AppUI) applicationTemplate.getUIComponent()).updateGUI();
            //this.displayData();
        } catch (Exception ex) {
            errorDialog.show(manager.getPropertyValue(AppPropertyTypes.INVALID_INPUT_TITLE.name()), manager.getPropertyValue(AppPropertyTypes.INVALID_INPUT_MESSAGE.name()));
        }
    }

    @Override
    public void saveData(Path dataFilePath) {
        File file = dataFilePath.toFile();
        try {
            writeFile(file);
        } catch (IOException ex) {

        }
    }

    public void writeFile(File file) throws IOException {
        BufferedWriter out = null;
        try {
            FileWriter fstream = new FileWriter(file, false);
            out = new BufferedWriter(fstream);
            out.write(((AppUI) applicationTemplate.getUIComponent()).getTextContent() + "\n");
            out.write(((AppUI) applicationTemplate.getUIComponent()).extractLinesToSaveFile());
        } catch (IOException e) {

        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    @Override
    public void clear() {
        processor.clear();
    }

    public void displayData() {
        processor.toChartData(((AppUI) applicationTemplate.getUIComponent()).getChart());
    }
}
