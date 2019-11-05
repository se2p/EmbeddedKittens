import analytics.IssueFinder;
import analytics.IssueTool;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.cli.*;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.FilenameUtils;
import scratch.newast.ParsingException;
import scratch.newast.model.Program;
import scratch.newast.parser.ProgramParser;
import scratch.structure.Project;
import utils.CSVWriter;
import utils.JsonParser;
import utils.ZipReader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Main {

    //"C:\\scratchprojects\\files3\\"

    public static void main(String[] args) throws ParseException {

        Options options = new Options();

        options.addOption("path", true, "path to folder or file that should be analyzed (required)");
        options.addOption("output", true, "path with name of the csv file you want to save (required if path argument" +
                " is a folder path)");
        options.addOption("detectors", true, "name all detectors you want to run separated by ',' " +
                "\n(all detectors defined in the README)");
        options.addOption("version", true, "the Scratch Version ('2' or '3') (required)");
        CommandLineParser parser = new DefaultParser();

        CommandLine cmd = parser.parse(options, args);

        if (cmd.hasOption("path") && cmd.hasOption("version")) {
            File folder = new File(cmd.getOptionValue("path"));
            if (folder.exists() && folder.isDirectory()) {
                checkMultiple(folder, cmd.getOptionValue("detectors", "all"),
                        cmd.getOptionValue("output"), cmd.getOptionValue("version"));
            } else if (folder.exists() && !folder.isDirectory()) {
                checkSingle(folder, cmd.getOptionValue("detectors",
                        "all"), cmd.getOptionValue("output"), cmd.getOptionValue("version"));
            } else {
                System.err.println("[Error] folder path given does not exist");
                return;
            }
            return;
        }
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("LitterBox", options);
        System.out.println("Example: " + "java -cp C:\\ScratchAnalytics-1.0.jar Main -path " +
                "C:\\scratchprojects\\files\\ -version 3 -output C:\\scratchprojects\\files\\test.csv -detectors cnt," +
                "glblstrt");
    }

    /**
     * The method for analyzing one Scratch project file (ZIP).
     * It will produce only console output.
     *
     * @param fileEntry the file to analyze
     * @param detectors
     */
    private static void checkSingle(File fileEntry, String detectors, String csv, String version) {
        try {

            Project project = getProject(fileEntry, version);
            Program program = extractProgram(fileEntry);

            //System.out.println(project.toString());
            IssueTool iT = new IssueTool();
            if (csv == null || csv.equals("")) {
                iT.checkRaw(project, detectors);
            } else {
                CSVPrinter printer = prepareCSVPrinter(detectors, iT, csv);
                iT.check(project, printer, detectors);
                System.out.println("Finished: " + fileEntry.getName());
                try {
                    assert printer != null;
                    CSVWriter.flushCSV(printer);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static CSVPrinter prepareCSVPrinter(String dtctrs, IssueTool iT, String name) {
        List<String> heads = new ArrayList<>();
        heads.add("project");
        String[] detectors;
        if (dtctrs.equals("all")) {
            detectors = iT.getFinder().keySet().toArray(new String[0]);
        } else {
            detectors = dtctrs.split(",");
        }
        for (String s : detectors) {
            if (iT.getFinder().containsKey(s)) {
                IssueFinder iF = iT.getFinder().get(s);
                heads.add(iF.getName());
            }
        }
        try {
            return CSVWriter.getNewPrinter(name, heads);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * The main method for analyzing all Scratch project files (ZIP) in the given folder location.
     * It will produce a .csv file with all entries.
     */
    private static void checkMultiple(File folder, String dtctrs, String csv, String version) {

        CSVPrinter printer = null;
        try {
            String name = csv;
            IssueTool iT = new IssueTool();
            printer = prepareCSVPrinter(dtctrs, iT, name);
            for (final File fileEntry : Objects.requireNonNull(folder.listFiles())) {
                if (!fileEntry.isDirectory()) {
                    Program program = extractProgram(fileEntry);
                    Project project = getProject(fileEntry, version);
                    //System.out.println(project.toString());
                    iT.check(project, printer, dtctrs);
                    System.out.println("Finished: " + fileEntry.getName());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                assert printer != null;
                CSVWriter.flushCSV(printer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static Project getProject(File fileEntry, String version) {
        System.out.println("Starting: " + fileEntry);
        Project project = null;
        try {
            if (version.equals("2")) {
                project = JsonParser.parse2(fileEntry.getName(), fileEntry.getPath());
            } else if (version.equals("3")) {
                project = JsonParser.parse3(fileEntry.getName(), fileEntry.getPath());
            }
            assert project != null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return project;
    }

    /**
     * This main method was used for analyzing a repository with 250.000 projects, that only contained the JSON files.
     * A Scratch project is normally zipped and contains pictures, sounds and the JSON.
     * This method uses a different JsonParser method which takes a raw JSON file and not the ZIP file.
     * Also it creates .csv files with 10.000 entries and 25 files in total.
     */
    public static void mainJson(File folder) {
        CSVPrinter printer;
        try {
            String name = "./dataset0.csv";
            Project project = null;
            IssueTool iT = new IssueTool();
            List<String> heads = new ArrayList<>();
            heads.add("project");
            for (IssueFinder iF : iT.getFinder().values()) {
                heads.add(iF.getName());
            }
            printer = CSVWriter.getNewPrinter(name, heads);
            int count = 0;
            int datacount = 0;
            for (final File fileEntry : Objects.requireNonNull(folder.listFiles())) {
                if (!fileEntry.isDirectory()) {
                    System.out.println(fileEntry);
                    try {
                        project = JsonParser.parseRaw(fileEntry);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (project != null) {
                        //System.out.println(project.toString());2
                        iT.check(project, printer, "all");
                        count++;
                    }
                }
                if (count == 10000) {
                    if (count == Objects.requireNonNull(folder.listFiles()).length - 1 || datacount == 24) {
                        CSVWriter.flushCSV(printer);
                        return;
                    }
                    CSVWriter.flushCSV(printer);
                    count = 0;
                    System.out.println("Finished: " + name);
                    datacount++;
                    name = "./dataset" + datacount + ".csv";
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Program extractProgram(File fileEntry) {
        ObjectMapper mapper = new ObjectMapper();
        Program program = null;
        if ((FilenameUtils.getExtension(fileEntry.getPath())).toLowerCase().equals("json")) {
            try {
                program = ProgramParser.parseProgram(fileEntry.getName(), mapper.readTree(fileEntry));
            } catch (ParsingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            JsonNode node = null;
            try {
                node = JsonParser.getTargetsNodeFromJSONString(ZipReader.getJsonString(fileEntry.getPath()));
                if (node == null) {
                    System.err.println("[Error] project json did not contain root node");
                }
                program = ProgramParser.parseProgram(fileEntry.getName(), node);
            } catch (ParsingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return program;
    }
}
