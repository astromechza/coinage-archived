package org.coinage.gui.components.table;

import org.coinage.core.models.SubTransaction;
import org.coinage.core.models.Transaction;
import org.joda.time.DateTime;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created At: 2016-11-17
 */
public class TransactionTableRow
{
    private final long transactionId;
    private final DateTime dateTime;
    private final String comment;

    private final List<SubTransaction> subTransactions;
    private final BigDecimal balance;

    public TransactionTableRow(List<SubTransaction> subTransactions, BigDecimal balance)
    {
        Transaction transaction = null;
        for (SubTransaction st : subTransactions)
        {
            if (st.getTransaction() != null)
            {
                transaction = st.getTransaction();
                break;
            }
        }
        if (transaction != null)
        {
            this.transactionId = transaction.getId();
            this.dateTime = transaction.getDatetime();
            this.comment = transaction.getComment();
        }
        else
        {
            this.transactionId = -1;
            this.dateTime = null;
            this.comment = "no transaction specified";
        }
        this.subTransactions = subTransactions;
        this.balance = balance;
    }

    public List<SubTransaction> getSubTransactions()
    {
        return this.subTransactions;
    }

    public long getTransactionId()
    {
        return transactionId;
    }

    public DateTime getDateTime()
    {
        return dateTime;
    }

    public String getComment()
    {
        return comment;
    }

    public BigDecimal getBalance()
    {
        return balance;
    }
}
