package org.coinage.gui.components;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import org.coinage.core.models.Account;

import java.util.*;

/**
 * Created At: 2016-11-23
 */
public class AccountAutoCompleteComboBox extends ComboBox<AccountAutoCompleteItem>
{
    private List<AccountAutoCompleteItem> data;

    public AccountAutoCompleteComboBox()
    {
        this(new HashMap<>());
    }

    public AccountAutoCompleteComboBox(Map<Long, String> nameMap)
    {
        this.setContent(nameMap);
        this.setEditable(true);
        this.setTooltip(new Tooltip("Select an account name"));
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
                boolean showTooltip = false;
                try
                {
                    Account.AssertValidAccountTree(newText);

                    if (data.stream().noneMatch(i -> i.getFullName().equals(newText)))
                    {
                        this.getEditor().setStyle("-fx-background-color: #eeeeff");
                        this.getTooltip().setText("Will be created!");
                        showTooltip = true;
                    }
                    else
                    {
                        this.getEditor().setStyle("");
                    }
                }
                catch (AssertionError e)
                {
                    this.getEditor().setStyle("-fx-background-color: red");
                    this.getTooltip().setText(String.format("Invalid: %s", e.getMessage()));
                    showTooltip = true;
                }

                if (showTooltip)
                {
                    if (!this.getTooltip().isShowing())
                    {
                        Point2D p = this.localToScene(this.getWidth(), this.getHeight());
                        this.getTooltip().show(
                                this,
                                p.getX() + this.getScene().getX() + this.getScene().getWindow().getX(),
                                p.getY() + this.getScene().getY() + this.getScene().getWindow().getY());
                    }
                }
                else
                {
                    this.getTooltip().hide();
                }

            }
            else
            {
                this.getTooltip().hide();
                this.getEditor().setStyle("");
            }
        });

        this.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            AccountAutoCompleteComboBox.this.getEditor().positionCaret(AccountAutoCompleteComboBox.this.getEditor().getText().length());
            this.getEditor().setStyle("");
        });

        this.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (! newValue)
            {
                if (this.isShowing() && this.getItems().size() > 0)
                {
                    this.getSelectionModel().select(0);
                }

                AccountAutoCompleteComboBox.this.getTooltip().hide();
            }
        });
    }

    public void setContent(Map<Long, String> nameMap)
    {
        data = new ArrayList<>();
        for (Map.Entry<Long, String> entry : nameMap.entrySet())
        {
            data.add(new AccountAutoCompleteItem(entry.getKey(), entry.getValue()));
        }
        data.sort(Comparator.comparing(AccountAutoCompleteItem::getFullName));

        ObservableList<AccountAutoCompleteItem> list = FXCollections.observableArrayList();
        data.forEach(list::add);
        this.setItems(list);
    }

    public Long getSelectedAccount()
    {
        if (this.getSelectionModel().isEmpty())
        {
            Optional<AccountAutoCompleteItem> o = data.stream().filter(i -> i.getFullName().equals(this.getEditor().getText())).findAny();
            return o.map(AccountAutoCompleteItem::getAccountId).orElse(null);
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
