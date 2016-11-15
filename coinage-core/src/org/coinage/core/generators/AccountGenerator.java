package org.coinage.core.generators;

import org.coinage.core.models.Account;

import java.util.List;
import java.util.Random;

/**
 * Created At: 2016-11-13
 */
public class AccountGenerator
{
    private static final String[] adjectives = {
        "huge", "large", "strange", "wise",
        "big", "fat", "red", "sad", "thin",
        "dry", "spry", "wry",
        "fast", "great", "quick", "short", "tall",
        "fickly", "polite",
        "bumpy", "heavy", "icy", "shiny", "tiny",
        "able", "gentle", "hollow", "narrow", "shallow", "simple",
        "quick", "fast", "slow", "speeding", "rushing", "bustling",
        "rapid", "snappy", "swift", "hasty", "brief",
    };

    private static final String[] animals = {
        "beaver", "cow", "horse", "panda", "sloth",
        "whale", "dog", "tiger", "lion", "bear",
        "giraffe", "cheetah", "mouse", "bat", "cat",
        "gorilla", "ape", "hamster", "snake"
    };

    private static final Random random = new Random();

    public static String fakeAccountName()
    {
        String firstPart = adjectives[random.nextInt(adjectives.length)];
        String secondPart = animals[random.nextInt(animals.length)];
        firstPart = firstPart.substring(0, 1).toUpperCase() + firstPart.substring(1);
        secondPart = secondPart.substring(0, 1).toUpperCase() + secondPart.substring(1);
        return firstPart + secondPart;
    }

    public static Account fakeAccountInTree(List<Account> possibleParents)
    {
        int i = random.nextInt(possibleParents.size() + 1);
        Account parent = null;
        if (i < possibleParents.size()) parent = possibleParents.get(random.nextInt(possibleParents.size()));
        return new Account(fakeAccountName(), parent);
    }

    public static Account fakeAccountInTree(Account... possibleParents)
    {
        int i = random.nextInt(possibleParents.length + 1);
        Account parent = null;
        if (i < possibleParents.length) parent = possibleParents[random.nextInt(possibleParents.length)];
        return new Account(fakeAccountName(), parent);
    }

    public static Account fakeSubAccount(List<Account> possibleParents)
    {
        Account parent = possibleParents.get(random.nextInt(possibleParents.size()));
        return new Account(fakeAccountName(), parent);
    }

    public static Account fakeSubAccount(Account... possibleParents)
    {
        Account parent = possibleParents[random.nextInt(possibleParents.length)];
        return new Account(fakeAccountName(), parent);
    }

    public static Account fakeSubAccount(Account parent)
    {
        return new Account(fakeAccountName(), parent);
    }

    public static Account fakeAccount()
    {
        return new Account(fakeAccountName());
    }
}
