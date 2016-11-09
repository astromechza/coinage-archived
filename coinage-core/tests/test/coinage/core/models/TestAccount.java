package test.coinage.core.models;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import org.coinage.core.models.Account;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

import static org.junit.Assert.*;

/**
 * Created At: 2016-11-06
 */
public class TestAccount
{
    private static final Logger LOG = LoggerFactory.getLogger(TestAccount.class);

    private Dao<Account, Long> buildDao() throws SQLException
    {
        ConnectionSource s = new JdbcConnectionSource("jdbc:sqlite::memory:");
        return DaoManager.createDao(s, Account.class);
    }

    @Test
    public void testCanConstructBasic() throws SQLException
    {
        Dao<Account, Long> dao = buildDao();
        TableUtils.createTable(dao);
        Account a = new Account("Assets");
        dao.create(a);
        assertEquals(1, a.getId().longValue());
        assertEquals("Assets", a.getName());

        Account b = dao.queryForId(a.getId());
        assertEquals("Assets", b.getName());
    }

    @Test
    public void testCanConstructWithParent() throws SQLException
    {
        Dao<Account, Long> dao = buildDao();
        TableUtils.createTable(dao);
        Account parent = new Account("Assets");
        dao.create(parent);
        Account child = new Account("Cash", parent);
        dao.create(child);
        assertEquals(1, parent.getId().longValue());
        assertEquals(2, child.getId().longValue());
        assertEquals(parent, child.getParent());

        Account child2 = dao.queryForId(child.getId());
        Account parent2 = child2.getParent();
        assertEquals(null, parent2.getName());
        dao.refresh(parent2);
        assertEquals("Assets", parent2.getName());
    }
}
