package daniel.stadtgeschichten.helper;

import android.app.ActivityManager;
import android.content.Context;

/**
 * This helper class contains general-purpose methods.
 */
public class Helper
{
    /**
     * Check whether my service is running.
     * @param context application's context
     * @param serviceClass class of the service to look for
     * @return true if the service is running, false otherwise
     */
    public static boolean isMyServiceRunning(Context context, Class<?> serviceClass)
    {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
            if (serviceClass.getName().equals(service.service.getClassName()))
                return true;
        return false;
    }
}
