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
    @DatabaseField(generatedId = true)
    private Long id;

    @DatabaseField(foreign = true)
    private Transaction transaction;

    @DatabaseField(foreign = true)
    private Account from;

    @DatabaseField(foreign = true)
    private Account to;

    @DatabaseField(dataType = DataType.BIG_DECIMAL_NUMERIC)
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
}
