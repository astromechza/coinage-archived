package org.coinage.gui.windows;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import org.coinage.core.Resources;
import org.coinage.core.helpers.AccountTreeHelper;
import org.coinage.core.models.Account;
import org.coinage.gui.ConnectionSourceProvider;
import org.coinage.gui.CurrencyField;
import org.coinage.gui.components.HExpander;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created At: 2016-11-19
 */
public class NewTransactionWindow extends BaseWindow
{
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

    private ObservableList<AccountComboItem> accountItems;
    private ComboBox<AccountComboItem> fromAccountBox;
    private Button newToAccountBtn;
    private VBox toAccountRows;
    private Label totalLabel;

    public NewTransactionWindow() throws IOException
    {
        this.initialise();
        this.fillAccountsList();
        this.fromAccountBox.setItems(this.accountItems);

        this.addNewAccountRow();
    }

    private void fillAccountsList()
    {
        List<AccountComboItem> sink = new ArrayList<>();
        try
        {
            Dao<Account, Long> accountDao = DaoManager.createDao(ConnectionSourceProvider.get(), Account.class);
            List<AccountTreeHelper.AccountTreeNode> accounts = AccountTreeHelper.buildAccountTree(accountDao.queryForAll());
            for (AccountTreeHelper.AccountTreeNode node : accounts)
            {
                this.fillAccountsList(node, "", sink);
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }

        sink.sort((o1, o2) -> o1.getName().compareTo(o2.getName()));
        this.accountItems = new SimpleListProperty<>(FXCollections.observableArrayList(sink));
    }

    private void fillAccountsList(AccountTreeHelper.AccountTreeNode node, String prefix, List<AccountComboItem> sink)
    {
        String name = prefix + node.value.getName();

        // do self
        sink.add(new AccountComboItem(name, node.value.getId()));

        // do children
        for (AccountTreeHelper.AccountTreeNode child : node.children)
        {
            this.fillAccountsList(child, name + ".", sink);
        }
    }

    @Override
    public void initControls()
    {
        this.fromAccountBox = new ComboBox<>();
        this.toAccountRows = new VBox(10);
        this.newToAccountBtn = new Button("Add new row");
        this.totalLabel = new Label("0.00");
    }

    @Override
    public Parent initLayout()
    {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        root.setTop(new HBox(10,
            new Label("From Account:"),
            fromAccountBox,
            new HExpander(),
            totalLabel
        ));
        HBox.setHgrow(fromAccountBox, Priority.ALWAYS);
        BorderPane middle = new BorderPane();
        middle.setTop(new HBox(10, new Label("To Accounts"), new HExpander(), this.newToAccountBtn));
        middle.setCenter(this.toAccountRows);
        root.setCenter(middle);

        root.setBottom(new HBox(10,
                new HExpander(),
                new Button("Cancel"),
                new Button("Create")
        ));

        return root;
    }

    @Override
    public void bindEvents()
    {
        this.newToAccountBtn.setOnAction(event -> {
            this.addNewAccountRow();
        });
    }

    @Override
    public void applyStyling()
    {

    }

    @Override
    public Scene initScene()
    {
        Scene scene = new Scene(this.getRootLayout());
        scene.getStylesheets().add(this.cssCommon);
        try { scene.getStylesheets().add(Resources.getExternalPath("/resources/css/new-transaction-window.css")); } catch (IOException e) { e.printStackTrace(); }
        this.getStage().setTitle("Coinage - New Transaction");
        this.getStage().setScene(scene);
        this.getStage().setResizable(true);
        this.getStage().setMinWidth(400);
        this.getStage().setMinHeight(300);
        return scene;
    }

    private void addNewAccountRow()
    {
        Button pop = new Button("X");
        CurrencyField currfield = new CurrencyField('R');
        ComboBox<AccountComboItem> accountSelector = new ComboBox<>();
        accountSelector.setItems(this.accountItems);
        HBox row = new HBox(10, accountSelector, currfield, pop);
        pop.setOnAction(event -> {
            if (this.toAccountRows.getChildren().size() > 1)
            {
                this.toAccountRows.getChildren().remove(row);
            }
            this.checkPopDisabled();
        });
        currfield.valueProperty.addListener((observable, oldValue, newValue) -> {
            recalculateTotal();
        });
        this.toAccountRows.getChildren().add(row);
        this.checkPopDisabled();
    }

    private void recalculateTotal()
    {
        final BigDecimal[] total = { BigDecimal.ZERO };
        this.toAccountRows.getChildren().stream()
            .map(node -> ((CurrencyField)((HBox) node).getChildren().get(1)).getDecimal())
            .forEach(value -> {
                if (value != null) total[0] = total[0].add(value);
            });
        totalLabel.setText("R " + total[0].negate().toString());
    }

    private void checkPopDisabled()
    {
        boolean popDisabled = this.toAccountRows.getChildren().size() <= 1;
        this.toAccountRows.getChildren().stream().forEach(
                node -> ((HBox) node).getChildren().get(2).setDisable(popDisabled));
    }
}
