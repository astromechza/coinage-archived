package org.coinage.core.helpers;

import com.j256.ormlite.dao.*;
import com.j256.ormlite.field.types.DateTimeType;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;
import org.coinage.core.LogTimer;
import org.coinage.core.models.Account;
import org.coinage.core.models.AccountClosure;
import org.coinage.core.models.SubTransaction;
import org.coinage.core.models.Transaction;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

/**
 * Created At: 2016-11-15
 */
public class TransactionTreeHelper
{
    private static final Logger LOG = LoggerFactory.getLogger(AccountTreeHelper.class);

    private final Dao<SubTransaction, Long> stdao;
    private final Dao<Transaction, Long> tdao;
    private final Dao<AccountClosure, Long> acldao;

    public TransactionTreeHelper(ConnectionSource source) throws SQLException
    {
        this.acldao = DaoManager.createDao(source, AccountClosure.class);
        this.tdao = DaoManager.createDao(source, Transaction.class);
        this.stdao = DaoManager.createDao(source, SubTransaction.class);
    }

    public List<SubTransaction> assembledDetailsSubTransactions(QueryBuilder<SubTransaction, Long> query)
            throws SQLException
    {
        query.orderByRaw("`transactions`.`datetime` ASC, `subtransactions`.`id` ASC");
        query.selectRaw(
                "`subtransactions`.`id`", "`subtransactions`.`transaction`", "`subtransactions`.`account`", "`subtransactions`.`source_account`", "`subtransactions`.`value`",
                "`transactions`.`datetime`", "`transactions`.`comment`");

        GenericRawResults<SubTransaction> custom = this.stdao.queryRaw(
                query.prepareStatementString(),
                databaseResults -> {
                    SubTransaction st = new SubTransaction(databaseResults.getLong(0));
                    st.setTransaction(new Transaction(databaseResults.getLong(1)));
                    st.setAccount(new Account(databaseResults.getLong(2)));
                    st.setSourceAccount(new Account(databaseResults.getLong(3)));
                    st.setValue(new BigDecimal(databaseResults.getString(4)));
                    st.getTransaction().setDatetime((DateTime) DateTimeType.getSingleton().sqlArgToJava(null, databaseResults.getLong(5), 0));
                    st.getTransaction().setComment(databaseResults.getString(6));
                    return st;
                });
        return custom.getResults();
    }

    public QueryBuilder<Transaction, Long> transactionQuery(DateTime after, DateTime before) throws SQLException
    {
        QueryBuilder<Transaction, Long> tq = this.tdao.queryBuilder();
        tq.orderBy(Transaction.COLUMN_DATETIME, true);
        if (after != null)
            tq.where().ge(Transaction.COLUMN_DATETIME, after);
        if (before != null)
            tq.where().le(Transaction.COLUMN_DATETIME, before);
        return tq;
    }

    public List<SubTransaction> getTransactionsToAccountAndChildren(long accountId, DateTime after, DateTime before)
            throws SQLException
    {
        try(LogTimer ignored = new LogTimer(LOG, "get transactions for account tree " + accountId))
        {
            QueryBuilder<Transaction, Long> tq = this.transactionQuery(after, before);

            QueryBuilder<AccountClosure, Long> aq = this.acldao.queryBuilder();
            aq.selectColumns(AccountClosure.COLUMN_DESCENDANT);
            aq.where().eq(AccountClosure.COLUMN_ANCESTOR, accountId);

            QueryBuilder<SubTransaction, Long> stq = this.stdao.queryBuilder();
            stq.where().in(SubTransaction.COLUMN_ACCOUNT, aq);

            QueryBuilder<SubTransaction, Long> finalq =
                    stq.join(SubTransaction.COLUMN_TRANSACTION, Transaction.COLUMN_ID, tq);
            return assembledDetailsSubTransactions(finalq);
        }
    }

    public List<SubTransaction> getTransactionsToAccountAndChildren(Account acc, DateTime after, DateTime before)
            throws SQLException
    {
        return this.getTransactionsToAccountAndChildren(acc.getId(), after, before);
    }

    public List<SubTransaction> getTransactionsToAccount(long accountId, DateTime after, DateTime before)
            throws SQLException
    {
        try(LogTimer ignored = new LogTimer(LOG, "get transactions for account " + accountId))
        {
            QueryBuilder<Transaction, Long> tq = this.transactionQuery(after, before);

            QueryBuilder<SubTransaction, Long> stq = this.stdao.queryBuilder();
            stq.where().eq(SubTransaction.COLUMN_ACCOUNT, accountId);

            QueryBuilder<SubTransaction, Long> finalq =
                    stq.join(SubTransaction.COLUMN_TRANSACTION, Transaction.COLUMN_ID, tq);
            return assembledDetailsSubTransactions(finalq);
        }
    }

    public List<SubTransaction> getTransactionsToAccount(Account acc, DateTime after, DateTime before)
            throws SQLException
    {
        return this.getTransactionsToAccount(acc.getId(), after, before);
    }

    public List<SubTransaction> getTransactionsFromAccountAndChildren(long accountId, DateTime after, DateTime before)
            throws SQLException
    {
        try(LogTimer ignored = new LogTimer(LOG, "get transactions for account tree " + accountId))
        {
            QueryBuilder<Transaction, Long> tq = this.transactionQuery(after, before);

            QueryBuilder<AccountClosure, Long> aq = this.acldao.queryBuilder();
            aq.selectColumns(AccountClosure.COLUMN_DESCENDANT);
            aq.where().eq(AccountClosure.COLUMN_ANCESTOR, accountId);

            QueryBuilder<SubTransaction, Long> stq = this.stdao.queryBuilder();
            stq.where().in(SubTransaction.COLUMN_SOURCE_ACCOUNT, aq);

            QueryBuilder<SubTransaction, Long> finalq = stq.join(SubTransaction.COLUMN_TRANSACTION, Transaction.COLUMN_ID, tq);
            return assembledDetailsSubTransactions(finalq);
        }
    }

    public List<SubTransaction> getTransactionsFromAccountAndChildren(Account account, DateTime after, DateTime before)
            throws SQLException
    {
        return getTransactionsFromAccountAndChildren(account.getId(), after, before);
    }

    public List<SubTransaction> getTransactionsFromAccount(long accountId, DateTime after, DateTime before)
            throws SQLException
    {
        try(LogTimer ignored = new LogTimer(LOG, "get transactions for account " + accountId))
        {
            QueryBuilder<Transaction, Long> tq = this.transactionQuery(after, before);

            QueryBuilder<SubTransaction, Long> stq = this.stdao.queryBuilder();
            stq.where().eq(SubTransaction.COLUMN_SOURCE_ACCOUNT, accountId);

            QueryBuilder<SubTransaction, Long> finalq = stq.join(SubTransaction.COLUMN_TRANSACTION, Transaction.COLUMN_ID, tq);
            return assembledDetailsSubTransactions(finalq);
        }
    }

    public List<SubTransaction> getTransactionsFromAccount(Account account, DateTime after, DateTime before)
            throws SQLException
    {
        return getTransactionsFromAccount(account.getId(), after, before);
    }

    public List<SubTransaction> getAllTransactionsForAccount(long accountId, DateTime after, DateTime before)
            throws SQLException
    {
        try(LogTimer ignored = new LogTimer(LOG, "get all transactions for account " + accountId))
        {
            QueryBuilder<Transaction, Long> tq = this.transactionQuery(after, before);

            QueryBuilder<SubTransaction, Long> stq = this.stdao.queryBuilder();
            stq.where()
                    .eq(SubTransaction.COLUMN_SOURCE_ACCOUNT, accountId)
                    .or().eq(SubTransaction.COLUMN_ACCOUNT, accountId);

            QueryBuilder<SubTransaction, Long> finalq = stq.join(SubTransaction.COLUMN_TRANSACTION, Transaction.COLUMN_ID, tq);
            return assembledDetailsSubTransactions(finalq);
        }
    }

    public List<SubTransaction> getAllTransactionsForAccount(Account account, DateTime after, DateTime before)
            throws SQLException
    {
        return getAllTransactionsForAccount(account.getId(), after, before);
    }

    public List<SubTransaction> getAllTransactionsForAccountAndChildren(long accountId, DateTime after, DateTime before)
            throws SQLException
    {
        try(LogTimer ignored = new LogTimer(LOG, "get all transactions for account " + accountId))
        {
            QueryBuilder<Transaction, Long> tq = this.transactionQuery(after, before);

            QueryBuilder<AccountClosure, Long> aq = this.acldao.queryBuilder();
            aq.selectColumns(AccountClosure.COLUMN_DESCENDANT);
            aq.where().eq(AccountClosure.COLUMN_ANCESTOR, accountId);

            QueryBuilder<SubTransaction, Long> stq = this.stdao.queryBuilder();
            stq.where()
                    .in(SubTransaction.COLUMN_SOURCE_ACCOUNT, aq)
                    .or().in(SubTransaction.COLUMN_ACCOUNT, aq);

            QueryBuilder<SubTransaction, Long> finalq = stq.join(SubTransaction.COLUMN_TRANSACTION, Transaction.COLUMN_ID, tq);
            return assembledDetailsSubTransactions(finalq);
        }
    }

    public List<SubTransaction> getAllTransactionsForAccountAndChildren(Account account, DateTime after, DateTime before)
            throws SQLException
    {
        return getAllTransactionsForAccountAndChildren(account.getId(), after, before);
    }
}
