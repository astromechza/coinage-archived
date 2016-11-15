package test.coinage.core.models;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import org.coinage.core.models.Account;
import org.coinage.core.models.SubTransaction;
import org.coinage.core.models.Transaction;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Arrays;

/**
 * Created At: 2016-11-10
 */
public class TestTransaction
{
    private static final Logger LOG = LoggerFactory.getLogger(TestAccount.class);

    private final Dao<Transaction, Long> transDao;
    private final Dao<SubTransaction, Long> subtransDao;
    private final Dao<Account, Long> accountDao;

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
    }

    @Test
    public void testBasic() throws SQLException
    {
        Account a1 = new Account("account1");
        accountDao.create(a1);
        Account a2 = new Account("account2");
        accountDao.create(a2);
        Account a3 = new Account("account3");
        accountDao.create(a3);
        {
            Transaction t1 = new Transaction(DateTime.now(), "my comment");
            transDao.create(t1);

            BigDecimal value = new BigDecimal(100.142);
            SubTransaction st1 = new SubTransaction(t1, a1,value.negate());
            SubTransaction st2 = new SubTransaction(t1, a3, value);
            LOG.info(st1.toString());
            LOG.info(st2.toString());
            subtransDao.create(Arrays.asList(st1, st2));
        }

        // test some query ideas

        QueryBuilder<Transaction, Long> q = transDao.queryBuilder();

        for (Transaction t : q.query())
        {
            LOG.info(t.toString());
            for (SubTransaction st : t.getSubTransactions())
            {
                LOG.info(st.toString());
            }
        }


    }
}
