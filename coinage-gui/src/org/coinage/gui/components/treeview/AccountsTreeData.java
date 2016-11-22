package org.coinage.gui.components.treeview;

import org.coinage.core.models.Account;

/**
 * Created At: 2016-11-13
 */
public class AccountsTreeData implements Comparable<AccountsTreeData>
{
    private final long id;
    private final String name;

    public AccountsTreeData(Account acc)
    {
        this.id = acc.getId();
        this.name = acc.getName();
    }

    public AccountsTreeData(long id, String name)
    {
        this.id = id;
        this.name = name;
    }

    public long getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    @Override
    public int compareTo(AccountsTreeData other)
    {
        return this.name.toLowerCase().compareTo(other.getName().toLowerCase());
    }
}
