package org.coinage.core;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created At: 2016-11-15
 */
public class Formatters
{
    public static String formatThousands(long n)
    {
        return String.format("%,d", n);
    }

    public static String formateDate(long milliseconds, String format)
    {
        DateFormat formatter = new SimpleDateFormat(format);
        return formatter.format(new Date(milliseconds));
    }

    public static String formatIso8601utc(long milliseconds)
    {
        return formateDate(milliseconds, "yyyy-MM-dd'T'HH:mm:ssZ");
    }

    public static String formatPrettyDate(long milliseconds)
    {
        return formateDate(milliseconds, "MMM dd HH:mm");
    }

    public static String formatPrettyElapsed(double milliseconds)
    {
        if (milliseconds == 0) return "0";
        if (milliseconds < 0) return "Unknown";
        if (milliseconds > Units.DAY) return String.format("%.1f Days", milliseconds / (double) Units.DAY);
        if (milliseconds > Units.HOUR) return String.format("%.1f Hours", milliseconds / (double) Units.HOUR);
        if (milliseconds > Units.MINUTE) return String.format("%.1f Minutes", milliseconds / (double) Units.MINUTE);
        if (milliseconds > Units.SECOND) return String.format("%.1f Seconds", milliseconds / (double) Units.SECOND);
        return String.format("%d Milliseconds", (long) milliseconds);
    }

    public static String formatPrettyElapsedShort(double milliseconds)
    {
        if (milliseconds == 0) return "0";
        if (milliseconds < 0) return "Unknown";
        if (milliseconds > Units.DAY) return String.format("%.1f Day", milliseconds / (double) Units.DAY);
        if (milliseconds > Units.HOUR) return String.format("%.1f Hrs", milliseconds / (double) Units.HOUR);
        if (milliseconds > Units.MINUTE) return String.format("%.1f Min", milliseconds / (double) Units.MINUTE);
        if (milliseconds > Units.SECOND) return String.format("%.1f Sec", milliseconds / (double) Units.SECOND);
        return String.format("%d Milliseconds", (long) milliseconds);
    }

    public static String wrap(String input, int linewidth)
    {
        if (input == null) return null;
        StringBuilder sb = new StringBuilder(input);
        int i = 0;
        while (i + linewidth < sb.length() && (i = sb.lastIndexOf(" ", i + linewidth)) != -1) {
            sb.replace(i, i + 1, "\n");
        }
        return sb.toString();
    }
}
