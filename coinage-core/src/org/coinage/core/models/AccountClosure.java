package org.coinage.core.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Closure Tree DataStructure
 *
 * ancestore_id     (first ref)
 * descendant_id    (second ref)
 * depth            (number of edges between first and second)
 *
 * - there is always a self-reference
 * - then a link between each ancestor and descendant
 *
 * Created At: 2016-11-06
 */
@DatabaseTable(tableName = "accountclosures")
public class AccountClosure
{
    public static final String COLUMN_ID = "id";
    @DatabaseField(columnName = COLUMN_ID, generatedId = true)
    private Long id;

    public static final String COLUMN_ANCESTOR = "ancestor";
    @DatabaseField(columnName = COLUMN_ANCESTOR, uniqueIndexName = "unique_ancestor_and_descendant")
    private long ancestor;

    public static final String COLUMN_DESCENDANT = "descendant";
    @DatabaseField(columnName = COLUMN_DESCENDANT, uniqueIndexName = "unique_ancestor_and_descendant")
    private long descendant;

    public static final String COLUMN_DEPTH = "depth";
    @DatabaseField(columnName = COLUMN_DEPTH)
    private int depth;

    public static final String COLUMN_ANCESTOR_IS_ROOT = "ancestor_is_root";
    @DatabaseField(columnName = COLUMN_ANCESTOR_IS_ROOT)
    private boolean ancestorIsRoot;

    public AccountClosure() {}

    public AccountClosure(long ancestor, long descendant, int depth, boolean ancestorIsRoot)
    {
        this.ancestor = ancestor;
        this.descendant = descendant;
        this.depth = depth;
        this.ancestorIsRoot = ancestorIsRoot;
    }

    public Long getId()
    {
        return id;
    }

    public long getAncestor()
    {
        return ancestor;
    }

    public void setAncestor(long ancestor)
    {
        this.ancestor = ancestor;
    }

    public int getDepth()
    {
        return depth;
    }

    public void setDepth(int depth)
    {
        this.depth = depth;
    }

    public long getDescendant()
    {
        return descendant;
    }

    public void setDescendant(long descendant)
    {
        this.descendant = descendant;
    }

    public boolean isAncestorRoot()
    {
        return ancestorIsRoot;
    }

    public void setAncestorIsRoot(boolean ancestorIsRoot)
    {
        this.ancestorIsRoot = ancestorIsRoot;
    }

    @Override
    public String toString()
    {
        return "AccountClosure{" +
                (ancestorIsRoot ? "(root) " : "") +
                "ancestor=" + ancestor +
                ", id=" + id +
                ", descendant=" + descendant +
                ", depth=" + depth +
                '}';
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AccountClosure that = (AccountClosure) o;

        if (ancestor != that.ancestor) return false;
        if (ancestorIsRoot != that.ancestorIsRoot) return false;
        if (descendant != that.descendant) return false;
        if (depth != that.depth) return false;
        return id != null ? id.equals(that.id) : that.id == null;
    }
}
