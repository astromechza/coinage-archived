package test.coinage.core.helpers;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import org.coinage.core.generators.AccountGenerator;
import org.coinage.core.helpers.AccountTreeHelper;
import org.coinage.core.models.Account;
import org.coinage.core.models.AccountClosure;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.util.*;

import static org.junit.Assert.assertEquals;

/**
 * Created At: 2016-11-07
 */
public class TestAccountTreeHelper
{
    private final AccountTreeHelper treehelper;
    private final Dao<Account, Long> accountDao;
    private final Dao<AccountClosure, Long> accountClosuresDao;
    private Account ac1;
    private Account ac11;
    private Account ac12;
    private Account ac121;
    private Account ac2;
    private Account ac3;
    private Account ac31;
    private Account ac311;
    private Account ac3111;
    private Account ac3112;
    private Account ac3113;

    public TestAccountTreeHelper() throws SQLException
    {
        ConnectionSource s = new JdbcConnectionSource("jdbc:sqlite::memory:");
        this.accountDao = DaoManager.createDao(s, Account.class);
        this.accountClosuresDao = DaoManager.createDao(s, AccountClosure.class);
        this.treehelper = new AccountTreeHelper(s);
    }

    @Before
    public void setUp() throws Exception
    {
        TableUtils.createTableIfNotExists(this.accountDao.getConnectionSource(), Account.class);
        TableUtils.createTableIfNotExists(this.accountClosuresDao.getConnectionSource(), AccountClosure.class);

        this.ac1 = AccountGenerator.fakeAccount();
        this.ac11 = AccountGenerator.fakeSubAccount(this.ac1);
        this.ac12 = AccountGenerator.fakeSubAccount(this.ac1);
        this.ac121 = AccountGenerator.fakeSubAccount(this.ac12);
        this.ac2 = AccountGenerator.fakeAccount();
        this.ac3 = AccountGenerator.fakeAccount();
        this.ac31 = AccountGenerator.fakeSubAccount(this.ac3);
        this.ac311 = AccountGenerator.fakeSubAccount(this.ac31);
        this.ac3111 = AccountGenerator.fakeSubAccount(this.ac311);
        this.ac3112 = AccountGenerator.fakeSubAccount(this.ac311);
        this.ac3113 = AccountGenerator.fakeSubAccount(this.ac311);

        accountDao.create(this.ac1);
        accountDao.create(this.ac11);
        accountDao.create(this.ac12);
        accountDao.create(this.ac121);
        accountDao.create(this.ac2);
        accountDao.create(this.ac3);
        accountDao.create(this.ac31);
        accountDao.create(this.ac311);
        accountDao.create(this.ac3111);
        accountDao.create(this.ac3112);
        accountDao.create(this.ac3113);

        treehelper.refreshTree();
    }

    @Test
    public void testRefreshBranch() throws SQLException
    {
        List<AccountClosure> closures = new ArrayList<>();
        Stack<Account> activeStack = new Stack<>();

        List<AccountTreeHelper.AccountTreeNode> t = AccountTreeHelper.buildAccountTree(Arrays.asList(
                ac1, ac11, ac12, ac121, ac2, ac3, ac31, ac311, ac3111, ac3112, ac3113));
        AccountTreeHelper.buildClosureList(activeStack, t.get(0), closures);
        AccountTreeHelper.buildClosureList(activeStack, t.get(1), closures);
        AccountTreeHelper.buildClosureList(activeStack, t.get(2), closures);

        assertEquals(27, closures.size());
        assertEquals(11, closures.stream().filter(AccountClosure::isAncestorRoot).count());
    }

    @Test
    public void testRefreshTree() throws SQLException
    {
        this.treehelper.refreshTree();
        List<AccountClosure> closures = this.accountClosuresDao.queryForAll();

        assertEquals(27, closures.size());
        assertEquals(11, closures.stream().filter(AccountClosure::isAncestorRoot).count());
    }

    @Test
    public void testSelfAndAncestors() throws SQLException
    {
        assertEquals(Collections.singletonList(ac2), treehelper.selfAndAncestors(ac2));
        assertEquals(Arrays.asList(ac3, ac31, ac311), treehelper.selfAndAncestors(ac311));
        assertEquals(Arrays.asList(ac3, ac31, ac311, ac3111), treehelper.selfAndAncestors(ac3111));
    }

    @Test
    public void testAncestors() throws SQLException
    {
        assertEquals(Collections.emptyList(), treehelper.ancestors(ac2));
        assertEquals(Arrays.asList(ac3, ac31), treehelper.ancestors(ac311));
        assertEquals(Arrays.asList(ac3, ac31, ac311), treehelper.ancestors(ac3111));
    }

    @Test
    public void testSelfAndDescendants() throws SQLException
    {
        AccountTreeHelper.AccountTreeNode node = treehelper.selfAndDescendants(ac1);
        assertEquals(node.value, ac1);
        assertEquals(node.children.size(), 2);
        assertEquals(node.children.get(0).value, ac11);
        assertEquals(node.children.get(1).value, ac12);
        assertEquals(node.children.get(0).children.size(), 0);
        assertEquals(node.children.get(1).children.size(), 1);
        assertEquals(node.children.get(1).children.get(0).value, ac121);

        node = treehelper.selfAndDescendants(ac121);
        assertEquals(node.value, ac121);
        assertEquals(node.children.size(), 0);
    }

    @Test
    public void testDescendants() throws SQLException
    {
        List<AccountTreeHelper.AccountTreeNode> nodes = treehelper.descendants(ac1);
        assertEquals(nodes.size(), 2);
        assertEquals(nodes.get(0).value, ac11);
        assertEquals(nodes.get(1).value, ac12);
        assertEquals(nodes.get(0).children.size(), 0);
        assertEquals(nodes.get(1).children.size(), 1);
        assertEquals(nodes.get(1).children.get(0).value, ac121);

        nodes = treehelper.descendants(ac121);
        assertEquals(nodes.size(), 0);
    }

    @Test
    public void testSelfAndSiblings() throws SQLException
    {
        assertEquals(Arrays.asList(ac1, ac2, ac3), treehelper.selfAndSiblings(ac1));
        assertEquals(Arrays.asList(ac3111, ac3112, ac3113), treehelper.selfAndSiblings(ac3112));
        assertEquals(Collections.singletonList(ac121), treehelper.selfAndSiblings(ac121));
    }

    @Test
    public void testSiblings() throws SQLException
    {
        assertEquals(Arrays.asList(ac2, ac3), treehelper.siblings(ac1));
        assertEquals(Arrays.asList(ac3111, ac3113), treehelper.siblings(ac3112));
        assertEquals(Collections.emptyList(), treehelper.siblings(ac121));
    }


    @Test
    public void testRoots() throws SQLException
    {
        assertEquals(Arrays.asList(ac1, ac2, ac3), treehelper.roots());

        assertEquals(ac1, treehelper.root(ac1));
        assertEquals(ac2, treehelper.root(ac2));
        assertEquals(ac1, treehelper.root(ac11));
        assertEquals(ac1, treehelper.root(ac121));
        assertEquals(ac3, treehelper.root(ac3112));
    }
}
