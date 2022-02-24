import java.io.FileWriter;
import java.io.IOException;

public class FileOutput {
    private FileWriter fileWriter;

    public FileOutput(String path) throws IOException {
        fileWriter = new FileWriter(path);
    }

    public void output(String str) throws IOException {
        fileWriter.write(str);
    }

    public void closeFile() throws IOException {
        fileWriter.close();
    }
}
