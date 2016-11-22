package org.coinage.gui.components;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Background;
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
    private boolean moveCaretToPos = false;
    private int caretPos;

    public AccountAutoCompleteComboBox(Map<Long, String> nameMap)
    {
        this.setEditable(true);
        data = new ArrayList<>();
        for (Map.Entry<Long, String> entry : nameMap.entrySet())
        {
            data.add(new AccountAutoCompleteItem(entry.getKey(), entry.getValue()));
            data.sort(Comparator.comparing(AccountAutoCompleteItem::getFullName));
        }
        this.setItems(FXCollections.observableArrayList(data));
        this.setPromptText("Type or select an account");
        this.setOnKeyPressed(t -> AccountAutoCompleteComboBox.this.hide());
        this.setOnKeyReleased(event -> {

            if (event.getCode() == KeyCode.UP)
            {
                caretPos = -1;
                moveCaret(this.getEditor().getText().length());
                return;
            }
            else if (event.getCode() == KeyCode.DOWN)
            {
                if (!this.isShowing()) this.show();
                caretPos = -1;
                moveCaret(this.getEditor().getText().length());
                return;
            }
            else if (event.getCode() == KeyCode.BACK_SPACE)
            {
                moveCaretToPos = true;
                caretPos = this.getEditor().getCaretPosition();
            }
            else if (event.getCode() == KeyCode.DELETE)
            {
                moveCaretToPos = true;
                caretPos = this.getEditor().getCaretPosition();
            }

            if (event.getCode() == KeyCode.RIGHT || event.getCode() == KeyCode.LEFT
                    || event.isControlDown() || event.getCode() == KeyCode.HOME
                    || event.getCode() == KeyCode.END || event.getCode() == KeyCode.TAB) return;

            ObservableList<AccountAutoCompleteItem> list = FXCollections.observableArrayList();
            for (AccountAutoCompleteItem d : data)
            {
                if (d.startsWithPrefix(AccountAutoCompleteComboBox.this.getEditor().getText()))
                {
                    list.add(d);
                }
            }

            String t = this.getEditor().getText();
            this.setItems(list);

            try
            {
                Account.AssertValidAccountTree(t);
                this.getEditor().setStyle("");
            }
            catch (AssertionError e)
            {
                this.getEditor().setStyle("-fx-background-color: red");
            }

            if (!moveCaretToPos) caretPos = -1;
            moveCaret(t.length());
            if (!list.isEmpty()) this.show();
        });
    }

    private void moveCaret(int textLength) {
        if(caretPos == -1) {
            this.getEditor().positionCaret(textLength);
        } else {
            this.getEditor().positionCaret(caretPos);
        }
        moveCaretToPos = false;
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
