package org.coinage.core.fixtures;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;
import org.coinage.core.helpers.AccountTreeHelper;
import org.coinage.core.models.Account;
import org.coinage.core.models.SubTransaction;
import org.coinage.core.models.Transaction;
import org.joda.time.DateTime;

import java.math.BigDecimal;
import java.sql.SQLException;

/**
 * Created At: 2016-11-22
 */
public class FakeFixture
{
    public static void install(ConnectionSource source)
    {
        try
        {
            Dao<Account, Long> accountDao = DaoManager.createDao(source, Account.class);

            Account accAssets = new Account("Assets");
            accountDao.create(accAssets);
            Account accBank = new Account("Bank", accAssets);
            accountDao.create(accBank);

            Account accExpenses = new Account("Expenses");
            accountDao.create(accExpenses);
            Account accCash = new Account("Cash", accExpenses);
            accountDao.create(accCash);
            Account accGrocery = new Account("Groceries", accExpenses);
            accountDao.create(accGrocery);

            Account accIncome = new Account("Income");
            accountDao.create(accIncome);
            Account accSalary = new Account("Salary", accIncome);
            accountDao.create(accSalary);

            new AccountTreeHelper(source).refreshTree();

            Dao<Transaction, Long> transactionDao = DaoManager.createDao(source, Transaction.class);
            Dao<SubTransaction, Long> subTransactionDao = DaoManager.createDao(source, SubTransaction.class);

            Transaction t1 = new Transaction(DateTime.now(), "Salary 1");
            transactionDao.create(t1);

            SubTransaction t11 = new SubTransaction(t1, accBank, accSalary, new BigDecimal(1000));
            SubTransaction t12 = new SubTransaction(t1, accSalary, null, new BigDecimal(1000).negate());
            subTransactionDao.create(t11);
            subTransactionDao.create(t12);

            Transaction t2 = new Transaction(DateTime.now(), "Transaction at grocery store");
            transactionDao.create(t2);

            SubTransaction t21 = new SubTransaction(t2, accCash, accBank, new BigDecimal(300));
            SubTransaction t22 = new SubTransaction(t2, accGrocery, accBank, new BigDecimal(200));
            SubTransaction t23 = new SubTransaction(t2, accBank, null, new BigDecimal(500).negate());
            subTransactionDao.create(t21);
            subTransactionDao.create(t22);
            subTransactionDao.create(t23);

            Transaction t3 = new Transaction(DateTime.now(), "Transaction at else where");
            transactionDao.create(t3);

            SubTransaction t31 = new SubTransaction(t3, accExpenses, accCash, new BigDecimal(50.44));
            SubTransaction t32 = new SubTransaction(t3, accCash, null, new BigDecimal(50.44).negate());
            subTransactionDao.create(t31);
            subTransactionDao.create(t32);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }
}
