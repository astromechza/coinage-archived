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
import org.coinage.gui.components.AccountAutoCompleteComboBox;
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
import java.util.*;

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
    private Map<Long, String> nameMap;

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
    private AccountAutoCompleteComboBox fromAccountBox;
    private Button newToAccountBtn;
    private VBox toAccountRows;
    private Label totalLabel;
    private TextArea commentBox;

    public NewTransactionWindow() throws IOException
    {
        this.initialise();
        this.fillAccountsList();

        this.addNewAccountRow();
    }

    private void fillAccountsList()
    {
        try
        {
            nameMap = new AccountTreeHelper(ConnectionSourceProvider.get()).nameMap();
            this.fromAccountBox.setContent(nameMap);
        }
        catch (SQLException e)
        {
            QuickDialogs.exception(e, "error while fetching name map for new transactions");
        }
    }

    @Override
    public void initControls()
    {
        this.fromAccountBox = new AccountAutoCompleteComboBox();
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

        HBox topRow = new HBox(10, fromAccountLabel, fromAccountBox, totalLabel);
        topRow.setAlignment(Pos.CENTER_LEFT);
        fromAccountBox.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(fromAccountBox, Priority.ALWAYS);

        HBox dateRow = new HBox(10, new Label("Transaction Date:"), new HExpander(), dateField, timeField);
        dateRow.setAlignment(Pos.CENTER_LEFT);

        VBox topRows = new VBox(10);
        topRows.getChildren().add(topRow);
        topRows.getChildren().add(dateRow);

        root.setTop(topRows);
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

            // capture and validate the FROM account
            Long fromAccountId = this.fromAccountBox.getSelectedAccount();
            String fromAccountName = this.fromAccountBox.getSelectedAccountName();
            if (fromAccountId == null)
            {
                if (fromAccountName.isEmpty())
                {
                    this.fromAccountBox.requestFocus();
                    QuickDialogs.error("Please select an account to transaction from!");
                    return;
                }
                try
                {
                    Account.AssertValidAccountTree(fromAccountName);
                }
                catch (AssertionError e)
                {
                    this.fromAccountBox.requestFocus();
                    QuickDialogs.error("From account '%s' was invalid %s", fromAccountName, e.getMessage());
                    return;
                }
            }

            // capture and validate the TO accounts and values
            Set<String> mentionedAccounts = new HashSet<>();
            for (Node n : toAccountRows.getChildren())
            {
                HBox hb = (HBox)n;
                AccountAutoCompleteComboBox cb = (AccountAutoCompleteComboBox)hb.getChildren().get(0);
                CurrencyField cf = (CurrencyField)hb.getChildren().get(1);
                if (cf.getDecimal() == null || cf.getDecimal().equals(BigDecimal.ZERO))
                {
                    QuickDialogs.error("One of your currency field inputs is empty or zero!");
                    return;
                }

                Long toAccountId = cb.getSelectedAccount();
                String toAccountName = cb.getSelectedAccountName();

                if (toAccountId == null)
                {
                    if (toAccountName.isEmpty())
                    {
                        cb.requestFocus();
                        QuickDialogs.error("Please fill in destination account for all subtransactions!");
                        return;
                    }
                    try
                    {
                        Account.AssertValidAccountTree(fromAccountName);
                    }
                    catch (AssertionError e)
                    {
                        cb.requestFocus();
                        QuickDialogs.error("To account '%s' was invalid: %s", toAccountName, e.getMessage());
                        return;
                    }
                }

                if (toAccountName.equals(fromAccountName))
                {
                    QuickDialogs.error("You cannot transact from and to the same account '%s'!", toAccountName);
                    return;
                }
                if (mentionedAccounts.contains(toAccountName))
                {
                    QuickDialogs.error("You've selected account '%s' more than once, please combine those subtransactions!");
                    return;
                }
                mentionedAccounts.add(toAccountName);
            }

            // validate and capture time and date
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

                AccountTreeHelper ath = new AccountTreeHelper(ConnectionSourceProvider.get());

                // build from account if it doesnt exist
                Account fromAccount;
                if (fromAccountId == null)
                    fromAccount = ath.createAccountAndParents(fromAccountName, false);
                else
                    fromAccount = new Account(fromAccountId);

                Transaction transaction = new Transaction(dt, commentBox.getText().trim());
                transaction.setSubTransactions(transactionDao.getEmptyForeignCollection("subtransactions"));
                transactionDao.create(transaction);

                BigDecimal inversion = BigDecimal.ZERO;
                List<SubTransaction> subtransactions = new ArrayList<>();
                for (Node n : toAccountRows.getChildren())
                {
                    HBox hb = (HBox)n;
                    AccountAutoCompleteComboBox cb = (AccountAutoCompleteComboBox)hb.getChildren().get(0);
                    CurrencyField cf = (CurrencyField)hb.getChildren().get(1);

                    Long toAccountId = cb.getSelectedAccount();
                    String toAccountName = cb.getSelectedAccountName();

                    Account toAccount;
                    if (toAccountId == null)
                        toAccount = ath.createAccountAndParents(toAccountName, false);
                    else
                        toAccount = new Account(toAccountId);

                    subtransactions.add(new SubTransaction(transaction, toAccount, fromAccount, cf.getDecimal()));
                    inversion = inversion.add(cf.getDecimal().negate());
                }
                subtransactions.add(new SubTransaction(transaction, fromAccount, inversion));
                subtransactionDao.create(subtransactions);
                ath.refreshTree();
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
        AccountAutoCompleteComboBox accountSelector = new AccountAutoCompleteComboBox();
        accountSelector.setContent(nameMap);
        accountSelector.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(accountSelector, Priority.ALWAYS);
        HBox row = new HBox(10, accountSelector, currfield, pop);
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
            .map(node -> ((CurrencyField)((HBox) node).getChildren().get(1)).getDecimal())
            .forEach(value -> {
                if (value != null) total[0] = total[0].add(value);
            });

        totalLabel.setText("R " + displayFormat.format(total[0].negate()));
    }

    private void checkPopDisabled()
    {
        boolean popDisabled = this.toAccountRows.getChildren().size() <= 1;
        this.toAccountRows.getChildren().forEach(
                node -> ((HBox) node).getChildren().get(2).setDisable(popDisabled));
    }
}
