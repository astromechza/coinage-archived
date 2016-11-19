package org.coinage.gui.components.table;

import org.coinage.core.models.SubTransaction;
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
        this.subTransactions = subTransactions;
        this.transactionId = subTransactions.get(0).getId();
        this.dateTime = subTransactions.get(0).getTransaction().getDatetime();
        this.comment = subTransactions.get(0).getTransaction().getComment();
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
