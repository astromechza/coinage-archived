package org.coinage.gui.components;

/**
 * Created At: 2016-11-23
 */
public class AccountAutoCompleteItem
{
    private final long accountId;
    private final String fullName;

    public AccountAutoCompleteItem(long accountId, String fullName)
    {
        this.accountId = accountId;
        this.fullName = fullName;
    }

    public long getAccountId()
    {
        return accountId;
    }

    public String getFullName()
    {
        return fullName;
    }

    @Override
    public String toString()
    {
        return this.fullName;
    }

    public boolean containsFilter(String s)
    {
        return this.fullName.toLowerCase().contains(s.toLowerCase());
    }
}
