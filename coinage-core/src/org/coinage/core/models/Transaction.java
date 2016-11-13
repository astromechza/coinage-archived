package org.coinage.core.models;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;
import org.joda.time.DateTime;

/**
 * Created At: 2016-11-06
 */
@DatabaseTable(tableName = "transactions")
public class Transaction
{
    public static final String COLUMN_ID = "id";
    @DatabaseField(columnName = COLUMN_ID, generatedId = true)
    private Long id;

    public static final String COLUMN_DATETIME = "datetime";
    @DatabaseField(columnName = COLUMN_DATETIME, canBeNull = false)
    private DateTime datetime;

    public static final String COLUMN_COMMENT = "comment";
    @DatabaseField(columnName = COLUMN_COMMENT, dataType = DataType.LONG_STRING)
    private String comment;

    @ForeignCollectionField(eager = true)
    private ForeignCollection<SubTransaction> subTransactions;

    Transaction() {}

    public Transaction(DateTime datetime, String comment)
    {
        this.datetime = datetime;
        this.comment = comment;
    }

    public String getComment()
    {
        return comment;
    }

    public void setComment(String comment)
    {
        this.comment = comment;
    }

    public DateTime getDatetime()
    {
        return datetime;
    }

    public void setDatetime(DateTime datetime)
    {
        this.datetime = datetime;
    }

    public Long getId()
    {
        return id;
    }

    public ForeignCollection<SubTransaction> getSubTransactions()
    {
        return subTransactions;
    }

    @Override
    public String toString()
    {
        return "Transaction{" +
                "comment='" + comment + '\'' +
                ", id=" + id +
                ", datetime=" + datetime +
                '}';
    }
}
