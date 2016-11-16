package test.coinage.core.helpers;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import org.coinage.core.helpers.AccountTreeHelper;
import org.coinage.core.helpers.TransactionTreeHelper;
import org.coinage.core.models.Account;
import org.coinage.core.models.AccountClosure;
import org.coinage.core.models.SubTransaction;
import org.coinage.core.models.Transaction;
import org.coinage.gui.ConnectionSourceProvider;
import org.joda.time.DateTime;
import org.junit.Test;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created At: 2016-11-15
 */
public class TestTransactionTreeHelper
{
    private final TransactionTreeHelper transTreehelper;
    private final AccountTreeHelper accTreehelper;
    private final Account accountExpenses;
    private final Account accountExpensesGroceries;
    private final Account accountExpensesElectricity;
    private final Account accountExpensesEntertainment;
    private final Account accountAssets;
    private final Account accountAssetsCheque;

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
        Dao<Transaction, Long> transactionDao = DaoManager.createDao(s, Transaction.class);
        Dao<SubTransaction, Long> subtransactionDao = DaoManager.createDao(s, SubTransaction.class);

        accountExpenses = new Account("Expenses");
        accountDao.create(accountExpenses);
        accountExpensesGroceries = new Account("Groceries", accountExpenses);
        accountExpensesElectricity = new Account("Electricity", accountExpenses);
        accountExpensesEntertainment = new Account("Entertainment", accountExpenses);
        accountDao.create(Arrays.asList(
                accountExpensesGroceries,
                accountExpensesElectricity,
                accountExpensesEntertainment
        ));

        accountAssets = new Account("Assets");
        accountDao.create(accountAssets);
        accountAssetsCheque = new Account("Cheque", accountAssets);
        accountDao.create(accountAssetsCheque);

        new AccountTreeHelper(s).refreshTree();

        Transaction t1 = new Transaction(DateTime.now(), "");
        transactionDao.create(t1);
        t1.setSubTransactions(transactionDao.getEmptyForeignCollection(Transaction.VIRTUALCOLUMN_SUBTRANSACTIONS));
        t1.getSubTransactions().add(new SubTransaction(t1, accountAssetsCheque, new BigDecimal(-539.50)));
        t1.getSubTransactions().add(new SubTransaction(t1, accountExpensesGroceries, accountAssetsCheque, new BigDecimal(39.50)));
        t1.getSubTransactions().add(new SubTransaction(t1, accountExpensesElectricity, accountAssetsCheque, new BigDecimal(500)));

        Transaction t2 = new Transaction(DateTime.now(), "");
        transactionDao.create(t2);
        t2.setSubTransactions(transactionDao.getEmptyForeignCollection(Transaction.VIRTUALCOLUMN_SUBTRANSACTIONS));
        t1.getSubTransactions().add(new SubTransaction(t1, accountAssetsCheque, new BigDecimal(-40)));
        t1.getSubTransactions().add(new SubTransaction(t1, accountExpensesEntertainment, accountAssetsCheque, new BigDecimal(40)));
    }

    @Test
    public void testFetchTransactionsTo() throws SQLException
    {
        List<SubTransaction> subtransactions = new TransactionTreeHelper(ConnectionSourceProvider.get())
                .getTransactionsToAccountAndChildren(accountExpensesElectricity, null, null);

        assertEquals(1, subtransactions.size());
        SubTransaction t = subtransactions.get(0);
        assertEquals(accountExpensesElectricity.getId(), t.getAccount().getId());
        assertEquals(accountAssetsCheque.getId(), t.getSourceAccount().getId());
        assertNotNull(t.getTransaction());
        assertNotNull(t.getTransaction().getDatetime());
        assertNotNull(t.getTransaction().getComment());
    }

    @Test
    public void testFetchTransactionsToRoot() throws SQLException
    {
        List<SubTransaction> subtransactions = new TransactionTreeHelper(ConnectionSourceProvider.get())
                .getTransactionsToAccountAndChildren(accountExpenses, null, null);

        assertEquals(3, subtransactions.size());
        SubTransaction t = subtransactions.get(0);
        assertEquals(accountExpensesGroceries.getId(), t.getAccount().getId());
        assertEquals(accountAssetsCheque.getId(), t.getSourceAccount().getId());
        t = subtransactions.get(1);
        assertEquals(accountExpensesElectricity.getId(), t.getAccount().getId());
        assertEquals(accountAssetsCheque.getId(), t.getSourceAccount().getId());
        t = subtransactions.get(2);
        assertEquals(accountExpensesEntertainment.getId(), t.getAccount().getId());
        assertEquals(accountAssetsCheque.getId(), t.getSourceAccount().getId());
        assertNotNull(t.getTransaction());
        assertNotNull(t.getTransaction().getDatetime());
        assertNotNull(t.getTransaction().getComment());
    }

    @Test
    public void testFetchTransactionsToSingle() throws SQLException
    {
        List<SubTransaction> subtransactions = new TransactionTreeHelper(ConnectionSourceProvider.get())
                .getTransactionsToAccount(accountExpenses, null, null);
        assertEquals(0, subtransactions.size());
    }

    @Test
    public void testFetchTransactionsToSingleResult() throws SQLException
    {
        List<SubTransaction> subtransactions = new TransactionTreeHelper(ConnectionSourceProvider.get())
                .getTransactionsToAccount(accountExpensesElectricity, null, null);
        assertEquals(1, subtransactions.size());
        SubTransaction t = subtransactions.get(0);
        assertEquals(accountExpensesElectricity.getId(), t.getAccount().getId());
        assertEquals(accountAssetsCheque.getId(), t.getSourceAccount().getId());
        assertNotNull(t.getTransaction());
        assertNotNull(t.getTransaction().getDatetime());
        assertNotNull(t.getTransaction().getComment());
    }

    @Test
    public void testFetchTransactionsFromAccountRoot() throws SQLException
    {
        List<SubTransaction> subtransactions = new TransactionTreeHelper(ConnectionSourceProvider.get())
                .getTransactionsFromAccountAndChildren(accountAssets, null, null);
        assertEquals(3, subtransactions.size());
        SubTransaction t = subtransactions.get(0);
        assertEquals(accountExpensesGroceries.getId(), t.getAccount().getId());
        assertEquals(accountAssetsCheque.getId(), t.getSourceAccount().getId());
        t = subtransactions.get(1);
        assertEquals(accountExpensesElectricity.getId(), t.getAccount().getId());
        assertEquals(accountAssetsCheque.getId(), t.getSourceAccount().getId());
        t = subtransactions.get(2);
        assertEquals(accountExpensesEntertainment.getId(), t.getAccount().getId());
        assertEquals(accountAssetsCheque.getId(), t.getSourceAccount().getId());
        assertNotNull(t.getTransaction());
        assertNotNull(t.getTransaction().getDatetime());
        assertNotNull(t.getTransaction().getComment());
    }

    @Test
    public void testFetchTransactionsFromSingleEmpty() throws SQLException
    {
        List<SubTransaction> subtransactions = new TransactionTreeHelper(ConnectionSourceProvider.get())
                .getTransactionsFromAccount(accountAssets, null, null);
        assertEquals(0, subtransactions.size());
    }

    @Test
    public void testFetchTransactionsFromSingle() throws SQLException
    {
        List<SubTransaction> subtransactions = new TransactionTreeHelper(ConnectionSourceProvider.get())
                .getTransactionsFromAccount(accountAssetsCheque, null, null);
        assertEquals(3, subtransactions.size());
        SubTransaction t = subtransactions.get(0);
        assertEquals(accountExpensesGroceries.getId(), t.getAccount().getId());
        assertEquals(accountAssetsCheque.getId(), t.getSourceAccount().getId());
        t = subtransactions.get(1);
        assertEquals(accountExpensesElectricity.getId(), t.getAccount().getId());
        assertEquals(accountAssetsCheque.getId(), t.getSourceAccount().getId());
        t = subtransactions.get(2);
        assertEquals(accountExpensesEntertainment.getId(), t.getAccount().getId());
        assertEquals(accountAssetsCheque.getId(), t.getSourceAccount().getId());
        assertNotNull(t.getTransaction());
        assertNotNull(t.getTransaction().getDatetime());
        assertNotNull(t.getTransaction().getComment());
    }
}
