package daniel.stadtgeschichten.service;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Pair;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

import daniel.stadtgeschichten.activity.MainActivity;
import daniel.stadtgeschichten.R;
import daniel.stadtgeschichten.exception.AlreadyPlayingException;
import daniel.stadtgeschichten.helper.LocationHelper;
import daniel.stadtgeschichten.helper.Logger;
import daniel.stadtgeschichten.helper.StoryReader;
import daniel.stadtgeschichten.helper.VariableHelper;
import daniel.stadtgeschichten.model.AbsOperator;
import daniel.stadtgeschichten.model.AbsStatement;
import daniel.stadtgeschichten.model.AssignmentStatement;
import daniel.stadtgeschichten.model.Circle;
import daniel.stadtgeschichten.model.EndStatement;
import daniel.stadtgeschichten.model.EqualityOperator;
import daniel.stadtgeschichten.model.IfStatement;
import daniel.stadtgeschichten.model.IncrementStatement;
import daniel.stadtgeschichten.model.PlayStatement;
import daniel.stadtgeschichten.model.Story;

/**
 * This {@link IntentService} subclass plays audio and for handles task requests asynchronously
 * on a separate handler thread.
 */
public class LocationPlayerService extends Service
        implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
                MediaPlayer.OnCompletionListener, AudioManager.OnAudioFocusChangeListener,
                LocationHelper.OnLocationListener
{
    public static final String ACTION_PLAY = "daniel.stadtgeschichten.action.PLAY";
    public static final String ACTION_PAUSE = "daniel.stadtgeschichten.action.PAUSE";
    public static final String ACTION_STOP = "daniel.stadtgeschichten.action.STOP";
    public static final String EVENT_NEW_RECORD = "daniel.stadtgeschichten.action.NEW_RECORD";
    public static final String EVENT_END = "daniel.stadtgeschichten.action.END";
    public static final String EVENT_NEXT_POI = "daniel.stadtgeschichten.action.NEXT_POI";
    public static final String EXTRA_TITLE = "daniel.stadtgeschichten.TITLE";
    public static final String EXTRA_IS_PLAYING = "daniel.stadtgeschichten.IS_PLAYING";
    public static final String EXTRA_RECORD = "daniel.stadtgeschichten.RECORD";
    public static final String EXTRA_NEXT_LATITUDE = "daniel.stadtgeschichten.NEXT_LATITUDE";
    public static final String EXTRA_NEXT_LONGITUDE = "daniel.stadtgeschichten.NEXT_LONGITUDE";
    private static final String LOG_TAG = "LocationPlayerService";

    /**
     * Unique identifier of the notification
     */
    private static final int NOTIFICATION_ID = 424242;

    /**
     * Logger
     */
    private Logger logger;

    /**
     * Media player used for playback
     */
    private MediaPlayer player = null;

    /**
     * Audio manager used to request and abandon audio focus
     */
    private AudioManager audioManager;

    /**
     * Location helper used to fetch the current location
     */
    private LocationHelper locationHelper;

    /**
     * Variable helper used to manage the used variables
     */
    private VariableHelper variableHelper;

    /**
     * Notification manager used to update the notification
     */
    private NotificationManager notificationManager;

    /**
     * Selected story's title
     */
    private String selectedStoryTitle;

    /**
     * Selected story.
     */
    private Story selectedStory;

    /**
     * Current play statement.
     */
    private PlayStatement currentPlayStatement;

    /**
     * True if an end tag has been read, false otherwise
     */
    private boolean isEndTagRead = false;

    @Override
    public void onCreate()
    {
        logger = Logger.getLogger(this);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        locationHelper = new LocationHelper(this, this);
        variableHelper = new VariableHelper();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        if (intent == null || intent.getAction() == null)
        {
            logger.e(LOG_TAG, "No action was specified. The service is stopped.");
            stopSelf();
            return START_NOT_STICKY;
        }

        // React depending on the requested action.
        if (intent.getAction().equals(ACTION_PLAY))
        {
            selectedStoryTitle = intent.getStringExtra(MainActivity.EXTRA_TITLE);

            // Play or resume.
            if (player == null)
                play();
            else
                resume();
        }
        else if (intent.getAction().equals(ACTION_PAUSE))
            pause();
        else if (intent.getAction().equals(ACTION_STOP))
            stop();
        else
        {
            logger.e(LOG_TAG, "Unknown action");
            stopSelf();
            return START_NOT_STICKY;
        }

        // Tell the system to recreate the service if it gets killed.
        return START_STICKY;
    }

    /**
     * Get the current story, start location tracking and prepare the player.
     */
    private void play()
    {
        // Get StoryReader.
        StoryReader reader;
        try
        {
            reader = new StoryReader(this);
        }
        catch (IOException | XmlPullParserException e)
        {
            logger.e(LOG_TAG, e.toString());
            Toast.makeText(this, getString(R.string.stories_read_failure),
                    Toast.LENGTH_SHORT).show();
            stopSelf();
            return;
        }

        // Get selected story.
        selectedStory = reader.getStory(selectedStoryTitle);

        // Process initialisation statements.
        variableHelper.processStatements(selectedStory.getInitStatements());

        // Set possible spots in the LocationHelper.
        locationHelper.setSpots(selectedStory.getSpots());

        // Send the record of the intro audio file to the activity.
        Intent intent = new Intent(EVENT_NEW_RECORD);
        intent.putExtra(EXTRA_RECORD, selectedStory.getIntroRecord());
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

        // Reset current play statement.
        currentPlayStatement = null;

        // Start location tracking.
        locationHelper.startLogging();

        initializePlayer();
        registerAsForeground();
        prepareIntroAudio();
    }

    /**
     * Pause location tracking and player if it is playing.
     */
    private void pause()
    {
        // Pause location tracking.
        locationHelper.stopLogging();

        // Pause player.
        if (player != null && player.isPlaying())
            player.pause();
    }

    /**
     * Resume location tracking and player if it has been playing.
     */
    private void resume()
    {
        // Resume location tracking.
        locationHelper.startLogging();

        // Resume player.
        if (player != null && !player.isPlaying())
            player.start();
    }

    /**
     * Stop location tracking, player and service.
     */
    private void stop()
    {
        // Stop location tracking.
        locationHelper.stopLogging();

        // Stop player.
        releasePlayer();

        // Stop service.
        unregisterAsForeground();
        stopSelf();
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        // Do not allow binding.
        return null;
    }

    @Override
    public void onDestroy()
    {
        // Abandon audio focus.
        abandonAudioFocus();

        // Stop location tracking.
        locationHelper.stopLogging();

        // Stop player.
        releasePlayer();

        // Stop service.
        unregisterAsForeground();
        stopSelf();
    }

    /**
     * Initialize the player.
     */
    private void initializePlayer()
    {
        player = new MediaPlayer();
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
        player.setWakeMode(this, PowerManager.PARTIAL_WAKE_LOCK);
    }

    /**
     * Prepare the intro audio file for playback.
     */
    private void prepareIntroAudio()
    {
        prepareAudioFile(selectedStory.getIntroAudioFileName(), 1.0f);
    }

    /**
     * Prepare the audio file with the given name for playback.
     * @param fileName name of the audio file to play
     * @param volume volume, ranging from 0.0 to 1.0
     */
    private void prepareAudioFile(String fileName, float volume)
    {
        try
        {
            AssetFileDescriptor fileDescriptor = getAssets().openFd(StoryReader.STORIES_FOLDER + "/"
                    + selectedStory.getFolderName() + "/" + fileName);
            player.setDataSource(fileDescriptor.getFileDescriptor(),
                    fileDescriptor.getStartOffset(),
                    fileDescriptor.getLength());
            player.setVolume(volume, volume);
            player.prepareAsync();
        }
        catch (IOException e)
        {
            logger.e(LOG_TAG, e.toString());
            Toast.makeText(this, getString(R.string.stories_read_failure), Toast.LENGTH_SHORT)
                    .show();
            stopSelf();
        }
        catch (IllegalStateException e)
        {
            logger.e(LOG_TAG, "With file " + fileName + ": " + e.toString());
            Toast.makeText(this, getString(R.string.stories_read_failure), Toast.LENGTH_SHORT)
                    .show();
            stopSelf();
        }
    }

    /**
     * Stop playback if the player is still playing, release the player and set it to null.
     */
    private void releasePlayer()
    {
        if (player == null)
            return;

        if (player.isPlaying())
            player.stop();
        player.release();
        player = null;
    }

    /**
     * Register as foreground service.
     */
    private void registerAsForeground()
    {
        startForeground(NOTIFICATION_ID, buildNotification());
    }

    /**
     * Unregister as foreground service.
     */
    private void unregisterAsForeground()
    {
        stopForeground(true);
    }

    private Notification buildNotification()
    {
        // Create an intent to start the activity.
        Intent notificationIntent = new Intent(this, MainActivity.class);

        // Append the selected story title.
        notificationIntent.putExtra(LocationPlayerService.EXTRA_TITLE, selectedStoryTitle);

        // Append the playback state.
        boolean isPlaying = true;
        if (player != null)
            isPlaying = player.isPlaying();
        notificationIntent.putExtra(LocationPlayerService.EXTRA_IS_PLAYING, isPlaying);

        // Create a back stack for the started activity to ensure that navigating backward from the
        // activity leads out of the app to the home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(notificationIntent);
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0,
                PendingIntent.FLAG_UPDATE_CURRENT);

        // Create the notification.
        return new NotificationCompat.Builder(this)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(selectedStoryTitle)
                .setOngoing(true)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.icon)
                .build();
    }

    @Override
    public void onPrepared(MediaPlayer player)
    {
        // Start playback.
        if (requestAudioFocus())
            player.start();

        // Update notification.
        notificationManager.notify(NOTIFICATION_ID, buildNotification());
    }

    @Override
    public void onCompletion(MediaPlayer mp)
    {
        // Stop this service when the end tag has been read.
        if (isEndTagRead)
            stop(); // TODO what happens when activity is in background?

        // Update notification.
        notificationManager.notify(NOTIFICATION_ID, buildNotification());

        // Reset the player.
        player.reset();

        // Send the coordinates of the closest spot to the activity.
        sendCoordinatesToActivity();
    }

    /**
     * Send the coordinates of the closest spot to the activity.
     */
    private void sendCoordinatesToActivity()
    {
        Pair<Double, Double> closestSpot = locationHelper.getClosestSpot();
        if (closestSpot.first != 360.0 && closestSpot.second != 360.0)
        {
            Intent intent = new Intent(EVENT_NEXT_POI);
            intent.putExtra(EXTRA_NEXT_LATITUDE, closestSpot.first);
            intent.putExtra(EXTRA_NEXT_LONGITUDE, closestSpot.second);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
    }

    @Override
    public boolean onError(MediaPlayer player, int what, int extra)
    {
        player.reset();
        //initializePlayer(); // TODO -> http://developer.android.com/reference/android/media/MediaPlayer.OnErrorListener.html
        return false;
    }

    /**
     * Request permanent audio focus as music stream.
     * @return true if the request has been successful, false otherwise
     */
    private boolean requestAudioFocus()
    {
        int result = audioManager.requestAudioFocus(
                this,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
        );
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }

    /**
     * Abandon the audio focus.
     */
    private void abandonAudioFocus()
    {
        audioManager.abandonAudioFocus(this);
    }

    @Override
    public void onAudioFocusChange(int focusChange)
    {
        // React depending on audio focus changes.
        switch (focusChange)
        {
            case AudioManager.AUDIOFOCUS_GAIN:
                // Resume playback.
                if (player == null)
                {
                    initializePlayer();
                    prepareAudioFile(currentPlayStatement.getAudioFileName(),
                            currentPlayStatement.getVolume());
                }
                else if (!player.isPlaying())
                    player.start();
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                // Lost focus for an unknown amount of time.
                locationHelper.stopLogging();
                releasePlayer();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                // Lost focus for a short time.
                // Pause playback since it is likely to resume.
                pause();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // Lost focus for a short time, but the playback could continue at an attenuated level.
                // Pause playback since listener could miss important information.
                pause();
                break;
        }
    }

    @Override
    public void onLocationArrived(Circle circle)
    {
        // Ignore event since the player is already playing.
        if (player.isPlaying())
            return;

        // Process statements.
        for (AbsStatement statement : circle.getStatements())
        {
            if (statement instanceof AssignmentStatement)
                processAssignmentStatement((AssignmentStatement) statement);
            else if (statement instanceof EndStatement)
                processEndStatement((EndStatement) statement);
            else if (statement instanceof IfStatement)
            {
                try
                {
                    processIfStatement(circle.getTitle(), (IfStatement) statement);
                }
                catch (AlreadyPlayingException e)
                {
                    break; // Stop processing.
                }
            }
            else if (statement instanceof IncrementStatement)
                processIncrementStatement((IncrementStatement) statement);
            else if (statement instanceof PlayStatement)
            {
                try
                {
                    processPlayStatement(circle.getTitle(), (PlayStatement) statement);
                }
                catch (AlreadyPlayingException e)
                {
                    break; // Stop processing.
                }
            }
            else
                logger.w(LOG_TAG, "An unknown statement was ignored.");
        }
    }

    @Override
    public void onFirstLocation()
    {
        // Send the coordinates of the closest spot to the activity.
        sendCoordinatesToActivity();
    }

    /**
     * Execute an assignment statement.
     * @param statement statement
     */
    private void processAssignmentStatement(AssignmentStatement statement)
    {
        variableHelper.setVariable(statement.getVariable(), statement.getValue());
    }

    /**
     * Execute an end statement.
     * @param statement statement
     */
    private void processEndStatement(EndStatement statement)
    {
        // Notify activity.
        Intent intent = new Intent(EVENT_END);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

        isEndTagRead = true;
    }

    /**
     * Execute an if statement.
     * @param title title to display
     * @param statement statement
     */
    private void processIfStatement(String title, IfStatement statement)
            throws AlreadyPlayingException
    {
        boolean isConditionFulfilled = true;
        for (AbsOperator operator : statement.getConditions())
        {
            if (operator instanceof EqualityOperator)
            {
                EqualityOperator equalityOperator = (EqualityOperator) operator;

                // Treat the elements as variable names if there actually exist variables with these
                // names.
                long value1;
                long value2;
                if (variableHelper.hasVariable(equalityOperator.getElement1()))
                    value1 = variableHelper.getValue(equalityOperator.getElement1());
                else
                    value1 = Integer.parseInt(equalityOperator.getElement1());
                if (variableHelper.hasVariable(equalityOperator.getElement2()))
                    value2 = variableHelper.getValue(equalityOperator.getElement2());
                else
                    value2 = Integer.parseInt(equalityOperator.getElement2());

                if (value1 != value2)
                {
                    // Every condition must be true.
                    isConditionFulfilled = false;
                    break;
                }
            }
            else
                logger.w(LOG_TAG, "An unknown operator was ignored");
        }

        if (isConditionFulfilled)
        {
            // Process Then statements.
            for (AbsStatement thenStatement : statement.getThenStatements())
            {
                if (thenStatement instanceof AssignmentStatement)
                    processAssignmentStatement((AssignmentStatement) thenStatement);
                else if (thenStatement instanceof EndStatement)
                    processEndStatement((EndStatement) thenStatement);
                else if (thenStatement instanceof IncrementStatement)
                    processIncrementStatement((IncrementStatement) thenStatement);
                else if (thenStatement instanceof PlayStatement)
                    processPlayStatement(title, (PlayStatement) thenStatement);
                else if (!(thenStatement instanceof IfStatement))
                {
                    // Ignore if statements at this level.
                    logger.w(LOG_TAG, "An unknown statement was ignored.");
                }
            }
        }
        else
        {
            // Process Else statements.
            for (AbsStatement elseStatement : statement.getElseStatements())
            {
                if (elseStatement instanceof AssignmentStatement)
                    processAssignmentStatement((AssignmentStatement) elseStatement);
                else if (elseStatement instanceof EndStatement)
                    processEndStatement((EndStatement) elseStatement);
                else if (elseStatement instanceof IncrementStatement)
                    processIncrementStatement((IncrementStatement) elseStatement);
                else if (elseStatement instanceof PlayStatement)
                    processPlayStatement(title, (PlayStatement) elseStatement);
                else if (!(elseStatement instanceof IfStatement))
                {
                    // Ignore if statements at this level.
                    logger.w(LOG_TAG, "An unknown statement was ignored.");
                }
            }
        }
    }

    /**
     * Execute an incrementation statement.
     * @param statement statement
     */
    private void processIncrementStatement(IncrementStatement statement)
    {
        int oldValue;
        if (variableHelper.hasVariable(statement.getVariable()))
            oldValue = variableHelper.getValue(statement.getVariable());
        else
            oldValue = 0;
        variableHelper.setVariable(statement.getVariable(), oldValue + statement.getValue());
    }

    /**
     * Execute a play statement.
     * @param title title to display
     * @param statement statement
     * @throws AlreadyPlayingException
     */
    private void processPlayStatement(String title, PlayStatement statement)
            throws AlreadyPlayingException
    {
        if (player.isPlaying())
            throw new AlreadyPlayingException();

        // Send the title of the current circle to the activity.
        Intent intent;
        if (title != null && !title.isEmpty())
        {
            intent = new Intent(EVENT_NEW_RECORD);
            intent.putExtra(EXTRA_RECORD, title);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }

        // Send the record of the audio file to the activity.
        if (statement.getText() != null && !statement.getText().isEmpty())
        {
            intent = new Intent(EVENT_NEW_RECORD);
            intent.putExtra(EXTRA_RECORD, statement.getText());
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }

        // Prepare playback.
        currentPlayStatement = statement;
        prepareAudioFile(statement.getAudioFileName(), statement.getVolume());
    }
}
