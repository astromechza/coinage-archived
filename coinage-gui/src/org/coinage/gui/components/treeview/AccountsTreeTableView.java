package org.coinage.gui.components.treeview;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.*;
import org.coinage.core.helpers.AccountTreeHelper;
import org.coinage.core.models.Account;
import org.coinage.gui.ConnectionSourceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;

/**
 * Created At: 2016-11-13
 */
public class AccountsTreeTableView extends TreeTableView<AccountsTreeData>
{
    private final TreeTableColumn<AccountsTreeData, String> accountNameColumn;

    public AccountsTreeTableView()
    {
        // build fake root
        this.setRoot(new TreeItem<>(null));
        this.setShowRoot(false);
        this.accountNameColumn = new TreeTableColumn<>("Account");
        this.accountNameColumn.setCellValueFactory(
                param -> new SimpleObjectProperty<>(param.getValue().getValue().getName()));
        this.getColumns().add(this.accountNameColumn);
        this.setColumnResizePolicy(TreeTableView.CONSTRAINED_RESIZE_POLICY);
        this.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    public void refreshAll(ConnectionSource source) throws SQLException
    {
        AccountTreeHelper ath = new AccountTreeHelper(source);
        List<AccountTreeHelper.AccountTreeNode> roots = ath.tree();

        this.getRoot().getChildren().clear();
        for (AccountTreeHelper.AccountTreeNode root : roots)
        {
            TreeItem<AccountsTreeData> node = new TreeItem<>(new AccountsTreeData(root.value));
            refillSubTree(node, root);
            this.getRoot().getChildren().add(node);
        }
        this.getRoot().getChildren().sort(Comparator.comparing(o -> o.getValue().getName()));
    }

    private void refillSubTree(TreeItem<AccountsTreeData> parent, AccountTreeHelper.AccountTreeNode parentNode)
    {
        for (AccountTreeHelper.AccountTreeNode child : parentNode.children)
        {
            TreeItem<AccountsTreeData> childTI = new TreeItem<>(new AccountsTreeData(child.value));
            refillSubTree(childTI, child);
            parent.getChildren().add(childTI);
        }

        parent.getChildren().sort(Comparator.comparing(o -> o.getValue().getName()));
    }

}
