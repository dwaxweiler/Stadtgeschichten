package daniel.stadtgeschichten.model;

/**
 * This class represents the audio file playing back statement.
 */
public class PlayStatement extends AbsStatement
{
    /**
     * Name of the audio file
     */
    private String audioFileName;

    /**
     * Written version of the played audio file, null or empty string when not specified
     */
    private String text = "";

    /**
     * Volume of the playback ranging from 0.0 to 1.0, with the latter as default
     */
    private float volume = 1.0f;

    public PlayStatement(String audioFileName, String text)
    {
        this.audioFileName = audioFileName;
        this.text = text;
    }

    public PlayStatement(String audioFileName, String text, float volume)
    {
        this.audioFileName = audioFileName;
        this.text = text;
        this.volume = volume;
    }

    /**
     * @return {@link PlayStatement#audioFileName}
     */
    public String getAudioFileName()
    {
        return audioFileName;
    }

    /**
     * @return {@link PlayStatement#text}
     */
    public String getText()
    {
        return text;
    }

    /**
     * @return {@link PlayStatement#volume}
     */
    public float getVolume()
    {
        return volume;
    }

    @Override
    public String toString()
    {
        return "PlayStatement{" +
                "audioFileName='" + audioFileName + '\'' +
                ", text='" + text + '\'' +
                ", volume=" + volume +
                '}';
    }
}
