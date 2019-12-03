import org.json.JSONObject;
import org.json.XML;

import java.io.*;
import java.util.ArrayList;

/**
 * Conversions.java
 * A class converting a collection of XML elements to their JSON equivalents.
 * Those equivalents are later on used for indexing; retracted from a dynamic container.
 * @author Georgios M. Moschovis (p3150113@aueb.gr)
 */
public class Conversions {
    private ArrayList<String> XML_data_filenames;
    private String inputDataRepository, outputDataRepository;

    /**
     * Overloaded Constructor.
     * @param inputRepository The name of the directory to find inputs' contained filepaths.
     * @param outputRepository The name of the directory to write parsed JSON equivalents.
     */
    public Conversions(String inputRepository, String outputRepository) {
        this.inputDataRepository = inputRepository;
        this.outputDataRepository = outputRepository;
        CollectionDAO.InitializeContainerJSON();
    }

    /**
     * A method detecting and writing down all filepaths contained in a given directory.
     */
    public void GenerateSearchData() {
        XML_data_filenames = FileManager.listAllFiles(".\\" + inputDataRepository);

        /*
         * Searching for the above listed files.
         */
        int numOfTargetFiles = XML_data_filenames.size();
        for(int file = 0; file < numOfTargetFiles; file++) {
            ReadTargetFile(inputDataRepository + "/" + XML_data_filenames.get(file));
        }
    }

    /**
     * A method reading a specific XML file and generating its JSON equivalent.
     * @param filePath The relative path of the XML file.
     */
    public void ReadTargetFile(String filePath) {
        BufferedReader br = null;
        String sCurrentLine, contents = "";
        try {
            br = new BufferedReader(new FileReader(filePath));
            while ((sCurrentLine = br.readLine()) != null) {
                sCurrentLine = sCurrentLine.trim().replaceAll(" +", " "); // unify whitespaces
                contents += (sCurrentLine + "\n");
            }

            /*
             * Converting XML to JSON; while unifying "title" and "objective" into "text"..
             */
            JSONObject obj = XML.toJSONObject(contents);
            String current = obj.toString().replaceAll("\"title\":", "\"text\":");
            current = current.replaceAll("\",\"objective\":\"", " ");

            System.out.println("File: " + filePath + "\nXML: " + contents + "\nJSON: " + current + "\n\n");

            /*
             * Storing JSON context into dynamic container; as well as writing it to a new file.
             */
            String[] filePathSplit = filePath.split("/");
            filePathSplit = filePathSplit[filePathSplit.length - 1].split("\\.");
            CollectionDAO.GetDataJSON().add(new Vec2<String, String>(filePathSplit[0], current));
            PrintWriter pw = new PrintWriter(outputDataRepository + "/" + filePathSplit[0] + ".json");
            pw.write(current); pw.flush(); pw.close();
        } catch (IOException exc) {
            exc.printStackTrace();
        }
    }
}