package org.coinage.gui;

import com.j256.ormlite.jdbc.JdbcConnectionSource;
import javafx.application.Application;
import javafx.stage.Stage;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import org.coinage.core.Version;
import org.coinage.gui.windows.MainWindow;

import java.io.File;
import java.net.URISyntaxException;

/**
 * Created At: 2016-11-13
 */
public class GuiMain
{
    public static void main(String[] args) throws ArgumentParserException
    {
        // Constructing parser and subcommands
        ArgumentParser parser = ArgumentParsers.newArgumentParser("bunkr");

        String entrypoint = GuiMain.class.getName();
        try
        {
            entrypoint = new File(GuiMain.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getName();
        }
        catch (URISyntaxException ignored) { }

        parser.version(
                String.format("%s\nversion: %s\ncommit date: %s\ncommit hash: %s",
                              entrypoint,
                              Version.versionString,
                              Version.gitDate,
                              Version.gitHash));
        parser.addArgument("--version").action(Arguments.version());
        parser.addArgument("--logging")
                .action(Arguments.storeTrue())
                .type(Boolean.class)
                .setDefault(false)
                .help("Enable debug logging. This may be a security issue due to information leakage.");
        parser.addArgument("--database")
                .type(String.class)
                .help("Open a particular database by file path");

        Namespace namespace = parser.parseArgs(args);
        String[] params = new String[]{namespace.get("database")};
        MainApplication.launch(MainApplication.class, params);
    }

    public static class MainApplication extends Application
    {
        @Override
        public void start(Stage primaryStage) throws Exception
        {
            ConnectionSourceProvider.set(new JdbcConnectionSource("jdbc:sqlite::memory:"));
            MainWindow window = new MainWindow(primaryStage);
            window.getStage().show();
            // TODO open the sqlite database given by getParameters().getRaw().get(0);
        }
    }
}
