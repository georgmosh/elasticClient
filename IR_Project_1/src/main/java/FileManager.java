import java.io.File;
import java.util.ArrayList;
/**
 * FileManager.java
 * A static class handling listing of existing filenames in a given directory of a file system.
 * @author Georgios M. Moschovis (p3150113@aueb.gr)
 */
public class FileManager {
    /**
     * A method creating a dynamic container of all filepaths contained in a given directory.
     * @param path The directory to find all contained filepaths.
     */
    public static ArrayList<String> listAllFiles(String path){
        ArrayList<String> filenames = new ArrayList<String>();

        /*
         * Listing given repository's contained files.
         */
        File folder = new File(path);
        File[] listOfFiles = folder.listFiles();
        System.out.println("Folder: " + folder);
        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) filenames.add(listOfFiles[i].getName());
        }

        return filenames;
    }
}
