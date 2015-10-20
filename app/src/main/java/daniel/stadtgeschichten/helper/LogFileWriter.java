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
 * This class holds methods for saving a log file to the external storage.
 */
public class LogFileWriter
{
    /**
     * Checks if external storage is available for read and write.
     * @return true if the external storage is available, false otherwise
     */
    public static boolean isExternalStorageWritable()
    {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    /**
     * Create a file with the given content on the external storage in a folder called after the app.
     * @param context application's context
     * @param content content to write into the file
     */
    public static void createFile(Context context, String content)
    {
        // Get folder.
        File folder = new File(Environment.getExternalStorageDirectory(),
                context.getString(R.string.app_name));

        // Create folder if it does not exist.
        if (!folder.exists())
            folder.mkdir();

        // Create file.
        File file = new File(folder, "locations" + (System.currentTimeMillis() / 1000) + ".txt");
        try
        {
            FileWriter writer = new FileWriter(file);
            writer.append(content);
            writer.flush();
            writer.close();

            // Show little toast message.
            Toast.makeText(context, context.getString(R.string.creating_file_success), Toast.LENGTH_SHORT).show();
        }
        catch (IOException e)
        {
            Log.d(LogFileWriter.class.getSimpleName(), e.toString());
            Toast.makeText(context, context.getString(R.string.creating_file_failure), Toast.LENGTH_SHORT).show();
        }
    }
}
