package daniel.stadtgeschichten.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.lang3.StringEscapeUtils;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

import daniel.stadtgeschichten.R;
import daniel.stadtgeschichten.helper.Helper;
import daniel.stadtgeschichten.helper.Logger;
import daniel.stadtgeschichten.helper.StoryReader;
import daniel.stadtgeschichten.service.LocationPlayerService;


public class MainActivity extends AppCompatActivity implements OnItemSelectedListener
{
    public final static String EXTRA_TITLE = "daniel.stadtgeschichten.TITLE";
    private final static String LOG_TAG = "MainActivity";
    private static final String STATE_SELECTED_STORY = "selectedStory";
    private static final String STATE_TEXT = "text";
    private static final String STATE_IS_PLAYING = "isPlaying";
    private static final String STATE_CURRENT_SPINNER_INDEX = "currentSpinnerIndex";

    /**
     * Logger
     */
    private Logger logger;

    /**
     * Story reader
     */
    private StoryReader storyReader;

    /**
     * Selected story's title
     */
    private String selectedStoryTitle;

    /**
     * Index of the current selection of the spinner used for checking whether onItemSelected is
     * directly called after the instantiation of this activity.
     */
    private int spinnerCurrentIndex;

    /**
     * Latitude of nearest next point of interest
     */
    private double nextLatitude;

    /**
     * Longitude of nearest next point of interest
     */
    private double nextLongitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        logger = Logger.getLogger(this);

        // Create an ArrayAdapter using the the stories title list and the default spinner layout.
        try
        {
            storyReader = new StoryReader(this);
        }
        catch (IOException | XmlPullParserException e)
        {
            logger.e(LOG_TAG, e.toString());
            Toast.makeText(this, getString(R.string.stories_read_failure),Toast.LENGTH_SHORT)
                    .show();
        }
        String[] stories = storyReader.getStoryTitles();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, stories);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner and set the OnItemSelectedListener.
        Spinner spinner = (Spinner) findViewById(R.id.stories_spinner);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        // Check if this activity has been started by the service.
        Intent intent = getIntent();
        if (intent.hasExtra(LocationPlayerService.EXTRA_TITLE))
        {
            // The service has passed in the selected story.
            selectedStoryTitle = intent.getStringExtra(LocationPlayerService.EXTRA_TITLE);

            // Search and the set the current story in the spinner.
            spinner.setSelection(adapter.getPosition(selectedStoryTitle));
        }
        else
        {
            // Select the first story as default.
            selectedStoryTitle = stories[0];
        }
        if (intent.hasExtra(LocationPlayerService.EXTRA_IS_PLAYING))
        {
            // The service has passed in the current playback state.
            Button button = (Button) findViewById(R.id.play_pause);
            if (intent.getBooleanExtra(LocationPlayerService.EXTRA_IS_PLAYING, false))
                button.setText(getString(R.string.pause));
            else
                button.setText(getString(R.string.play));
        }

        // Get the index of the selected item of the spinner.
        spinnerCurrentIndex = spinner.getSelectedItemPosition();

        // Presses on the volume keys should change the media playback volume, also when nothing is
        // currently being played.
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }

    @Override
    protected void onResume()
    {
        // Register to receive broadcasted Intents.
        LocalBroadcastManager.getInstance(this).registerReceiver(newTextHandler,
                new IntentFilter(LocationPlayerService.EVENT_NEW_RECORD));
        LocalBroadcastManager.getInstance(this).registerReceiver(storyEndedHandler,
                new IntentFilter(LocationPlayerService.EVENT_END));
        LocalBroadcastManager.getInstance(this).registerReceiver(nextPOIHandler,
                new IntentFilter(LocationPlayerService.EVENT_NEXT_POI));

        super.onResume();
    }

    /**
     * This broadcast receiver is called every time an intent with the action
     * LocationPlayerService.EVENT_NEW_RECORD, which is sent when text should be added to the text
     * view, is received.
     */
    private BroadcastReceiver newTextHandler = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String text = intent.getStringExtra(LocationPlayerService.EXTRA_RECORD);
            addText(text);
        }
    };

    /**
     * This broadcast receiver is called every time an intent with the action
     * LocationPlayerService.EVENT_END, which is sent when the story is over, is received.
     */
    private BroadcastReceiver storyEndedHandler = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            // Add THE END. to the text.
            addText("\nTHE END.");

            // Reset play/pause button.
            Button playPauseButton = (Button) findViewById(R.id.play_pause);
            playPauseButton.setText(getString(R.string.play));
        }
    };

    /**
     * This broadcast receiver is called every time an intent with the action
     * LocationPlayerService.EVENT_NEXT_POI, which contains the coordinates of the nearest next
     * point of interest, is received.
     */
    private BroadcastReceiver nextPOIHandler = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            nextLatitude = intent.getDoubleExtra(LocationPlayerService.EXTRA_NEXT_LATITUDE, 0);
            nextLongitude = intent.getDoubleExtra(LocationPlayerService.EXTRA_NEXT_LONGITUDE, 0);
        }
    };

    /**
     * Add text to the text view.
     * @param text text to add
     */
    private void addText(String text)
    {
        TextView textView = (TextView) findViewById(R.id.record);
        String oldText = (String) textView.getText();
        Log.d(LOG_TAG, text);
        if (oldText.isEmpty())
            textView.setText(StringEscapeUtils.unescapeJava(text));
        else
            textView.setText(oldText + "\n\n" + StringEscapeUtils.unescapeJava(text));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        // Save selected story.
        outState.putString(STATE_SELECTED_STORY, selectedStoryTitle);

        // Save shown text.
        TextView textView = (TextView) findViewById(R.id.record);
        outState.putCharSequence(STATE_TEXT, textView.getText());

        // Save play/pause button text.
        Button button = (Button) findViewById(R.id.play_pause);
        outState.putCharSequence(STATE_IS_PLAYING, button.getText());

        // Save current spinner index.
        outState.putInt(STATE_CURRENT_SPINNER_INDEX, spinnerCurrentIndex);

        // Always call the superclass so it can save the view hierarchy state.
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState)
    {
        // Always call the superclass so it can restore the view hierarchy.
        super.onRestoreInstanceState(savedInstanceState);

        // Restore selected story.
        selectedStoryTitle = savedInstanceState.getString(STATE_SELECTED_STORY);

        // Restore shown text.
        TextView textView = (TextView) findViewById(R.id.record);
        textView.setText(savedInstanceState.getCharSequence(STATE_TEXT));

        // Restore current spinner index.
        spinnerCurrentIndex = savedInstanceState.getInt(STATE_CURRENT_SPINNER_INDEX);
        Spinner spinner = (Spinner) findViewById(R.id.stories_spinner);
        spinner.setSelection(spinnerCurrentIndex);

        // Restore play/pause button text.
        Button button = (Button) findViewById(R.id.play_pause);
        button.setText(savedInstanceState.getString(STATE_IS_PLAYING));
    }

    @Override
    protected void onPause()
    {
        // Unregister the broadcast receiver.
        LocalBroadcastManager.getInstance(this).unregisterReceiver(newTextHandler);

        super.onPause();
    }

    /**
     * Play or pause the selected story by clicking the button.
     * @param view pressed button
     */
    public void play_pause(View view)
    {
        Button button = (Button) view;
        Intent intent = new Intent(this, LocationPlayerService.class);
        if (button.getText().equals(getString(R.string.play)))
        {
            // Play.
            intent.setAction(LocationPlayerService.ACTION_PLAY);
            intent.putExtra(EXTRA_TITLE, selectedStoryTitle);

            // Change button text.
            button.setText(getString(R.string.pause));
        }
        else
        {
            // Pause.
            intent.setAction(LocationPlayerService.ACTION_PAUSE);

            // Change button text.
            button.setText(getString(R.string.play));
        }

        // Start the service.
        startService(intent);
    }

    /**
     * Stop the playing back the current story, and terminate the service.
     * @param view pressed button
     */
    public void stop(View view)
    {
        stopPlaying();
    }

    private void stopPlaying()
    {
        reset();

        // Inform service.
        Intent intent = new Intent(this, LocationPlayerService.class);
        intent.setAction(LocationPlayerService.ACTION_STOP);
        startService(intent);
    }

    /**
     * Reset activity.
     */
    private void reset()
    {
        // Reset text view.
        TextView textView = (TextView) findViewById(R.id.record);
        textView.setText("");

        // Reset play/pause button.
        Button playPauseButton = (Button) findViewById(R.id.play_pause);
        playPauseButton.setText(getString(R.string.play));
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
    {
        // This method is called every time the activity is created.

        // Ignore same selections.
        if (spinnerCurrentIndex == parent.getSelectedItemPosition())
            return;

        spinnerCurrentIndex = parent.getSelectedItemPosition();
        selectedStoryTitle = (String) parent.getItemAtPosition(position);

        // Do not start service to stop it.
        if (Helper.isMyServiceRunning(this, LocationPlayerService.class))
            stopPlaying();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent)
    {
        // Nothing should happen.
    }

    /**
     * Open a mapping application by passing the coordinates of the next point of interest to it.
     * @param view pressed button
     */
    public void openMap(View view)
    {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("geo:0,0?q=" + String.valueOf(nextLatitude) + "," +
                String.valueOf(nextLongitude) + "(Closest point of interest)"));
        if (intent.resolveActivity(getPackageManager()) != null)
            startActivity(intent);
        // else TODO show error
    }
}
