package daniel.stadtgeschichten.model;

import java.util.Arrays;

/**
 * This class represents a full story.
 */
public class Story
{
    /**
     * Title of the story
     */
    private String title;

    /**
     * Name of the folder containing all the files
     */
    private String folderName;

    /**
     * Name of the audio file played as introduction to the story
     */
    private String introAudioFileName;

    /**
     * Written version of the audio file played as introduction to the story
     */
    private String introRecord;

    /**
     * Assignment statements that are executed at the beginning
     */
    private AssignmentStatement[] initStatements;

    /**
     * Spots of the story
     */
    private Spot[] spots;

    public Story(String title, String folderName, String introAudioFileName, String introRecord,
                 Spot[] spots, AssignmentStatement[] initStatements)
    {
        this.title = title;
        this.folderName = folderName;
        this.introAudioFileName = introAudioFileName;
        this.introRecord = introRecord;
        this.spots = spots;
        this.initStatements = initStatements;
    }

    /**
     * @return {@link Story#title}
     */
    public String getTitle()
    {
        return title;
    }

    /**
     * @return {@link Story#folderName}
     */
    public String getFolderName()
    {
        return folderName;
    }

    /**
     * @return {@link Story#introAudioFileName}
     */
    public String getIntroAudioFileName()
    {
        return introAudioFileName;
    }

    /**
     * @return {@link Story#introRecord}
     */
    public String getIntroRecord()
    {
        return introRecord;
    }

    /**
     * @return {@link Story#initStatements}
     */
    public AssignmentStatement[] getInitStatements()
    {
        return initStatements;
    }

    /**
     * @return {@link Story#spots}
     */
    public Spot[] getSpots()
    {
        return spots;
    }

    @Override
    public String toString()
    {
        return "Story{" +
                "title='" + title + '\'' +
                ", folderName='" + folderName + '\'' +
                ", introAudioFileName='" + introAudioFileName + '\'' +
                ", introRecord='" + introRecord + '\'' +
                ", initStatements=" + Arrays.toString(initStatements) +
                ", spots=" + Arrays.toString(spots) +
                '}';
    }
}