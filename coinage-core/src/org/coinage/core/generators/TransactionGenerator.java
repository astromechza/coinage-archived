package org.coinage.core.generators;

import org.coinage.core.models.Account;
import org.coinage.core.models.SubTransaction;
import org.coinage.core.models.Transaction;
import org.joda.time.DateTime;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;

/**
 * Created At: 2016-11-13
 */
public class TransactionGenerator
{
    private static final Random random = new Random();

    public static Transaction addSubtransactions(Transaction t, List<Account> accounts)
    {
        Account from = accounts.get(random.nextInt(accounts.size()));
        Account to = from;
        while (to == from)
        {
            to = accounts.get(random.nextInt(accounts.size()));
        }
        return addSubtransactions(t, from, to);
    }

    public static Transaction addSubtransactions(Transaction t, Account from, Account to)
    {
        BigDecimal value = new BigDecimal(random.nextInt(1000) / 100.0);
        SubTransaction st1 = new SubTransaction(t, from, to, value.negate());
        SubTransaction st2 = new SubTransaction(t, to, from, value);
        t.getSubTransactions().add(st1);
        t.getSubTransactions().add(st2);
        return t;
    }
}
