package test.coinage.core.controllers;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import org.coinage.core.generators.AccountGenerator;
import org.coinage.core.generators.TransactionGenerator;
import org.coinage.core.controllers.TransactionController;
import org.coinage.core.models.Account;
import org.coinage.core.models.SubTransaction;
import org.coinage.core.models.Transaction;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;

/**
 * Created At: 2016-11-13
 */
public class TestTransactionController
{
    private final Dao<Transaction, Long> transDao;
    private final Dao<SubTransaction, Long> subTransDao;
    private final TransactionController controller;
    private final Dao<Account, Long> accountDao;

    public TestTransactionController() throws SQLException
    {
        ConnectionSource s = new JdbcConnectionSource("jdbc:sqlite::memory:");
        this.accountDao = DaoManager.createDao(s, Account.class);
        this.transDao = DaoManager.createDao(s, Transaction.class);
        this.subTransDao = DaoManager.createDao(s, SubTransaction.class);
        this.controller = new TransactionController(s);
    }

    @Before
    public void setUp() throws Exception
    {
        TableUtils.createTableIfNotExists(this.accountDao.getConnectionSource(), Account.class);
        TableUtils.createTableIfNotExists(this.transDao.getConnectionSource(), Transaction.class);
        TableUtils.createTableIfNotExists(this.subTransDao.getConnectionSource(), SubTransaction.class);
    }

    @Test
    public void testBasic() throws SQLException
    {
        Account a1 = AccountGenerator.fakeAccount();
        Account a2 = AccountGenerator.fakeAccount();
        this.accountDao.create(a1);
        this.accountDao.create(a2);

        Transaction t1 = new Transaction(DateTime.now(), "");
        t1.setSubTransactions(transDao.getEmptyForeignCollection(Transaction.VIRTUALCOLUMN_SUBTRANSACTIONS));
        TransactionGenerator.addSubtransactions(t1, a1, a2);
        this.transDao.create(t1);
        this.subTransDao.create(t1.getSubTransactions());

        System.out.println(this.controller.buildTransactionQueryForAccount(a1).prepareStatementString());

        for (Transaction t : this.controller.buildTransactionQueryForAccount(a1).query())
        {
            System.out.println(t);
            for (SubTransaction st : t.getSubTransactions())
            {
                System.out.println(st);
            }
        }

    }
}
