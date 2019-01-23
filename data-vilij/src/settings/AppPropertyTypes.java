package settings;

/**
 * This enumerable type lists the various application-specific property types listed in the initial set of properties to
 * be loaded from the workspace properties <code>xml</code> file specified by the initialization parameters.
 *
 * @author Andy Cen
 * @see vilij.settings.InitializationParams
 */
public enum AppPropertyTypes {

    /* resource files and folders */
    DATA_RESOURCE_PATH,
    DATA_VILIJ_CSS,

    /* user interface icon file names */
    SCREENSHOT_ICON,
    CONFIG_ICON,
    RUN_ICON,

    /* tooltips for user interface buttons */
    SCREENSHOT_TOOLTIP,

    /* error messages */
    RESOURCE_SUBDIR_NOT_FOUND,
    EXIT_WHILE_RUNNING_WARNING,

    /* application-specific message titles */
    SAVE_UNSAVED_WORK_TITLE,

    /* application-specific messages */
    SAVE_UNSAVED_WORK,

    /* application-specific parameters */
    DATA_FILE_EXT,
    DATA_FILE_EXT_DESC,
    TEXT_AREA,
    SPECIFIED_FILE,

    /* label and button names */
    DATA_TEXT_AREA,
    DATA_VISUALIZE,
    DISPLAY_NAME,
    AVERAGE_LINE,
    READ_ONLY,
    EMPTY_SPACE,
    
    /* input error messages */
    INVALID_INPUT_TITLE,
    INVALID_DATA_MSG,
    INVALID_NAME,
    DUPLICATE_NAME,
    INVALID_INPUT_MESSAGE,
    INVALID_NUM_DATA,
    
    /* filechooser */
    LOAD_DATA_TITLE,
    SET_DIRECTORY,
    SAVED,
    NULL,
    SAVE_IMAGE_EXT,
    SAVE_IMAGE_PNG,
    PNG,
    
    /* load data info */
    INFO_ONE,
    INFO_TWO,
    
    /* css */
    CHECKBOX_GRAY,
    CHECKBOX_WHITE,
    LINE_LOOKUP,
    INVIS_LINE,
    CURSOR_STYLE,
    
    /* use cases */
    RUN_TOOLTIP,
    RUN_NAME,
    CONFIG_BUTTON_CSS_ID,
    RADIO_CLASSIFICATION,
    RADIO_CLUSTERING,
    CLASSIFICATION,
    CLUSTERING,
    K_MEANS_CLUSTERING,
    CONFIGURATION_DEFAULT_VALUE,
    DEFAULT_CLUSTER_MIN,
    DEFAULT_CLUSTER_MAX,
    CLUSTERING_CLUSTER_LABEL,
    ITERATION_LABEL,
    INTERVAL_LABEL,
    CONTINUOUS_LABEL,
    CONFIG_WINDOW_TITLE,
    ALGORITHM_LABEL_CSS_ID,
    FILE_INFO_ONE,
    FILE_INFO_TWO,
    FILE_INFO_THREE,
    SOURCEDATA_NULL,
    CREATE_DATA_ERROR_ONE,
    CREATE_DATA_ERROR_TWO,
    CONFIRMATION_YES,
    EXIT_WHILE_RUNNING_TITLE,
    DONE_TOGGLE_NAME,
    EDIT_TOGGLE_NAME,
    SELECTION_BOX_TITLE,
    BACK_BUTTON_NAME,
    UPDATE_BUTTON_RUNNING,
    CANCEL_RUN,
    UPDATE_BUTTON_RESUME,
    PROMPT_TEXT,
    ALGORITHMS_FOLDER,
    ALGO_ITERATION_LABEL,
    RClusterer,
    KClusterer,
    RClassifier,
    INVALID_LABEL_TITLE,
    INVALID_LABEL,
    
}
