package test.coinage.core.models;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import org.coinage.core.helpers.AccountTreeHelper;
import org.coinage.core.models.Account;
import org.coinage.core.models.AccountClosure;
import org.coinage.core.models.SubTransaction;
import org.coinage.core.models.Transaction;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.sql.SQLException;

/**
 * Created At: 2016-11-10
 */
public class TestTransaction
{
    private final Dao<Transaction, Long> transDao;
    private final Dao<SubTransaction, Long> subtransDao;
    private final Dao<Account, Long> accountDao;
    private Account account1;
    private Account account2;
    private Account account3;
    private Transaction trans1;
    private SubTransaction subtrans11;

    public TestTransaction() throws SQLException
    {
        ConnectionSource s = new JdbcConnectionSource("jdbc:sqlite::memory:");
        this.accountDao = DaoManager.createDao(s, Account.class);
        this.transDao = DaoManager.createDao(s, Transaction.class);
        this.subtransDao = DaoManager.createDao(s, SubTransaction.class);
    }

    @Before
    public void setUp() throws Exception
    {
        TableUtils.createTableIfNotExists(this.accountDao.getConnectionSource(), Account.class);
        TableUtils.createTableIfNotExists(this.transDao.getConnectionSource(), Transaction.class);
        TableUtils.createTableIfNotExists(this.subtransDao.getConnectionSource(), SubTransaction.class);

        account1 = new Account("ac1");
        account2 = new Account("ac2");
        account3 = new Account("ac3");
        trans1 = new Transaction(DateTime.now(), "");
        subtrans11 = new SubTransaction(trans1, account1, account2, new BigDecimal(100.10));
        subtrans11 = new SubTransaction(trans1, account1, account3, new BigDecimal(99.1231));

        accountDao.create(account1);
        accountDao.create(account2);
        accountDao.create(account3);
        transDao.create(trans1);
        subtransDao.create(subtrans11);
        subtransDao.create(subtrans11);
    }

    @Test
    public void testBasic()
    {

    }
}
