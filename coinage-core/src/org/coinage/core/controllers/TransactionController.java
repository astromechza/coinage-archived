package org.coinage.core.controllers;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;
import org.coinage.core.models.Account;
import org.coinage.core.models.SubTransaction;
import org.coinage.core.models.Transaction;

import java.sql.SQLException;
import java.util.List;

/**
 * Created At: 2016-11-13
 */
public class TransactionController
{
    private final Dao<Transaction, Long> transDao;
    private final Dao<SubTransaction, Long> subTrans;

    public TransactionController(ConnectionSource source) throws SQLException
    {
        // construct required DAO's here
        this.transDao = DaoManager.createDao(source, Transaction.class);
        this.subTrans = DaoManager.createDao(source, SubTransaction.class);
    }

    public QueryBuilder<Transaction, Long>  buildTransactionQueryForAccount(Account acc) throws SQLException
    {
        QueryBuilder<SubTransaction, Long> q = subTrans.queryBuilder();
        q.where().eq(SubTransaction.COLUMN_ACCOUNT, acc.getId());
        QueryBuilder<Transaction, Long> q2 = transDao.queryBuilder();
        q2.orderBy(Transaction.COLUMN_DATETIME, true);
        q2.join(Transaction.COLUMN_ID, SubTransaction.COLUMN_TRANSACTION, q);
        return q2;
    }

}
