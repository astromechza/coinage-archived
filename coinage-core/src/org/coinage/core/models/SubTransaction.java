package org.coinage.core.models;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.math.BigDecimal;

/**
 * Created At: 2016-11-06
 */
@DatabaseTable(tableName = "subtransactions")
public class SubTransaction
{
    public static final String COLUMN_ID = "id";
    @DatabaseField(columnName = COLUMN_ID, generatedId = true)
    private Long id;

    public static final String COLUMN_TRANSACTION = "transaction";
    @DatabaseField(columnName = COLUMN_TRANSACTION, foreign = true)
    private Transaction transaction;

    public static final String COLUMN_ACCOUNT_FROM = "account_from";
    @DatabaseField(columnName = COLUMN_ACCOUNT_FROM, foreign = true)
    private Account from;

    public static final String COLUMN_ACCOUNT_TO = "account_to";
    @DatabaseField(columnName = COLUMN_ACCOUNT_TO, foreign = true)
    private Account to;

    public static final String COLUMN_VALUE = "value";
    @DatabaseField(columnName = COLUMN_VALUE, dataType = DataType.BIG_DECIMAL_NUMERIC)
    private BigDecimal value;

    SubTransaction() {}

    public SubTransaction(Transaction parent, Account from, Account to, BigDecimal value)
    {
        this.transaction = parent;
        this.from = from;
        this.to = to;
        this.value = value;
    }

    public Account getFrom()
    {
        return from;
    }

    public void setFrom(Account from)
    {
        this.from = from;
    }

    public Long getId()
    {
        return id;
    }

    public Account getTo()
    {
        return to;
    }

    public void setTo(Account to)
    {
        this.to = to;
    }

    public Transaction getTransaction()
    {
        return transaction;
    }

    public void setTransaction(Transaction transaction)
    {
        this.transaction = transaction;
    }

    public BigDecimal getValue()
    {
        return value;
    }

    public void setValue(BigDecimal value)
    {
        this.value = value;
    }

    @Override
    public String toString()
    {
        return "SubTransaction{" +
                "id=" + id +
                ", transaction=" + transaction.getId() +
                ", from=" + from.getName() +
                ", to=" + to.getName() +
                ", value=" + value +
                '}';
    }
}
