package train;

import java.io.File;
import java.nio.file.Paths;

/**
 *
 * @author Akshen Kadakia <akshenk8@gmail.com>
 */
public class FileNames {

    public static final String SEPARATOR = File.separator;

    public static final String TEACHER_FOLDER_NAME = "Trainer";
    public static final String TEACHER_FILE_NAME = "ts.ser";

    /**
     * Return project root directory
     *
     * @return
     */
    public static String currentDir() {
        return Paths.get("").toAbsolutePath().toString();
    }
}
