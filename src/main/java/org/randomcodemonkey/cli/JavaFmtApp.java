package org.randomcodemonkey.cli;

import com.google.googlejavaformat.java.Formatter;
import com.google.googlejavaformat.java.FormatterException;
import com.google.googlejavaformat.java.StringWrapper;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/**
 * A command line utility to format java source code
 *
 * @author Ilkka Kaakkola
 */
@Command(
    name = "javafmt",
    description = "Format Java sources with 'google-java-fmt'",
    mixinStandardHelpOptions = true)
public class JavaFmtApp implements Runnable {

  protected static final String JAVA_EXTENSION = ".java";

  private static final FileFilter FILE_FILTER =
      new FileFilter() {

        @Override
        public boolean accept(File pathname) {
          if (pathname.isDirectory()) {
            return true;
          }
          return pathname.getName().endsWith(JAVA_EXTENSION);
        }
      };

  @Option(
      required = false,
      names = {"-r", "--recursive"},
      description = "Recurse into directories")
  private boolean recursive;

  @Option(
      required = false,
      names = {"-s", "--split"},
      description = "Split long strings into multiple lines")
  private boolean reflowStrings;

  @Option(
      required = false,
      names = {"-v", "--verbose"},
      description = "Verbose output (default: false)",
      defaultValue = "false")
  private boolean verbose;

  @Parameters(
      arity = "1..*",
      paramLabel = "files",
      description = "one or more files or directories to format. Use '-' to read from STDIN")
  private File[] files;

  @Override
  public void run() {
    if ("-".equals(files[0].getName())) {
      formatPiped();
      return;
    }

    for (File file : files) {
      if (!file.exists()) {
        continue;
      }
      format(file);
    }
  }

  private void formatPiped() {
    StringBuilder source = new StringBuilder();

    try {
      int ch;
      while ((ch = System.in.read()) != -1) {
        source.append((char) ch);
      }
      System.out.println(format(source.toString()));
    } catch (FormatterException | IOException e) {
      throw new IllegalStateException(String.format("Could not read STDIN: %s", e.getMessage()), e);
    }
  }

  private void format(File file) {
    if (file.isDirectory()) {
      if (!recursive) {
        if (verbose) {
          System.out.println(
              String.format("File %s is a directory and recursive mode is not enabled", file));
        }
        return;
      }

      for (File child : file.listFiles(FILE_FILTER)) {
        format(child);
      }
      return;
    }

    if (!file.getName().endsWith(JAVA_EXTENSION)) {
      return;
    }

    if (verbose) {
      System.out.println(String.format("checking %s", file));
    }
    try {
      String source = new String((Files.readAllBytes(file.toPath())));
      String formatted = format(source);
      if (source.equals(formatted)) {
        if (verbose) {
          System.out.println(String.format(" - [no change] %s", file));
        }
        return;
      }
      Files.writeString(file.toPath(), formatted, StandardOpenOption.TRUNCATE_EXISTING);
      if (verbose) {
        System.out.println(String.format(" - [formatted] %s", file));
      }
    } catch (FormatterException | IOException e) {
      throw new IllegalStateException(
          String.format("Could not format %s: %s", file.toString(), e.getMessage()), e);
    }
  }

  private String format(String source) throws FormatterException {
    Formatter formatter = new Formatter();
    String formatted = formatter.formatSource(source);
    if (reflowStrings) {
      formatted = StringWrapper.wrap(formatted, formatter);
    }
    return formatted;
  }

  public static void main(String[] args) throws Exception {
    CommandLine cli = new CommandLine(new JavaFmtApp());
    System.exit(cli.execute(args));
  }
}
