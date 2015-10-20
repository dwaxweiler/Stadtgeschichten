package daniel.stadtgeschichten.helper;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import daniel.stadtgeschichten.R;

/**
 * This logger logs to LogCat and writes the same to a log file.
 */
public class Logger
{
    /**
     * Indicates whether the logging is enabled or disabled
     */
    private static final boolean IS_LOGGING = true;

    /**
     * Name of the log file
     */
    private static final String logFileName = "log.txt";

    /**
     * Log tag
     */
    private static final String LOG_TAG = "Logger";

    /**
     * File to log to
     */
    private File logFile;

    /**
     * Application's context
     */
    private Context context;

    /**
     * Create a logger.
     * @param context application's context
     */
    public Logger(Context context)
    {
        this.context = context;

        // Get folder.
        File folder = new File(Environment.getExternalStorageDirectory(),
                context.getString(R.string.app_name));

        // Create folder if it does not exist.
        if (!folder.exists())
            folder.mkdir();

        // Get file.
        logFile = new File(folder, logFileName);

        // Create log file if it does not exist.
        if (!logFile.exists())
        {
            try
            {
                logFile.createNewFile();
            }
            catch (IOException e)
            {
                Toast.makeText(context, context.getString(R.string.log_file_creation_failure),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Get a logger.
     * @param context application's context
     * @return logger
     */
    public static Logger getLogger(Context context)
    {
        return new Logger(context);
    }

    /**
     * Write the given text to the log file and append a carriage return and a line feed.
     * @param text text to write
     */
    private void writeLine(String text)
    {
        try
        {
            FileWriter writer = new FileWriter(logFile, true);
            writer.append(text);
            writer.append("\r\n");
            writer.flush();
            writer.close();
        }
        catch (IOException e)
        {
            Log.e(LOG_TAG, e.toString());
            Toast.makeText(context, context.getString(R.string.log_file_writing_failure),
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void d(String tag, String message)
    {
        if (!IS_LOGGING)
            return;

        Log.d(tag, message);
        writeLine("D: " + tag + " : " + message);
    }

    public void e(String tag, String message)
    {
        if (!IS_LOGGING)
            return;

        Log.e(tag, message);
        writeLine("E: " + tag + " : " + message);
    }

    public void i(String tag, String message)
    {
        if (!IS_LOGGING)
            return;

        Log.i(tag, message);
        writeLine("I: " + tag + " : " + message);
    }

    public void v(String tag, String message)
    {
        if (!IS_LOGGING)
            return;

        Log.v(tag, message);
        writeLine("V: " + tag + " : " + message);
    }

    public void w(String tag, String message)
    {
        if (!IS_LOGGING)
            return;

        Log.w(tag, message);
        writeLine("W: " + tag + " : " + message);
    }
}
