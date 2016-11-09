package org.coinage.core.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.regex.Pattern;

/**
 * Created At: 2016-11-06
 */
@DatabaseTable(tableName = "accounts")
public class Account
{
    public static final String COLUMN_ID = "id";
    @DatabaseField(columnName = COLUMN_ID, generatedId = true)
    private Long id;

    public static final String COLUMN_PARENT = "parent";
    @DatabaseField(columnName = COLUMN_PARENT, foreign = true)
    private Account parent;

    public static final String COLUMN_NAME = "name";
    @DatabaseField(columnName = COLUMN_NAME)
    private String name;

    private String cachedFullName = null;

    public static final Pattern NAME_PATTERN = Pattern.compile("[A-Z0-9][a-z0-9_\\-\\.]+");

    public Account() {}

    public Account(String name, Account parent)
    {
        this.name = name;
        this.parent = parent;
    }

    public Account(String name)
    {
        this(name, null);
    }

    public Long getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public Account getParent()
    {
        return parent;
    }

    public void setParent(Account parent)
    {
        this.parent = parent;
    }

    @Override
    public String toString()
    {
        return "Account{" +
                "id=" + id +
                ", parent=" + parent +
                ", name='" + name + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Account other = (Account) o;
        // check id
        if (id != null ? !id.equals(other.id) : other.id != null) return false;
        // check parent id
        if (parent == null ^ other.parent == null) return false;
        if (parent != null && parent.getId() != other.parent.getId()) return false;
        return name != null ? name.equals(other.name) : other.name == null;
    }

    public void validate()
    {
        if (this.name == null) throw new AssertionError("Account name is null");
        this.name = name.trim();
        if (this.name.length() < 3) throw new AssertionError("Account name is shorter than 3 characters");
        if (!NAME_PATTERN.matcher(this.name).matches())
            throw new AssertionError("Account name does not match pattern " + NAME_PATTERN.pattern());
    }
}
