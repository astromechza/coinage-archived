package org.coinage.gui.components;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import org.coinage.core.models.Account;
import org.coinage.gui.AccountAutoCompleteItem;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Created At: 2016-11-23
 */
public class AccountAutoCompleteComboBox extends ComboBox<AccountAutoCompleteItem>
{
    private List<AccountAutoCompleteItem> data;

    public AccountAutoCompleteComboBox(Map<Long, String> nameMap)
    {
        this.setEditable(true);
        data = new ArrayList<>();
        for (Map.Entry<Long, String> entry : nameMap.entrySet())
        {
            data.add(new AccountAutoCompleteItem(entry.getKey(), entry.getValue()));
            data.sort(Comparator.comparing(AccountAutoCompleteItem::getFullName));
        }
        this.setTooltip(new Tooltip("Select an account name"));
        this.setCursor(Cursor.HAND);
        this.getTooltip().setAutoHide(true);
        this.getTooltip().setHideOnEscape(true);
        this.setItems(FXCollections.observableArrayList(data));
        this.setPromptText("Type or select an account");
        this.setOnKeyPressed(t -> AccountAutoCompleteComboBox.this.hide());
        this.setOnKeyReleased(event -> {

            if (event.getCode() == KeyCode.RIGHT || event.getCode() == KeyCode.LEFT
                    || event.getCode() == KeyCode.UP || event.getCode() == KeyCode.DOWN
                    || event.isControlDown() || event.getCode() == KeyCode.HOME
                    || event.getCode() == KeyCode.END || event.getCode() == KeyCode.TAB) return;

            final String newText = this.getEditor().getText();
            this.getSelectionModel().clearSelection();
            this.getEditor().setText(newText);
            this.getEditor().positionCaret(newText.length());

            ObservableList<AccountAutoCompleteItem> list = FXCollections.observableArrayList();
            data.stream().filter(item -> item.containsFilter(newText)).forEach(list::add);

            int sizeBefore = this.getItems().size();
            if (sizeBefore != list.size()) this.hide();
            this.setItems(list);
            if (! list.isEmpty()) this.show();
            if (! newText.isEmpty())
            {
                try
                {
                    Account.AssertValidAccountTree(newText);
                    this.getEditor().setStyle("");
                    this.getTooltip().hide();

                    if (data.stream().noneMatch(i -> i.getFullName().equals(newText)))
                    {
                        this.getEditor().setStyle("-fx-background-color: #eeeeff");
                    }
                }
                catch (AssertionError e)
                {
                    this.getEditor().setStyle("-fx-background-color: red");
                    this.getTooltip().setText(String.format("Invalid: %s", e.getMessage()));

                    if (!this.getTooltip().isShowing())
                    {
                        Point2D p = this.localToScene(this.getWidth(), this.getHeight());
                        this.getTooltip().show(
                                this,
                                p.getX() + this.getScene().getX() + this.getScene().getWindow().getX(),
                                p.getY() + this.getScene().getY() + this.getScene().getWindow().getY());
                    }
                }
            }
        });

        this.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            AccountAutoCompleteComboBox.this.getEditor().positionCaret(AccountAutoCompleteComboBox.this.getEditor().getText().length());
            this.getEditor().setStyle("");
        });
    }

    public Long getSelectedAccount()
    {
        if (this.getSelectionModel().isEmpty())
        {
            return null;
        }
        return this.getSelectionModel().getSelectedItem().getAccountId();
    }

    public String getSelectedAccountName()
    {
        if (this.getSelectionModel().isEmpty())
        {
            return this.getEditor().getText();
        }
        return this.getSelectionModel().getSelectedItem().getFullName();
    }
}
