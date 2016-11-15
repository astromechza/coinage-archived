package test.coinage.core.helpers;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import org.coinage.core.generators.AccountGenerator;
import org.coinage.core.generators.TransactionGenerator;
import org.coinage.core.helpers.AccountTreeHelper;
import org.coinage.core.helpers.TransactionTreeHelper;
import org.coinage.core.models.Account;
import org.coinage.core.models.AccountClosure;
import org.coinage.core.models.SubTransaction;
import org.coinage.core.models.Transaction;
import org.coinage.gui.ConnectionSourceProvider;
import org.joda.time.DateTime;
import org.junit.Test;

import java.sql.SQLException;
import java.util.List;

/**
 * Created At: 2016-11-15
 */
public class TestTransactionTreeHelper
{
    private final TransactionTreeHelper transTreehelper;
    private final AccountTreeHelper accTreehelper;
    private final Account accountA1;
    private final Account accountA2;
    private final Account accountB1;

    public TestTransactionTreeHelper() throws SQLException
    {
        ConnectionSource s = new JdbcConnectionSource("jdbc:sqlite::memory:");
        ConnectionSourceProvider.set(s);
        this.transTreehelper = new TransactionTreeHelper(s);
        this.accTreehelper = new AccountTreeHelper(s);

        TableUtils.createTableIfNotExists(s, Account.class);
        TableUtils.createTableIfNotExists(s, AccountClosure.class);
        TableUtils.createTableIfNotExists(s, Transaction.class);
        TableUtils.createTableIfNotExists(s, SubTransaction.class);

        Dao<Account, Long> accountDao = DaoManager.createDao(s, Account.class);

        accountA1 = AccountGenerator.fakeAccount();
        accountDao.create(accountA1);
        accountA2 = AccountGenerator.fakeSubAccount(accountA1);
        accountDao.create(accountA2);
        accountB1 = AccountGenerator.fakeAccount();
        accountDao.create(accountB1);
        accTreehelper.refreshTree();

        Dao<Transaction, Long> transactionDao = DaoManager.createDao(s, Transaction.class);
        Dao<SubTransaction, Long> subtransactionDao = DaoManager.createDao(s, SubTransaction.class);

        for (int i = 0; i < 100; i++)
        {
            Transaction t = new Transaction(DateTime.now(), "");
            t.setSubTransactions(transactionDao.getEmptyForeignCollection("subtransactions"));
            if (i % 2 == 0)
                TransactionGenerator.addSubtransactions(t, accountB1, accountA2);
            else
                TransactionGenerator.addSubtransactions(t, accountA2, accountB1);
            transactionDao.create(t);
            subtransactionDao.create(t.getSubTransactions());
        }
    }

    @Test
    public void testFetchSome() throws SQLException
    {
        List<SubTransaction>
                subtransactions = new TransactionTreeHelper(ConnectionSourceProvider.get()).getTransactionsToAccountAndChildren(accountA2, null, null);
        for (SubTransaction t : subtransactions)
        {
            System.out.println(t);
            System.out.println(t.getTransaction());
            System.out.println(t.getAccount());
        }
    }
}
