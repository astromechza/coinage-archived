package org.coinage.gui.windows;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.StringConverter;
import org.coinage.core.Resources;
import org.coinage.core.helpers.AccountTreeHelper;
import org.coinage.core.models.Account;
import org.coinage.core.models.SubTransaction;
import org.coinage.core.models.Transaction;
import org.coinage.gui.ConnectionSourceProvider;
import org.coinage.gui.components.CurrencyField;
import org.coinage.gui.components.HExpander;
import org.coinage.gui.components.TimeField;
import org.coinage.gui.dialogs.QuickDialogs;
import org.joda.time.DateTime;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created At: 2016-11-19
 */
public class NewTransactionWindow extends BaseWindow
{
    private DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private DecimalFormat displayFormat = new DecimalFormat("#,##0.00");
    private Button cancelBtn;
    private Button createBtn;
    private Label fromAccountLabel;
    private Label commentLabel;
    private DatePicker dateField;
    private TimeField timeField;

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
    private TextArea commentBox;

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
        this.newToAccountBtn = new Button("+");
        this.totalLabel = new Label("R 0.00");
        this.commentBox = new TextArea();
        cancelBtn = new Button("Cancel");
        createBtn = new Button("Create");
        fromAccountLabel = new Label("From account:");
        commentLabel = new Label("Transaction comment:");
        dateField = new DatePicker();
        dateField.setValue(LocalDate.now());
        timeField = new TimeField();

        dateField.setConverter(new StringConverter<LocalDate>() {
            @Override
            public String toString(LocalDate ld)
            {
                if (ld == null) return "";
                return dateFormat.format(ld);
            }

            @Override
            public LocalDate fromString(String s)
            {
                if (s == null || s.trim().isEmpty()) return null;
                return LocalDate.parse(s, dateFormat);
            }
        });

    }

    @Override
    public Parent initLayout()
    {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        HBox topRow = new HBox(10, fromAccountLabel, fromAccountBox, new HExpander(), totalLabel);
        topRow.setAlignment(Pos.CENTER_LEFT);

        HBox dateRow = new HBox(10, new Label("Transaction Date:"), new HExpander(), dateField, timeField);
        dateRow.setAlignment(Pos.CENTER_LEFT);

        VBox topRows = new VBox(10);
        topRows.getChildren().add(topRow);
        topRows.getChildren().add(dateRow);

        root.setTop(topRows);
        HBox.setHgrow(fromAccountBox, Priority.ALWAYS);
        BorderPane middle = new BorderPane();
        middle.setPadding(new Insets(10, 0, 10, 0));

        HBox toAccountsHeader = new HBox(10, new HExpander(), this.newToAccountBtn);
        toAccountsHeader.setAlignment(Pos.CENTER_LEFT);

        BorderPane pane2 = new BorderPane();
        pane2.setCenter(this.toAccountRows);
        pane2.setBottom(toAccountsHeader);
        BorderPane.setMargin(this.toAccountRows, new Insets(10, 0, 10, 0));

        middle.setCenter(pane2);

        this.commentBox.setPrefSize(400, 100);
        VBox commentVBox = new VBox(10, commentLabel, this.commentBox);
        middle.setBottom(commentVBox);

        root.setCenter(middle);
        root.setBottom(new HBox(10, new HExpander(), cancelBtn, createBtn));
        return root;
    }

    @Override
    public void bindEvents()
    {
        this.newToAccountBtn.setOnAction(event -> this.addNewAccountRow());
        this.cancelBtn.setOnAction(event -> this.getStage().close());
        this.createBtn.setOnAction(event -> {
            if (this.fromAccountBox.getSelectionModel().isEmpty())
            {
                QuickDialogs.error("Please select a from account!");
                return;
            }

            Set<Long> mentionedAccounts = new HashSet<>();
            for (Node n : toAccountRows.getChildren())
            {
                HBox hb = (HBox)n;
                ComboBox<AccountComboItem> cb = (ComboBox<AccountComboItem>)hb.getChildren().get(0);
                CurrencyField cf = (CurrencyField)hb.getChildren().get(2);
                if (cf.getDecimal() == null || cf.getDecimal().equals(BigDecimal.ZERO))
                {
                    QuickDialogs.error("One of your currency field inputs is empty or zero!");
                    return;
                }
                else if (cb.getSelectionModel().isEmpty())
                {
                    QuickDialogs.error("One of your account select inputs is empty!");
                    return;
                }
                else if (cb.getSelectionModel().getSelectedIndex() == fromAccountBox.getSelectionModel().getSelectedIndex())
                {
                    QuickDialogs.error("One of your account select inputs is equal to the from account!");
                    return;
                }
                Long l = cb.getSelectionModel().getSelectedItem().getId();
                if (mentionedAccounts.contains(l))
                {
                    QuickDialogs.error("You've selected the same account more than once, please combine those!");
                    return;
                }
                mentionedAccounts.add(l);
            }



            LocalDate selectedDate = dateField.valueProperty().get();
            if (selectedDate == null)
            {
                QuickDialogs.error("Please fill in the date field!");
                return;
            }

            LocalTime selectedTime = timeField.timeProperty.get();
            if (selectedTime == null)
            {
                QuickDialogs.error("Please fill in the time field!");
                return;
            }

            LocalDateTime ldt = selectedDate.atTime(selectedTime);
            DateTime dt = new DateTime(ldt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());

            try
            {
                Dao<Transaction, Long> transactionDao = DaoManager.createDao(ConnectionSourceProvider.get(), Transaction.class);
                Dao<SubTransaction, Long> subtransactionDao = DaoManager.createDao(ConnectionSourceProvider.get(), SubTransaction.class);
                Transaction transaction = new Transaction(dt, commentBox.getText().trim());
                transaction.setSubTransactions(transactionDao.getEmptyForeignCollection("subtransactions"));
                transactionDao.create(transaction);

                Account fromA = new Account(fromAccountBox.selectionModelProperty().get().getSelectedItem().getId());
                BigDecimal inversion = BigDecimal.ZERO;
                List<SubTransaction> subtransactions = new ArrayList<>();
                for (Node n : toAccountRows.getChildren())
                {
                    HBox hb = (HBox)n;
                    ComboBox<AccountComboItem> cb = (ComboBox<AccountComboItem>)hb.getChildren().get(0);
                    CurrencyField cf = (CurrencyField)hb.getChildren().get(2);

                    Account toA = new Account(cb.selectionModelProperty().get().getSelectedItem().getId());
                    subtransactions.add(new SubTransaction(transaction, toA, fromA, cf.getDecimal()));
                    inversion = inversion.add(cf.getDecimal().negate());
                }
                subtransactions.add(new SubTransaction(transaction, fromA, inversion));
                subtransactionDao.create(subtransactions);
                this.getStage().close();
            }
            catch (SQLException e)
            {
                e.printStackTrace();
            }

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
        this.getStage().setResizable(false);
        return scene;
    }

    private void addNewAccountRow()
    {
        Button pop = new Button("X");
        CurrencyField currfield = new CurrencyField('R');
        ComboBox<AccountComboItem> accountSelector = new ComboBox<>();
        accountSelector.setItems(this.accountItems);
        HBox.setHgrow(accountSelector, Priority.ALWAYS);
        HBox row = new HBox(10, accountSelector, new HExpander(), currfield, pop);
        pop.setOnAction(event -> {
            if (this.toAccountRows.getChildren().size() > 1)
            {
                this.toAccountRows.getChildren().remove(row);
                this.getStage().sizeToScene();
            }
            this.checkPopDisabled();
        });
        currfield.valueProperty.addListener((observable, oldValue, newValue) -> {
            recalculateTotal();
        });
        this.toAccountRows.getChildren().add(row);
        this.checkPopDisabled();
        this.getStage().sizeToScene();
    }

    private void recalculateTotal()
    {
        final BigDecimal[] total = { BigDecimal.ZERO };
        this.toAccountRows.getChildren().stream()
            .map(node -> ((CurrencyField)((HBox) node).getChildren().get(2)).getDecimal())
            .forEach(value -> {
                if (value != null) total[0] = total[0].add(value);
            });

        totalLabel.setText("R " + displayFormat.format(total[0].negate()));
    }

    private void checkPopDisabled()
    {
        boolean popDisabled = this.toAccountRows.getChildren().size() <= 1;
        this.toAccountRows.getChildren().stream().forEach(
                node -> ((HBox) node).getChildren().get(3).setDisable(popDisabled));
    }
}
