package actions;

import dataprocessors.AppData;
import java.io.File;
import java.io.FileWriter;
import vilij.components.ActionComponent;
import vilij.templates.ApplicationTemplate;
import java.io.IOException;
import java.nio.file.Path;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.WritableImage;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import settings.AppPropertyTypes;
import vilij.propertymanager.PropertyManager;
import vilij.components.*;
import ui.AppUI;
import javafx.embed.swing.SwingFXUtils;
import javax.imageio.ImageIO;

import static vilij.settings.PropertyTypes.*;

/**
 * This is the concrete implementation of the action handlers required by the
 * application.
 *
 * @author Andy Cen
 */
public final class AppActions implements ActionComponent {

    /**
     * The application to which this class of actions belongs.
     */
    private ApplicationTemplate applicationTemplate;
    private PropertyManager manager;

    /**
     * Path to the data file currently active.
     */
    Path dataFilePath;
    
    public String getFileName() { 
        if (dataFilePath != null) {
            return dataFilePath.getFileName().toString();
        }
        return "New Data";
    }
    
    public boolean existingFile() {
        return dataFilePath != null;
    }
    
    public Path getDataFilePath() {
        return dataFilePath;
    }
    
    public void setDataFilePath(Path path) {
        dataFilePath = path;
    }
    
    public void resetDataFilePath() { 
        dataFilePath = null;
    }
    

    public AppActions(ApplicationTemplate applicationTemplate) {
        this.applicationTemplate = applicationTemplate;
        manager = applicationTemplate.manager;
    }

    /*
    Saves File
     */
    private void SaveFile(String content, File file) {
        try {
            FileWriter fileWriter;
            fileWriter = new FileWriter(file);
            fileWriter.write(content);
            fileWriter.close();
        } catch (IOException ex) {
            ErrorDialog errorDialog = ErrorDialog.getDialog();
            errorDialog.show(manager.getPropertyValue(SAVE_ERROR_TITLE.name()), manager.getPropertyValue(SAVE_ERROR_MSG.name()));
        }
    }

    @Override
    public void handleNewRequest() {
        Stage window = new Stage();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(manager.getPropertyValue(AppPropertyTypes.SAVE_UNSAVED_WORK.name()));
        fileChooser.setInitialDirectory(new File(manager.getPropertyValue(AppPropertyTypes.SET_DIRECTORY.name())));
        FileChooser.ExtensionFilter ext = new FileChooser.ExtensionFilter(manager.getPropertyValue(AppPropertyTypes.DATA_FILE_EXT_DESC.name()), manager.getPropertyValue(AppPropertyTypes.DATA_FILE_EXT.name()));
        fileChooser.getExtensionFilters().addAll(ext);
        File file = fileChooser.showSaveDialog(window);

        if (file != null) {
            SaveFile(((AppUI) applicationTemplate.getUIComponent()).getTextContent(), file);
        }

    }

    @Override
    public void handleSaveRequest() {
        Stage window = new Stage();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(manager.getPropertyValue(AppPropertyTypes.SAVE_UNSAVED_WORK.name()));
        fileChooser.setInitialDirectory(new File(manager.getPropertyValue(AppPropertyTypes.SET_DIRECTORY.name())));
        FileChooser.ExtensionFilter ext = new FileChooser.ExtensionFilter(manager.getPropertyValue(AppPropertyTypes.DATA_FILE_EXT_DESC.name()), manager.getPropertyValue(AppPropertyTypes.DATA_FILE_EXT.name()));
        fileChooser.getExtensionFilters().addAll(ext);
        File file = fileChooser.showSaveDialog(window);

        if (file != null) {
            SaveFile(((AppUI) applicationTemplate.getUIComponent()).getTextContent(), file);
            dataFilePath = file.toPath();
        }
    }

    @Override
    public void handleLoadRequest() {
        Stage window = new Stage();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(manager.getPropertyValue(AppPropertyTypes.LOAD_DATA_TITLE.name()));
        fileChooser.setInitialDirectory(new File(manager.getPropertyValue(AppPropertyTypes.SET_DIRECTORY.name())));
        FileChooser.ExtensionFilter ext = new FileChooser.ExtensionFilter(manager.getPropertyValue(AppPropertyTypes.DATA_FILE_EXT_DESC.name()), manager.getPropertyValue(AppPropertyTypes.DATA_FILE_EXT.name()));
        fileChooser.getExtensionFilters().addAll(ext);
        File file = fileChooser.showOpenDialog(window);

        if (file != null) {
            dataFilePath = file.toPath();
            ((AppUI) applicationTemplate.getUIComponent()).getOverflowLines().clear();
            ((AppData)applicationTemplate.getDataComponent()).loadData(file.toPath());
            if (((AppUI) applicationTemplate.getUIComponent()).toggleShown())
                ((AppUI) applicationTemplate.getUIComponent()).removeToggles();
        }
    }

    @Override
    public void handleExitRequest() {
        Platform.exit();
    }

    @Override
    public void handlePrintRequest() {
        // Not yet implemented
    }

    public void handleScreenshotRequest() throws IOException {
        WritableImage image = ((AppUI) applicationTemplate.getUIComponent()).getChart().snapshot(null, null);

        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(manager.getPropertyValue(AppPropertyTypes.SET_DIRECTORY.name())));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(manager.getPropertyValue(AppPropertyTypes.SAVE_IMAGE_EXT.name()), manager.getPropertyValue(AppPropertyTypes.SAVE_IMAGE_PNG.name())));
        File file = fileChooser.showSaveDialog(null);

        if (file != null) {
            try {
                ImageIO.write(SwingFXUtils.fromFXImage(image, null), manager.getPropertyValue(AppPropertyTypes.PNG.name()), file);
            } catch (IOException e) {
                ErrorDialog errorDialog = ErrorDialog.getDialog();
                errorDialog.show(manager.getPropertyValue(SAVE_ERROR_TITLE.name()), manager.getPropertyValue(SAVE_ERROR_MSG.name()));
            }
        }
    }

    /**
     * This helper method verifies that the user really wants to save their
     * unsaved work, which they might not want to do. The user will be presented
     * with three options:
     * <ol>
     * <li><code>yes</code>, indicating that the user wants to save the work and
     * continue with the action,</li>
     * <li><code>no</code>, indicating that the user wants to continue with the
     * action without saving the work, and</li>
     * <li><code>cancel</code>, to indicate that the user does not want to
     * continue with the action, but also does not want to save the work at this
     * point.</li>
     * </ol>
     *
     * @return <code>false</code> if the user presses the <i>cancel</i>, and
     * <code>true</code> otherwise.
     */
    public boolean promptToSave() throws IOException {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, manager.getPropertyValue(AppPropertyTypes.SAVE_UNSAVED_WORK.name()), ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
        alert.setTitle(manager.getPropertyValue(AppPropertyTypes.SAVE_UNSAVED_WORK_TITLE.name()));
        alert.showAndWait();
        if (alert.getResult() == ButtonType.NO) {
            Platform.exit();
            return false;
        } else if (alert.getResult() == ButtonType.YES) {
            return true;
        }
        return false;
    }
}
