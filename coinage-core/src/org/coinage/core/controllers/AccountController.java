package org.coinage.core.controllers;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;
import org.coinage.core.helpers.AccountTreeHelper;
import org.coinage.core.models.Account;
import org.coinage.core.models.AccountClosure;

import java.sql.SQLException;
import java.util.List;

/**
 * Created At: 2016-11-09
 */
public class AccountController
{
    private final Dao<Account, Long> accountDao;
    private final Dao<AccountClosure, Long> accountClosureDao;
    private final AccountTreeHelper accountTreeHelper;

    public AccountController(ConnectionSource source) throws SQLException
    {
        // construct required DAO's here
        this.accountDao = DaoManager.createDao(source, Account.class);
        this.accountClosureDao = DaoManager.createDao(source, AccountClosure.class);
        this.accountTreeHelper = new AccountTreeHelper(source);
    }

    public void create(Account account) throws Exception
    {
        if (account.getId() != null)
            throw new Exception("Account is already bound (it has an id)");

        // validate fields
        account.validate();

        // check that parent is saved by looking up parent id
        if (account.getParent() != null)
        {
            Account parentAccount = this.accountDao.queryForId(account.getParent().getId());
            if (parentAccount == null)
                throw new Exception(String.format(
                        "Declared parent account %s does not exist in the database", account.getParent().getId()));
        }

        // create
        this.accountDao.create(account);
        this.accountTreeHelper.refreshTree();
    }

    public void save(Account account) throws Exception
    {
        if (account.getId() == null)
            throw new Exception("Account is not bound (it has no id)");

        // validate fields
        account.validate();

        // check that parent is saved by looking up parent id
        if (account.getParent() != null)
        {
            Account parentAccount = this.accountDao.queryForId(account.getParent().getId());
            if (parentAccount == null)
                throw new Exception(String.format(
                        "Declared parent account %s does not exist in the database", account.getParent().getId()));
        }

        // create
        this.accountDao.update(account);
        this.accountTreeHelper.refreshTree();
    }

    public boolean delete(Account account) throws Exception
    {
        if (account.getId() == null) return false;

        // refresh db information
        this.accountDao.refresh(account);

        // cannot delete if it has children
        if (this.accountTreeHelper.hasChildren(account))
            throw new Exception("Cannot delete account that has children, you must use recursive delete for this");

        // update
        this.accountDao.delete(account);
        this.accountTreeHelper.refreshTree();
        return true;
    }
}
