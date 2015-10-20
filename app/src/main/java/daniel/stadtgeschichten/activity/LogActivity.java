package daniel.stadtgeschichten.activity;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import daniel.stadtgeschichten.R;
import daniel.stadtgeschichten.helper.LogFileWriter;

/**
 * Activity used to test the location accuracy of the GPS sensor. Unused at the moment.
 */
public class LogActivity extends AppCompatActivity
{
    private static final String STATE_IS_LOGGING = "isLogging";
    private static final String STATE_LOCATIONS = "locations";
    private static final String STATE_START_STOP = "startStop";
    private static final long LOG_TIME_INTERVAL = 500; // milliseconds
    private LocationManager locationManager;
    private LocationListener locationListener;
    private boolean isLogging = false;
    private ArrayList<Location> locations = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);

        // Get the location manager.
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        // Create the location listener
        locationListener = new LocationListener()
        {
            @Override
            public void onLocationChanged(Location location)
            {
                locations.add(location);
                displayLocation(location);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras)
            {
                // TODO
            }

            @Override
            public void onProviderEnabled(String provider)
            {
                // TODO
            }

            @Override
            public void onProviderDisabled(String provider)
            {
                // TODO
            }
        };
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        // Save the user's current state.
        outState.putBoolean(STATE_IS_LOGGING, isLogging);
        outState.putParcelableArrayList(STATE_LOCATIONS, locations);
        outState.putCharSequence(STATE_START_STOP,
                ((TextView) findViewById(R.id.toggleLogging)).getText());

        // Unregister the location listener.
        locationManager.removeUpdates(locationListener);

        // Change button text.
        Button button = (Button) findViewById(R.id.toggleLogging);
        button.setText(R.string.start);

        // Always call the superclass so it can save the view hierarchy state.
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState)
    {
        // Always call the superclass so it can restore the view hierarchy.
        super.onRestoreInstanceState(savedInstanceState);

        // Restore value of members from saved state
        locations = savedInstanceState.getParcelableArrayList(STATE_LOCATIONS);
        isLogging = savedInstanceState.getBoolean(STATE_IS_LOGGING);
        ((TextView) findViewById(R.id.toggleLogging)).setText(savedInstanceState.getCharSequence(STATE_START_STOP));

        // Display already existing locations.
        for(int i = 0; i < locations.size(); i++)
        {
            displayLocation(locations.get(i));
        }

        // Resume location listener.
        if (isLogging)
        {
            // Register the location listener in the location manager.
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOG_TIME_INTERVAL, 0, locationListener);

            // Change button text.
            Button button = (Button) findViewById(R.id.toggleLogging);
            button.setText(R.string.stop);
        }
    }

    /**
     * Display the provided location's values.
     * @param location Location object
     */
    private void displayLocation(Location location)
    {
        if (location == null)
            throw new NullPointerException();

        // Create text views.
        TextView latitude = new TextView(this);
        latitude.setText(String.valueOf(location.getLatitude()));
        TextView longitude = new TextView(this);
        longitude.setText(String.valueOf(location.getLongitude()));
        TextView accuracy = new TextView(this);
        accuracy.setText(String.valueOf(location.getAccuracy()));

        // Create row and add elements.
        TableRow tableRow = new TableRow(this);
        tableRow.addView(latitude);
        tableRow.addView(longitude);
        tableRow.addView(accuracy);

        // Add row to table.
        TableLayout tableLayout = (TableLayout) findViewById(R.id.table);
        tableLayout.addView(tableRow);
    }

    /**
     * Toggle logging.
     * @param view Button
     */
    public void toggleLogging(View view)
    {
        Button button = (Button) view;
        if (isLogging)
        {
            // Stop logging.
            // Unregister the location listener.
            locationManager.removeUpdates(locationListener);

            // Change button text.
            button.setText(R.string.start);
        }
        else
        {
            // Start logging.
            // Register the location listener in the location manager.
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOG_TIME_INTERVAL, 0, locationListener);

            // Change button text.
            button.setText(R.string.stop);
        }
        isLogging = !isLogging;
    }

    /**
     * Export the locations in a text file
     * @param view pressed button
     */
    public void exportData(View view)
    {
        if (!LogFileWriter.isExternalStorageWritable())
        {
            Toast.makeText(this, getString(R.string.external_storage_unavailable),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        LogFileWriter.createFile(this, getLocationsAsString());
    }

    /**
     * Get the latitude, longitude and accuracy of each location as string.
     * The first line contains the three different data types seperated by commas.
     * The locations follow with one line per location.
     * @return string, in which the lines are separated by "\r\n"
     */
    private String getLocationsAsString()
    {
        String output = "Latitude,Longitude,Accuracy\r\n";
        for (int i = 0; i < locations.size(); i++)
        {
            output += locations.get(i).getLatitude() + ","
                    + locations.get(i).getLongitude() + ","
                    + locations.get(i).getAccuracy() + "\r\n";
        }
        return output;
    }

    /**
     * Delete all logged locations on screen and behing the scenes.
     * @param view pressed button
     */
    public void reset(View view)
    {
        locations.clear();

        // Remove views containing the values row by row.
        TableLayout table = (TableLayout) findViewById(R.id.table);
        for (int i = table.getChildCount() - 1; i > 0; i--)
        // Begin with the last child to be sure to get them all.
        // Leave out first row (i=0) as it contains the headers.
        {
            table.removeViewAt(i);
        }
    }
}
