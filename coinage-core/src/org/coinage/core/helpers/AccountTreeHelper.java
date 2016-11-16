package org.coinage.core.helpers;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;
import org.coinage.core.models.Account;
import org.coinage.core.models.AccountClosure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.*;

/**
 * Created At: 2016-11-06
 */
public class AccountTreeHelper
{
    private static final Logger LOG = LoggerFactory.getLogger(AccountTreeHelper.class);
    private final Dao<Account, Long> accdao;
    private final Dao<AccountClosure, Long> clsdao;

    public AccountTreeHelper(ConnectionSource source) throws SQLException
    {
        this.accdao = DaoManager.createDao(source, Account.class);
        this.clsdao = DaoManager.createDao(source, AccountClosure.class);
    }

    public static class AccountTreeNode
    {
        public Account value;
        public List<AccountTreeNode> children = new ArrayList<>();

        public AccountTreeNode(Account v)
        {
            this.value = v;
        }

        @Override
        public String toString()
        {
            return "AccountTreeNode{" +
                    "value.name=" + value.getName() +
                    ", children.length=" + children.size() +
                    '}';
        }
    }

    /**
     * Utility function for building the account closures for an active stack of Accounts.
     * @param activeStack the working stack
     * @param current the current account being processed
     * @param closures destination for generated data
     */
    public static void buildClosureList(
            Stack<Account> activeStack,
            AccountTreeNode current,
            List<AccountClosure> closures
    )
    {
        // ready for processing
        activeStack.push(current.value);

        // build links
        int depth = 0;

        // stack iterates in the wrong way so we have to go backwards
        ListIterator<Account> it = activeStack.listIterator(activeStack.size());
        while(it.hasPrevious())
        {
            AccountClosure closure = new AccountClosure(it.previous().getId(), current.value.getId(), depth, false);
            if (!it.hasPrevious()) closure.setAncestorIsRoot(true);
            closures.add(closure);
            depth++;
        }

        // now run through each child as well
        for (AccountTreeNode child : current.children)
        {
            buildClosureList(activeStack, child, closures);
        }

        // we're done!
        activeStack.pop();
    }

    /**
     * Utility function to generate an account tree map given a list of Account objects.
     * @param source a collection of accounts to assemble into a tree like structure
     * @return map of nodes to children collections
     */
    public static List<AccountTreeNode> buildAccountTree(Collection<Account> source)
    {
        // mapping the tree
        Map<Long, AccountTreeNode> idToAccountMap = new HashMap<>();
        for (Account item : source)
        {
            if (item.getId() == null) throw new IllegalArgumentException(
                    String.format("Account %s does not have an id", item.getName()));
            idToAccountMap.put(item.getId(), new AccountTreeNode(item));
        }

        // build children lists
        List<AccountTreeNode> roots = new ArrayList<>();
        for (Account item : source)
        {
            Account parent = item.getParent();
            if (parent != null)
            {
                Long parentId = parent.getId();
                if (idToAccountMap.containsKey(parentId))
                {
                    idToAccountMap.get(parent.getId()).children.add(idToAccountMap.get(item.getId()));
                    continue;
                }
            }

            roots.add(idToAccountMap.get(item.getId()));
        }

        return roots;
    }

    /**
     * Clear and rebuild the account closure table.
     * @throws SQLException
     */
    public void refreshTree() throws SQLException
    {
        // fetch all accounts
        List<AccountTreeNode> accountTree = buildAccountTree(accdao.queryForAll());

        // now build all the things
        List<AccountClosure> closures = new ArrayList<>();
        Stack<Account> activeStack = new Stack<>();
        for (AccountTreeNode n : accountTree)
        {
            // for each root item, do the thing!
            buildClosureList(activeStack, n, closures);
        }

        int deleted = clsdao.deleteBuilder().delete();
        LOG.debug("Deleted {} existing Account closures", deleted);

        LOG.debug("Writing {} Account closures", closures.size());
        clsdao.create(closures);
    }

    public List<Account> selfAndAncestors(Account account) throws SQLException
    {
        QueryBuilder<AccountClosure, Long> q = clsdao.queryBuilder();
        q.selectColumns(AccountClosure.COLUMN_ANCESTOR)
                .orderBy(AccountClosure.COLUMN_DEPTH, true)
                .where().eq(AccountClosure.COLUMN_DESCENDANT, account.getId());
        return accdao.queryBuilder().where().in(Account.COLUMN_ID, q).query();
    }

    public List<Account> ancestors(Account account) throws SQLException
    {
        QueryBuilder<AccountClosure, Long> q = clsdao.queryBuilder();
        q.selectColumns(AccountClosure.COLUMN_ANCESTOR)
                .orderBy(AccountClosure.COLUMN_DEPTH, true)
                .where()
                .eq(AccountClosure.COLUMN_DESCENDANT, account.getId())
                .and().ne(AccountClosure.COLUMN_ANCESTOR, account.getId());
        return accdao.queryBuilder().where().in(Account.COLUMN_ID, q).query();
    }

    public AccountTreeNode selfAndDescendants(Account account) throws SQLException
    {
        QueryBuilder<AccountClosure, Long> q = clsdao.queryBuilder();
        q.selectColumns(AccountClosure.COLUMN_DESCENDANT)
                .orderBy(AccountClosure.COLUMN_DEPTH, true)
                .where().eq(AccountClosure.COLUMN_ANCESTOR, account.getId());
        List<AccountTreeNode> result = buildAccountTree(accdao.queryBuilder().where().in(Account.COLUMN_ID, q).query());
        if (result.size() != 1) throw new SQLException("Expected single result");
        return result.get(0);
    }

    public List<AccountTreeNode> descendants(Account account) throws SQLException
    {
        return this.selfAndDescendants(account).children;
    }

    private Where<Account, Long> childrenQuery(Account account) throws SQLException
    {
        return accdao.queryBuilder().where().eq(Account.COLUMN_PARENT, account);
    }

    public boolean hasChildren(Account account) throws SQLException
    {
        return this.childrenQuery(account).countOf() > 0;
    }

    public List<Account> children(Account account) throws SQLException
    {
        return this.childrenQuery(account).query();
    }

    public List<Account> roots() throws SQLException
    {
        QueryBuilder<Account, Long> q = accdao.queryBuilder();
        q.orderBy(Account.COLUMN_ID, true);
        return q.where().isNull(Account.COLUMN_PARENT).query();
    }

    public Account root(Account account) throws SQLException
    {
        if (account.getParent() == null) return account;
        QueryBuilder<AccountClosure, Long> q = clsdao.queryBuilder();
        q.selectColumns(AccountClosure.COLUMN_ANCESTOR);
        q.where().eq(AccountClosure.COLUMN_ANCESTOR_IS_ROOT, true)
                 .and().eq(AccountClosure.COLUMN_DESCENDANT, account.getId());
        return accdao.queryBuilder().join(Account.COLUMN_ID, AccountClosure.COLUMN_ANCESTOR, q).queryForFirst();
    }

    private Where<Account, Long> siblingsQuery(Account account) throws SQLException
    {
        QueryBuilder<Account, Long> q = accdao.queryBuilder();
        q.orderBy(Account.COLUMN_ID, true);
        if (account.getParent() == null)
            return q.where().isNull(Account.COLUMN_PARENT);
        return q.where().eq(Account.COLUMN_PARENT, account.getParent());
    }

    public List<Account> selfAndSiblings(Account account) throws SQLException
    {
        return this.siblingsQuery(account).query();
    }

    public List<Account> siblings(Account account) throws SQLException
    {
        return this.siblingsQuery(account).and().ne(Account.COLUMN_ID, account.getId()).query();
    }
}
