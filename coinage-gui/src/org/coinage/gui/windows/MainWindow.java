package org.coinage.gui.windows;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.coinage.core.generators.AccountGenerator;
import org.coinage.core.models.Account;
import org.coinage.gui.ConnectionSourceProvider;
import org.coinage.gui.components.treeview.AccountsTreeData;
import org.coinage.gui.components.treeview.AccountsTreeTableView;
import org.coinage.gui.dialogs.QuickDialogs;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created At: 2016-11-13
 */
public class MainWindow extends BaseWindow
{
    private AccountsTreeTableView accountsTree;
    private TableView transactionTable;
    private Menu fileMI;
    private Menu accountsMI;
    private MenuItem newAccountMI;

    public MainWindow(Stage container) throws IOException
    {
        super(container);
        this.initialise();

        try
        {
            ConnectionSource s = ConnectionSourceProvider.get();
            this.buildTables(s);
            this.buildFakeData(s);
            this.accountsTree.refreshAll(s);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    private void buildTables(ConnectionSource source) throws SQLException
    {
        TableUtils.createTableIfNotExists(source, Account.class);
    }

    private void buildFakeData(ConnectionSource source) throws SQLException
    {
        Dao<Account, Long> accountsDao = DaoManager.createDao(source, Account.class);
        List<Account> accounts = new ArrayList<>();
        for (int i = 0; i < 100; i++)
        {
            accounts.add(AccountGenerator.fakeAccountInTree(accounts));
        }
        accountsDao.create(accounts);
    }

    @Override
    public void initControls()
    {
        this.accountsTree = new AccountsTreeTableView();
        this.transactionTable = new TableView();

        // menu items
        this.fileMI = new Menu("File");
        this.accountsMI = new Menu("Accounts");
        this.newAccountMI = new MenuItem("Add Account..");
    }

    @Override
    public Parent initLayout()
    {
        BorderPane outerLayout = new BorderPane();

        MenuBar menu = new MenuBar(this.fileMI, this.accountsMI);
        this.accountsMI.getItems().addAll(this.newAccountMI);

        outerLayout.setTop(menu);

        SplitPane horizantalSplit = new SplitPane();
        horizantalSplit.setDividerPosition(0, 0.3);

        horizantalSplit.getItems().add(accountsTree);

        SplitPane verticalSplit = new SplitPane();
        verticalSplit.setDividerPosition(0, 0.7);
        verticalSplit.setOrientation(Orientation.VERTICAL);

        verticalSplit.getItems().add(transactionTable);

        verticalSplit.getItems().add(new Label("Graph not implemented yet"));

        horizantalSplit.getItems().add(verticalSplit);

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
        Scene scene = new Scene(this.getRootLayout(), 800, 600);
        scene.getStylesheets().add(this.cssCommon);
        this.getStage().setTitle("Coinage");
        this.getStage().setScene(scene);
        this.getStage().setResizable(true);
        return scene;
    }
}
