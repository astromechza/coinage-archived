package org.coinage.gui.tabs.reports;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import org.coinage.core.helpers.AccountTreeHelper;
import org.coinage.core.helpers.TransactionTreeHelper;
import org.coinage.core.models.Account;
import org.coinage.core.models.SubTransaction;
import org.coinage.gui.ConnectionSourceProvider;
import org.coinage.gui.components.table.TransactionTable;
import org.coinage.gui.components.table.TransactionTableRow;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created At: 2016-11-17
 */
public class AllTransactionsReport extends Tab
{
    private final long accountId;
    private TransactionTable transactionTable;
    private StackPane loadingOverlay;
    private StackPane rootStack;
    private Label reportLabel;

    public AllTransactionsReport(String name, long accountId)
    {
        this.setText(name);
        this.accountId = accountId;
        this.buildControls();
        this.setContent(this.buildLayout());
        this.applyStyles();
        this.load();
    }

    private void buildControls()
    {
        this.transactionTable = new TransactionTable();
        this.loadingOverlay = new StackPane();
        this.reportLabel = new Label("Loading...");
    }

    private Node buildLayout()
    {
        rootStack = new StackPane();

        BorderPane main = new BorderPane();
        main.setTop(this.reportLabel);
        main.setCenter(this.transactionTable);

        rootStack.getChildren().add(main);
        rootStack.getChildren().add(this.loadingOverlay);
        return rootStack;
    }

    private void applyStyles()
    {
        this.loadingOverlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7)");
    }

    private void load()
    {
        new Thread(new Task<Void>() {
            @Override
            protected Void call() throws Exception
            {
                try
                {
                    Dao<Account, Long> allAccounts = DaoManager.createDao(ConnectionSourceProvider.get(), Account.class);

                    List<AccountTreeHelper.AccountTreeNode> tree = AccountTreeHelper.buildAccountTree(allAccounts.queryForAll());
                    Map<Long, String> nameMap = AccountTreeHelper.buildNameMap(tree);

                    TransactionTreeHelper tth = new TransactionTreeHelper(ConnectionSourceProvider.get());

                    String tabName = String.format("All Transactions for %s", nameMap.get(AllTransactionsReport.this.accountId));

                    String activePref = nameMap.get(AllTransactionsReport.this.accountId);

                    long groupId = 0;
                    BigDecimal balance = new BigDecimal(0);
                    BigDecimal groupBalance = new BigDecimal(0);
                    List<SubTransaction> group = new ArrayList<>();
                    List<TransactionTableRow> rows = new ArrayList<>();
                    for (SubTransaction st : tth.getAllTransactionsForAccountAndChildren(accountId, null, null))
                    {
                        String currentName = nameMap.get(st.getAccount().getId());
                        st.getAccount().setName(currentName);

                        if (st.getTransaction().getId() != groupId)
                        {
                            if (group.size() > 0)
                            {
                                if (groupBalance.compareTo(BigDecimal.ZERO) != 0)
                                {
                                    balance = balance.add(groupBalance);
                                    rows.add(new TransactionTableRow(group, balance));
                                }
                                groupBalance = BigDecimal.ZERO;
                                group = new ArrayList<>();
                            }
                            groupId = st.getTransaction().getId();
                        }

                        if (currentName.startsWith(activePref))
                        {
                            groupBalance = groupBalance.add(st.getValue());
                        }
                        group.add(st);
                    }
                    if (group.size() > 0)
                    {
                        if (groupBalance.compareTo(BigDecimal.ZERO) != 0)
                        {
                            balance = balance.add(groupBalance);
                            rows.add(new TransactionTableRow(group, balance));
                        }
                    }

                    Platform.runLater(() -> {
                        AllTransactionsReport.this.reportLabel.setText(tabName);
                        AllTransactionsReport.this.transactionTable.getItems().addAll(rows);
                        AllTransactionsReport.this.rootStack.getChildren()
                                .remove(AllTransactionsReport.this.loadingOverlay);
                    });
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                return null;
            }
        }).start();
    }

}
