import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import java.awt.TextArea;
import java.io.FileWriter;
import java.io.IOException;
import static org.junit.Assert.*;

/**
 *
 * @author Andy
 */
public class JUnitTest {

    public JUnitTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }
    
    @Test
    public void testTSDValid() throws Exception {
        String testStr1 = "@instance1	label1	2,2";
        String testStr2 = "@instance2	label4	10.2,5.3";
        
        assertTrue(processString(testStr1));
        assertTrue(processString(testStr2));
        
        System.out.println("\nTest Parse One Line Of Data\n" + testStr1 + "\n -> Valid Format? " + processString(testStr1));
        System.out.println("\nTest Parse One Line Of Data\n" + testStr2 + "\n -> Valid Format? " + processString(testStr2));

    }

    /*
    @testStr1 , Entire format is wrong
    @testStr2 , Proper format, but the label is a empty string
    @testStr3 , Includes the necessary parameters: instance name, label name, and data point, but is not tab-separated
    */
    @Test(expected = IndexOutOfBoundsException.class)
    public void testTSDInvalid() throws Exception {
        String testStr1 = "afdf"; // No '@'
        String testStr2 = "@instance    2,2";
        String testStr3 = "@instancelabel12.2";
        
        assertFalse(processString(testStr1));
        assertFalse(processString(testStr2));
        assertFalse(processString(testStr3));
    }

    @Test
    public void saveTSDValid() throws Exception {
        TextArea textArea = new TextArea();
        String saveStr1 = "@instance1	label1	21,21";
        textArea.setText(saveStr1);
        if (processString(textArea.getText())) {
            String file = "ValidTSD.tsd";
            try {
                writeFile(file, textArea.getText());
                System.out.println("\nTesting Save Valid Data To .TSD File\n" + saveStr1 + "\n -> Valid Format? " + processString(saveStr1) + "\n -> Saved.");
            } catch (IOException ex) {
            }
        }
    }

    /*
    @saveStr1 , Entirely format is wrong
    */
    @Test(expected = IndexOutOfBoundsException.class)
    public void saveTSDInvalid() throws Exception {
        TextArea textArea = new TextArea();
        String saveStr1 = "fdagasd"; // No "@'
        textArea.setText(saveStr1);
        if (processString(textArea.getText())) {
            String file = "InvalidTSD.tsd";
            try {
                writeFile(file, textArea.getText());
            } catch (IOException ex) {
            }
        }
    }

    @Test
    public void testClassificationValid() {
        String maxIteration = "12";
        String updateInterval = "3";

        assertTrue(testClassificationValues(maxIteration, updateInterval));

        System.out.println("\nValues for Classification:\n" + maxIteration + " Iterations," + updateInterval + " Interval  \n -> Valid?: " + testClassificationValues(maxIteration, updateInterval));
    }

    /*
    @maxIteration1, updateInterval1 , The values are negative and therefore is invalid
    @maxIteration2, updateInterval2 , The values are not numeric
    @maxIteration3, updateInterval3 , The values are positive, but it is a Double
    */
    @Test(expected = NumberFormatException.class)
    public void testClassificationInvalid() {
        String maxIteration1 = "-2";
        String updateInterval1 = "-6";

        String maxIteration2 = "das";
        String updateInterval2 = "bdas";
        
        String maxIteration3 = "3.3";
        String updateInterval3 = "1.1";

        assertFalse(testClassificationValues(maxIteration1, updateInterval1));
        assertFalse(testClassificationValues(maxIteration2, updateInterval2));
        assertFalse(testClassificationValues(maxIteration3, updateInterval3));
    }

    @Test
    public void testClusteringValid() {
        String maxIteration = "12";
        String updateInterval = "3";
        String labels = "3";

        assertTrue(testClusteringValues(maxIteration, updateInterval, labels));

        System.out.println("\nValues for Clusterer:\n" + maxIteration + " Iterations," + updateInterval + " Interval," + labels + " Labels \n -> Valid?: " + testClusteringValues(maxIteration, updateInterval, labels));
    }

    @Test(expected = NumberFormatException.class)
    public void testClusteringInvalid() {
        String maxIteration1 = "-2";
        String updateInterval1 = "-6";
        String labels1 = "-2";

        String maxIteration2 = "das";
        String updateInterval2 = "bdas";
        String labels2 = "dasf";
        
        String maxIteration3 = "3.3";
        String updateInterval3 = "1.1";
        String labels3 = "0.5";

        assertFalse(testClusteringValues(maxIteration1, updateInterval1, labels1));
        assertFalse(testClusteringValues(maxIteration2, updateInterval2, labels2));
        assertFalse(testClusteringValues(maxIteration3, updateInterval3, labels3));

    }

    //// METHODS TO TEST THE METHODS
    private boolean processString(String tsdString) throws Exception {
        String[] tabSplit = tsdString.split("\t");
        String label = tabSplit[1];
        String[] pair = tabSplit[2].split(",");
        if (!checkedname(tabSplit[0]) || label.equals("")) {
            throw new IndexOutOfBoundsException();
        }
        try {
            double d1 = Double.parseDouble(pair[0]);
            double d2 = Double.parseDouble(pair[1]);
        } catch (NumberFormatException e) {
            throw new IndexOutOfBoundsException();
        }
        return true;
    }

    private boolean checkedname(String name) {
        return name.startsWith("@");
    }

    private void writeFile(String file, String text) throws IOException {
        FileWriter fstream = null;
        try {
            fstream = new FileWriter(file, false);
            fstream.write(text);
        } catch (IOException e) {
            System.out.println("File Doesn't Exist");
        } finally {
            if (fstream != null) {
                fstream.close();
            }
        }
    }

    private boolean testClassificationValues(String maxIterations, String updateInterval) {
        try {
            int iteration = Integer.parseInt(maxIterations);
            int interval = Integer.parseInt(updateInterval);
            if (iteration < 1 || interval < 1) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            throw new NumberFormatException();
        }
        return true;
    }

    private boolean testClusteringValues(String maxIterations, String updateInterval, String numLabels) {
        try {
            int iteration = Integer.parseInt(maxIterations);
            int interval = Integer.parseInt(updateInterval);
            int labels = Integer.parseInt(numLabels);
            if (iteration < 1 || interval < 1) {
                throw new NumberFormatException();
            }
            if (labels < 2 || labels > 4) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            throw new NumberFormatException();
        }
        return true;
    }
}
