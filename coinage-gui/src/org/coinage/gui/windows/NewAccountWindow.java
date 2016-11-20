package org.coinage.gui.windows;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import org.coinage.core.Resources;
import org.coinage.core.helpers.AccountTreeHelper;
import org.coinage.core.models.Account;
import org.coinage.core.models.AccountClosure;
import org.coinage.gui.ConnectionSourceProvider;
import org.coinage.gui.components.HExpander;
import org.coinage.gui.dialogs.QuickDialogs;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * Created At: 2016-11-15
 */
public class NewAccountWindow extends BaseWindow
{
    private TextField nameBox;
    private Button createBtn;
    private Button cancelBtn;
    private Label messageField;
    private ComboBox<AccountComboItem> parentBox;

    private static class AccountComboItem
    {
        private final String name;
        private final Long id;

        public AccountComboItem(String name, Long id)
        {
            this.name = name;
            this.id = id;
        }

        public String getName()
        {
            return this.name;
        }

        public Long getId()
        {
            return this.id;
        }

        @Override
        public String toString()
        {
            return this.name;
        }
    }

    public NewAccountWindow(Account selectedParent) throws IOException
    {
        this.initialise();

        // load content
        try
        {
            Dao<Account, Long> accountsDao = DaoManager.createDao(ConnectionSourceProvider.get(), Account.class);
            List<AccountTreeHelper.AccountTreeNode> accounts = AccountTreeHelper.buildAccountTree(accountsDao.queryForAll());
            for (AccountTreeHelper.AccountTreeNode node : accounts)
            {
                this.fillContent(node, "", this.parentBox);
            }
            this.parentBox.getItems().sort((o1, o2) -> o1.getName().compareTo(o2.getName()));
            this.parentBox.getItems().add(0, new AccountComboItem("<No Parent>", null));
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }

        int index = 0;
        if (selectedParent != null)
            for (AccountComboItem item : this.parentBox.getItems())
            {
                if (item.getId() != null && item.getId().equals(selectedParent.getId())) break;
                index += 1;
            }
        this.parentBox.getSelectionModel().select(index);
        this.nameBox.requestFocus();
        this.createBtn.defaultButtonProperty().bind(this.createBtn.focusedProperty());
    }

    @Override
    public void initControls()
    {
        this.parentBox = new ComboBox<>();
        this.nameBox = new TextField();
        this.cancelBtn = new Button("Cancel");
        this.createBtn = new Button("Create");
        this.messageField = new Label();
        this.setMessageToFail("Please fill in the fields!");
    }

    @Override
    public Parent initLayout()
    {
        BorderPane layout = new BorderPane();
        layout.setPadding(new Insets(10));

        GridPane formGrid = new GridPane();
        formGrid.add(new Label("Parent:"), 0, 0); formGrid.add(this.parentBox, 1, 0);
        GridPane.setMargin(this.parentBox, new Insets(10, 0, 0, 20));
        formGrid.add(new Label("Account Name:"), 0, 1); formGrid.add(this.nameBox, 1, 1);
        GridPane.setMargin(this.nameBox, new Insets(10, 0, 10, 20));
        formGrid.add(this.messageField, 0, 2);
        GridPane.setColumnSpan(this.messageField, 2);
        GridPane.setHalignment(this.messageField, HPos.RIGHT);
        GridPane.setMargin(this.messageField, new Insets(0, 0, 10, 0));
        layout.setCenter(formGrid);

        layout.setBottom(new HBox(10, new HExpander(), this.cancelBtn, this.createBtn));
        return layout;
    }

    @Override
    public void bindEvents()
    {
        this.cancelBtn.setOnAction(e -> this.getStage().close());
        this.nameBox.textProperty().addListener((observable, oldValue, newValue) -> {
            try
            {
                Account.AssertValidAccountName(newValue);
                setMessageToSuccess();
            }
            catch (AssertionError e)
            {
                setMessageToFail(e.getMessage());
            }
        });
        this.createBtn.setOnAction(e -> {
            try
            {
                Account.AssertValidAccountName(this.nameBox.getText());
                AccountComboItem selected = this.parentBox.getSelectionModel().getSelectedItem();
                Account newAccount = new Account(this.nameBox.getText());
                if (selected.getId() != null)
                {
                    newAccount.setParent(selected.getId());
                }
                Dao<Account, Long> accountDao =  DaoManager.createDao(ConnectionSourceProvider.get(), Account.class);
                accountDao.create(newAccount);
                new AccountTreeHelper(ConnectionSourceProvider.get()).refreshSubTree(newAccount);
                this.getStage().close();
            }
            catch (AssertionError er1)
            {
                QuickDialogs.error("Account name is invalid! The name %s", er1.getMessage());
            }
            catch (SQLException er2)
            {
                QuickDialogs.error("Failed to create new account! %s", er2.getMessage());
            }
        });
    }

    @Override
    public void applyStyling()
    {
        this.messageField.getStyleClass().add("new-account-form-message");
    }

    private void setMessageToFail(String message)
    {
        this.messageField.setText(message);
        this.messageField.getStyleClass().clear();
        this.messageField.getStyleClass().add("new-account-form-message");
        this.messageField.getStyleClass().add("form-message-fail");
    }

    private void setMessageToSuccess()
    {
        this.messageField.setText("Looks Good!");
        this.messageField.getStyleClass().clear();
        this.messageField.getStyleClass().add("new-account-form-message");
        this.messageField.getStyleClass().add("form-message-success");
    }

    @Override
    public Scene initScene()
    {
        Scene scene = new Scene(this.getRootLayout());
        scene.getStylesheets().add(this.cssCommon);
        try { scene.getStylesheets().add(Resources.getExternalPath("/resources/css/new-account-window.css")); } catch (IOException e) { e.printStackTrace(); }
        this.getStage().setTitle("Coinage - New Account");
        this.getStage().setScene(scene);
        this.getStage().setResizable(true);
        return scene;
    }

    private void fillContent(AccountTreeHelper.AccountTreeNode node, String prefix, ComboBox<AccountComboItem> sink)
    {
        String name = prefix + node.value.getName();

        // do self
        sink.getItems().add(new AccountComboItem(name, node.value.getId()));

        // do children
        for (AccountTreeHelper.AccountTreeNode child : node.children)
        {
            this.fillContent(child, name + ".", sink);
        }
    }
}
