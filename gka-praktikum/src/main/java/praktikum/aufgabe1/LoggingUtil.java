package main.java.praktikum.aufgabe1;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Utility class to create log files.
 * <p>
 * How to use:
 * 1.) Create a new LoggingUtil-Object in your main application. You can specify a path, filename and charset you want to use.
 * If not specified, a directory and file will be created at the specified STANDARD_PATH named after the specified
 * STANDARD_FILENAME using the specified STANDARD_CHARSET.
 * If not changed the file will be located at ./logs/log.txt using the UTF-8 charset.
 * <p>
 * 2.) Use the write()-Methods and append()-Methods (as well as their variations) to add new information to the logfile.
 * Multiple LoggingUtil-Objects can write to the same logfile.
 *
 */
public class LoggingUtil {
    private static final String STANDARD_PATH = "." + File.separatorChar + "logs";
    private final String path;
    private static final String STANDARD_FILENAME = "log.txt";
    private final String filename;
    private String filePath;
    private static final Charset STANDARD_CHARSET = StandardCharsets.UTF_8;
    private final Charset charset;

    public LoggingUtil(String path, String filename, Charset charset) throws IOException {
        if (path == null || path.equals("")) {
            throw new IOException("Path can not be null or an empty String");
        }
        if (filename == null || filename.equals("")) {
            throw new IOException("Filename can not be null or an empty String.");
        }
        this.path = path;
        this.filename = filename;
        this.filePath = path + File.separator;
        this.filePath = this.filePath.concat(filename);
        this.charset = charset;
    }

    public LoggingUtil(String filename, Charset charset) throws IOException {
        this(STANDARD_PATH, filename, charset);
    }

    public LoggingUtil(String path, String filename) throws IOException {
        this(path, filename, STANDARD_CHARSET);
    }

    public LoggingUtil(String filename) throws IOException {
        this(STANDARD_PATH, filename);
    }

    public LoggingUtil() throws IOException {
        this(STANDARD_FILENAME);
    }

    /**
     * Opens a file accordingly to filePath and returns the File-Object for other Methods to be used to write to.
     *
     * @return A File-Object according to filePath.
     * @throws IOException
     */
    public File openFile() {
        File file = new File(filePath);
        if (!file.exists() && !file.getParentFile().mkdirs()) {
            try {
                // This exception will be thrown if a part filePath of the path already exists
                throw new IOException("Unable to create " + file.getParentFile() + ". This may be, because part of the path already existed.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    /**
     * Writes input at the beginning of the logfile.
     * This method overwrites anything that is currently in the file!
     *
     * @param input This will be written at the start of the logfile.
     * @throws IOException
     */
    public void write(String input) {
        File file = openFile();
        try (BufferedWriter bf = new BufferedWriter(new FileWriter(file, charset))) {
            bf.write(input);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void write(int input) {
        write("" + input);
    }

    public void write(char input) {
        write(input);
    }

    public <T> void write(T[] input) {
        String s = "";
        for (T t : input) {
            s = s.concat("" + t);
        }
        write(s);
    }

    public <T> void write(List<T> input) {
        String s = "";
        for (T t : input) {
            s = s.concat("" + t);
        }
        write(s);
    }

    /**
     * Writes input at the beginning of the logfile and creates a new line.
     * This method overwrites anything that is currently in the file!
     *
     * @param input This will be written at the start of the logfile.
     * @throws IOException
     */
    public void writenl(String input) {
        write(input);
        newLine();
    }

    public void writenl(int input) {
        writenl("" + input);
    }

    public void writenl(char input) {
        writenl(input);
    }

    public <T> void writenl(T[] input) {
        String s = "";
        for (T t : input) {
            s = s.concat("" + t + "\n");
        }
        write(s);
    }

    public <T> void writenl(List<T> input) {
        String s = "";
        for (T t : input) {
            s = s.concat("" + t + "\n");
        }
        write(s);
    }

    /**
     * Appends input to the end of the logfile.
     *
     * @param input This will be appended to the end of the logfile.
     * @throws IOException
     */
    public void append(String input) {
        File file = openFile();
        try (BufferedWriter bf = new BufferedWriter(new FileWriter(file, charset, true))) {
            bf.append(input);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void append(int input) {
        append("" + input);
    }

    public void append(char input) {
        append(input);
    }

    public <T> void append(T[] input) {
        String s = "";
        for (T t : input) {
            s = s.concat("" + t);
        }
        append(s);
    }

    public <T> void append(List<T> input) {
        String s = "";
        for (T t : input) {
            s = s.concat("" + t);
        }
        append(s);
    }

    /**
     * Appends input to the end of the logfile and creates a new line.
     *
     * @param input This will be appended to the end of the logfile.
     * @throws IOException
     */
    public void appendnl(String input) {
        append(input);
        newLine();
    }

    public void appendnl(int input) {
        appendnl("" + input);
    }

    public void appendnl(char input) {
        appendnl(input);
    }

    public <T> void appendnl(T[] input) {
        String s = "";
        for (T t : input) {
            s = s.concat("" + t + "\n");
        }
        append(s);
    }

    public <T> void appendnl(List<T> input) {
        String s = "";
        for (T t : input) {
            s = s.concat("" + t + "\n");
        }
        append(s);
    }

    /**
     * Creates a new line at the end of the file.
     *
     * @throws IOException
     */
    public void newLine() {
        File file = openFile();
        try (BufferedWriter bf = new BufferedWriter(new FileWriter(file, charset, true))) {
            bf.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates the directory named by filePath, including any necessary but nonexistent parent directories.
     *
     * @return true if and only if the directory was created, along with all necessary parent directories; false otherwise
     */
    public boolean makeDir() {
        File file = new File(filePath);
        return file.getParentFile().mkdirs();
    }

    /**
     * Creates an empty file names by filePath, including any necessary but nonexistent parent directories.
     *
     * @return true if and only if the file was created, along with all necessary parent directories; false otherwise
     * @throws IOException
     */
    public boolean makeFile() {
        write("");
        return getFile().exists();
    }

    public String getDirectoryPath() {
        return path;
    }

    public String getFilename() {
        return filename;
    }

    public String getFilePath() {
        return filePath;
    }

    public File getFile() {
        return new File(filePath);
    }

    public Charset getCharset() {
        return charset;
    }

    @Override
    public String toString() {
        return "Path: " + path + "\n" +
                "Filename: " + filename + "\n" +
                "Filepath: " + filePath + "\n" +
                "Charset: " + charset + "\n";
    }
}
