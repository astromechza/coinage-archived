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

    public static final String COLUMN_ACCOUNT = "account";
    @DatabaseField(columnName = COLUMN_ACCOUNT, foreign = true)
    private Account account;

    public static final String COLUMN_VALUE = "value";
    @DatabaseField(columnName = COLUMN_VALUE, dataType = DataType.BIG_DECIMAL_NUMERIC)
    private BigDecimal value;

    public SubTransaction() {}

    public SubTransaction(Transaction parent, Account account, BigDecimal value)
    {
        this.transaction = parent;
        this.account = account;
        this.value = value;
    }

    public SubTransaction(long id)
    {
        this.id = id;
    }

    public Account getAccount()
    {
        return account;
    }

    public void setAccount(Account account)
    {
        this.account = account;
    }

    public Long getId()
    {
        return id;
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
                ", account=(" + account.getId() + ")" + account.getName() +
                ", value=" + value +
                '}';
    }
}
