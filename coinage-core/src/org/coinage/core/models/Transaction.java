package org.coinage.core.models;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import org.joda.time.DateTime;

/**
 * Created At: 2016-11-06
 */
@DatabaseTable(tableName = "transactions")
public class Transaction
{
    @DatabaseField(generatedId = true)
    private Long id;

    @DatabaseField(canBeNull = false)
    private DateTime datetime;

    @DatabaseField(dataType = DataType.LONG_STRING)
    private String comment;

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
}
