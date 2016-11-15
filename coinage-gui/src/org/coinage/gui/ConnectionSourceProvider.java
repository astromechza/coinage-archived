package org.coinage.gui;

import com.j256.ormlite.support.ConnectionSource;

/**
 * Created At: 2016-11-15
 */
public class ConnectionSourceProvider
{
    private static ConnectionSource activeConnectionSource = null;

    public static ConnectionSource get()
    {
        if (activeConnectionSource == null) throw new RuntimeException("Connection source has not been initialised!");
        return activeConnectionSource;
    }

    public static void set(ConnectionSource source)
    {
        activeConnectionSource = source;
    }
}
