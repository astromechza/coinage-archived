package org.coinage.gui.components;

import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;

import java.time.LocalTime;
import java.util.regex.Pattern;

/**
 * Created At: 2016-11-20
 */
public class TimeField extends TextField
{
    private final Pattern timepatt3 = Pattern.compile("^[012]|(?:[012]\\d|2[123]):?$");
    private final Pattern timepatt6 = Pattern.compile("^[012345]|(?:[012345]\\d):?$");
    private final Pattern timepatt8 = Pattern.compile("^[012345]|(?:[012345]\\d)$");

    public final SimpleObjectProperty<LocalTime> timeProperty;

    public TimeField()
    {
        super();
        this.setMaxWidth(80);
        this.timeProperty = new SimpleObjectProperty<>(LocalTime.of(12, 0, 0));
        this.setText("12:00:00");

        /**
         * Filter keys so we only allow digits and decimals.
         */
        this.addEventFilter(KeyEvent.KEY_TYPED, ke -> {
            String sc = ke.getCharacter();
            if (sc.length() > 0)
            {
                char c = sc.charAt(0);
                if (Character.isDigit(c)) return;
                if (c == ':') return;
                ke.consume();
            }
        });

        /**
         * Whenever the change is bad, revert it
         */
        this.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() == 0)
            {
                this.timeProperty.setValue(null);
                return;
            }
            if (newValue.length() > 8)
            {
                this.textProperty().setValue(oldValue);
                return;
            }
            if (newValue.length() > 6)
            {
                String remainder = newValue.substring(6, newValue.length());
                if (!timepatt8.matcher(remainder).matches())
                {
                    this.textProperty().setValue(oldValue);
                    return;
                }
            }
            if (newValue.length() > 3)
            {
                String remainder = newValue.substring(3, Integer.min(newValue.length(), 6));
                if (!timepatt6.matcher(remainder).matches())
                {
                    this.textProperty().setValue(oldValue);
                    return;
                }
            }
            String remainder = newValue.substring(0, Integer.min(newValue.length(), 3));
            if (!timepatt3.matcher(remainder).matches())
            {
                this.textProperty().setValue(oldValue);
            }
            if (newValue.length() == 8)
            {
                this.timeProperty.setValue(this.getTime());
            }
            else
            {
                this.timeProperty.setValue(null);
            }
        });

        this.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue && !newValue)
            {
                this.setText(this.getTimeString());
                this.timeProperty.setValue(this.getTime());
            }
        });


        this.timeProperty.addListener((observable, oldValue, newValue) -> {
            System.out.println(newValue);
        });
    }

    public String getTimeString()
    {
        String content = this.getText();
        return content + "00:00:00".substring(content.length());
    }

    public LocalTime getTime()
    {
        String content = this.getTimeString();
        int hour = Integer.parseInt(content.substring(0, 2));
        int minute = Integer.parseInt(content.substring(3, 5));
        int second = Integer.parseInt(content.substring(6, 8));
        return LocalTime.of(hour, minute, second);
    }

}
