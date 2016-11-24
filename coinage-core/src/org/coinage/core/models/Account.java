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
    @DatabaseField(columnName = COLUMN_PARENT, foreign = true, uniqueIndexName = "unique_parent_and_name")
    private Account parent;

    public static final String COLUMN_NAME = "name";
    @DatabaseField(columnName = COLUMN_NAME, uniqueIndexName = "unique_parent_and_name")
    private String name;

    public static final Pattern NAME_PATTERN = Pattern.compile("[A-Z0-9][a-z0-9_\\-.]+");

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

    public Account(Long id)
    {
        this.id = id;
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

    public void setParent(Long id)
    {
        Account fakeParent = new Account();
        fakeParent.id = id;
        this.parent = fakeParent;
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
        if (parent != null && !parent.getId().equals(other.parent.getId())) return false;
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

    public static final Pattern VALID_NAME_REGEX = Pattern.compile("^[A-Z][A-Za-z0-9_]+$");
    public static void AssertValidAccountName(String name)
    {
        if (name.length() < 3) throw new AssertionError("is too short");
        if (name.trim().length() != name.length()) throw new AssertionError("cannot start or end with whitespace");
        if (!Character.isUpperCase(name.charAt(0))) throw new AssertionError("must start with capital letter");
        if (!VALID_NAME_REGEX.matcher(name).matches())
            throw new AssertionError("contains invalid characters");
    }

    public static final Pattern VALID_NAMECRUMB_REGEX = Pattern.compile("^[A-Za-z0-9_.]*$");
    public static void AssertValidAccountTree(String name)
    {
        if (!VALID_NAMECRUMB_REGEX.matcher(name).matches())
            throw new AssertionError("contains invalid characters");

        String[] parts = name.split("\\.", -1);

        if (parts.length == 1) AssertValidAccountName(name);

        int i = 1;
        for (String s : parts)
        {
            try
            {
                AssertValidAccountName(s);
            }
            catch (AssertionError e)
            {
                throw new AssertionError(String.format("part %d %s", i, e.getMessage()));
            }
            i++;
        }
    }

}
