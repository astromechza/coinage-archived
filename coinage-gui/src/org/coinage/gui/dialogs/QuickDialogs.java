package org.coinage.gui.dialogs;

import javafx.scene.control.*;
import javafx.stage.Modality;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Optional;

/**
 * Created At: 2016-11-15
 */
public class QuickDialogs
{
    private static final Logger LOG = LoggerFactory.getLogger(QuickDialogs.class);

    public static void info(String title, String header, String format, Object... args)
    {
        LOG.info(format, args);
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(String.format(format, args));
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.showAndWait();
    }

    public static void info(String format, Object... args)
    {
        info("Info", null, format, args);
    }

    public static boolean confirmFull(String title, String header, String format, Object... args)
    {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(String.format(format, args));
        ButtonType positiveButton = new ButtonType("Yes");
        ButtonType negativeButton = new ButtonType("No");
        alert.getButtonTypes().setAll(positiveButton, negativeButton);
        alert.initModality(Modality.APPLICATION_MODAL);
        Optional<ButtonType> result = alert.showAndWait();
        return result.get() == positiveButton;
    }

    public static boolean confirm(String format, Object... args)
    {
        return confirmFull("Input Required", null, format, args);
    }

    public static void exception(Throwable e)
    {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Exception thrown");
        alert.setHeaderText(e.getClass().getName());
        alert.setContentText(e.getMessage());

        // Create stacktrace
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);

        // Log to stdout
        LOG.error("Exception", e);

        TextArea textArea = new TextArea(sw.toString());
        textArea.setEditable(false);
        textArea.setWrapText(true);
        alert.getDialogPane().setExpandableContent(textArea);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.showAndWait();
    }

    public static String input(String content, String placeholder)
    {
        TextInputDialog dialog = new TextInputDialog(placeholder);
        dialog.setTitle("Input Required");
        dialog.setHeaderText(null);
        dialog.setContentText(content);
        dialog.initModality(Modality.APPLICATION_MODAL);

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent())
        {
            return result.get();
        }
        return null;
    }

    public static void error(String title, String heading, String message, Object... args)
    {
        LOG.error(message, args);
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(heading);
        alert.setContentText(String.format(message, args));
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.showAndWait();
    }

    public static void error(String format, Object... args)
    {
        error("Error", "An Error occured", format, args);
    }

    public static <T> T pick(String title, String heading, String content, List<T> items, T initial)
    {
        ChoiceDialog<T> dialog = new ChoiceDialog<>(initial, items);
        dialog.setTitle(title);
        dialog.setHeaderText(heading);
        dialog.setContentText(content);
        dialog.initModality(Modality.APPLICATION_MODAL);
        Optional<T> result = dialog.showAndWait();
        if (result.isPresent())
        {
            return result.get();
        }
        return initial;
    }

    public static <T> T pick(String content, List<T> items, T initial)
    {
        return pick("Select a choice", null, content, items, initial);
    }
}
