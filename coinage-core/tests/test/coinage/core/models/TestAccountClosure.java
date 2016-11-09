package test.coinage.core.models;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import org.coinage.core.models.Account;
import org.coinage.core.models.AccountClosure;
import org.junit.Test;

import java.sql.SQLException;

import static org.junit.Assert.assertEquals;

/**
 * Created At: 2016-11-06
 */
public class TestAccountClosure
{
    private Dao<AccountClosure, Long> buildDao() throws SQLException
    {
        ConnectionSource s = new JdbcConnectionSource("jdbc:sqlite::memory:");
        return DaoManager.createDao(s, AccountClosure.class);
    }

    @Test
    public void testConstruct() throws SQLException
    {
        Dao<AccountClosure, Long> dao = buildDao();
        TableUtils.createTable(dao);

        AccountClosure ac = new AccountClosure(1, 2, 3, false);
        dao.create(ac);
        assertEquals(1, ac.getId().longValue());
        assertEquals(1, ac.getAncestor());
        assertEquals(2, ac.getDescendant());
        assertEquals(3, ac.getDepth());
        assertEquals(false, ac.isAncestorRoot());

        AccountClosure bc = dao.queryForId(ac.getId());
        assertEquals(1, bc.getAncestor());
        assertEquals(2, bc.getDescendant());
        assertEquals(3, bc.getDepth());
        assertEquals(false, bc.isAncestorRoot());
    }
}
