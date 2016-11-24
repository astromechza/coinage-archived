package org.coinage.gui.windows;

import com.j256.ormlite.dao.DaoManager;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.coinage.core.models.Account;
import org.coinage.gui.ConnectionSourceProvider;
import org.coinage.gui.components.treeview.AccountsTreeData;
import org.coinage.gui.components.treeview.AccountsTreeTableView;
import org.coinage.gui.dialogs.QuickDialogs;
import org.coinage.gui.tabs.reports.AllTransactionsReport;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Created At: 2016-11-13
 */
public class MainWindow extends BaseWindow
{
    private AccountsTreeTableView accountsTree;
    private TabPane tabPane;
    private Menu fileMI;
    private Menu accountsMI;
    private MenuItem newAccountMI;
    private MenuItem newTransactionMI;

    public MainWindow(Stage container) throws IOException
    {
        super(container);
        this.initialise();

        try
        {
            this.accountsTree.refreshAll(ConnectionSourceProvider.get());
        }
        catch (SQLException e)
        {
            QuickDialogs.exception(e, "Failure when refreshing account tree");
        }
    }

    @Override
    public void initControls()
    {
        this.accountsTree = new AccountsTreeTableView();
        this.tabPane = new TabPane();

        // menu items
        this.fileMI = new Menu("File");
        this.accountsMI = new Menu("Accounts");
        this.newAccountMI = new MenuItem("Add Account..");
        this.newTransactionMI = new MenuItem("Add Transaction..");
    }

    @Override
    public Parent initLayout()
    {
        BorderPane outerLayout = new BorderPane();

        MenuBar menu = new MenuBar(this.fileMI, this.accountsMI);
        this.accountsMI.getItems().addAll(this.newAccountMI, this.newTransactionMI);

        outerLayout.setTop(menu);

        SplitPane horizantalSplit = new SplitPane();
        horizantalSplit.setDividerPosition(0, 0.3);

        horizantalSplit.getItems().add(accountsTree);
        horizantalSplit.getItems().add(tabPane);

        outerLayout.setCenter(horizantalSplit);
        return outerLayout;
    }

    @Override
    public void bindEvents()
    {
        this.newAccountMI.setOnAction(event -> {
            try
            {
                TreeItem<AccountsTreeData> selectedAccount = this.accountsTree.getSelectionModel().getSelectedItem();
                Account fakeAccount = null;
                if (selectedAccount != null)
                {
                    fakeAccount = new Account(selectedAccount.getValue().getId());
                }
                new NewAccountWindow(fakeAccount).getStage().showAndWait();
                this.accountsTree.refreshAll(ConnectionSourceProvider.get());
            }
            catch (IOException | SQLException e)
            {
                QuickDialogs.exception(e);
            }
        });
        this.accountsTree.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2)
            {
                Node n = event.getPickResult().getIntersectedNode();
                n = n.getParent();
                if (!(n instanceof TreeTableRow)) n = n.getParent();
                if (n instanceof TreeTableRow)
                {
                    TreeTableRow ttr = (TreeTableRow) n;
                    AccountsTreeData data = (AccountsTreeData) ttr.getTreeItem().getValue();
                    this.tabPane.getTabs().add(new AllTransactionsReport(data.getName(), data.getId()));
                }
            }
        });

        this.newTransactionMI.setOnAction(event -> {
            try
            {
                new NewTransactionWindow().getStage().showAndWait();
                this.accountsTree.refreshAll(ConnectionSourceProvider.get());
            }
            catch (IOException e)
            {
                QuickDialogs.exception(e);
            }
            catch (SQLException e)
            {
                e.printStackTrace();
            }
        });

        try
        {
            DaoManager.createDao(ConnectionSourceProvider.get(), Account.class).registerObserver(
                    () -> System.out.println("A Change Occured"));
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void applyStyling()
    {

    }

    @Override
    public Scene initScene()
    {
        Scene scene = new Scene(this.getRootLayout(), 800, 600);
        scene.getStylesheets().add(this.cssCommon);
        this.getStage().setTitle("Coinage");
        this.getStage().setScene(scene);
        this.getStage().setResizable(true);
        return scene;
    }
}
