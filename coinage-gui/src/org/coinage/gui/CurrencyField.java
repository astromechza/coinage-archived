package org.coinage.gui;


import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;

import java.math.BigDecimal;
import java.util.regex.Pattern;


/**
 * Created At: 2016-11-19
 */
public class CurrencyField extends TextField
{
    private final Pattern currencyPattern = Pattern.compile("(?:(?:[1-9]\\d*)|0)(?:\\.\\d*)?");
    public final SimpleObjectProperty<BigDecimal> valueProperty;
    public CurrencyField(Character currencySymbol)
    {
        super();
        this.valueProperty = new SimpleObjectProperty<>(null);

        Label childLabel = new Label("" + currencySymbol);
        childLabel.setMaxWidth(Double.MAX_VALUE);
        childLabel.setStyle("-fx-padding: 5 5 5 -15");
        this.getChildren().add(childLabel);
        this.setStyle("-fx-padding: 5 5 5 20");

        /**
         * Filter keys so we only allow digits and decimals.
         */
        this.addEventFilter(KeyEvent.KEY_TYPED, ke -> {
            String sc = ke.getCharacter();
            if (sc.length() > 0)
            {
                char c = sc.charAt(0);
                if (Character.isDigit(c)) return;
                if (c == '.') return;
                ke.consume();
            }
        });

        /**
         * Whenever the change is bad, revert it
         */
        this.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > 0 && !currencyPattern.matcher(newValue).matches())
            {
                textProperty().setValue(oldValue);
            }
            else
            {
                valueProperty.setValue(this.getDecimal());
            }
        });
    }

    public String getDecimalString()
    {
        String current = this.getText();
        if (current.length() == 0) return null;
        if (current.charAt(current.length() - 1) == '.') current += "0";
        return current;
    }

    public BigDecimal getDecimal()
    {
        String current = this.getDecimalString();
        if (current == null) return null;
        return new BigDecimal(current);
    }
}
