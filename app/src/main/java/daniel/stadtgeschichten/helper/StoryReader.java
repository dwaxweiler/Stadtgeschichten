package daniel.stadtgeschichten.helper;

import android.content.Context;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;

import daniel.stadtgeschichten.model.Story;
import daniel.stadtgeschichten.parser.StoryXmlParser;

/**
 * This class helps reading the different stories.
 */
public class StoryReader
{
    /**
     * Name of the subfolder of the assets folder that contains the stories
     */
    public static final String STORIES_FOLDER = "stories";

    /**
     * Application's context
     */
    private Context context;

    /**
     * List of stories
     */
    private ArrayList<Story> stories = new ArrayList<>();

    /**
     * Initialize this class by parsing the the XML files.
     * @param context application's context
     * @throws IOException
     * @throws XmlPullParserException
     */
    public StoryReader(Context context) throws IOException, XmlPullParserException
    {
        this.context = context;

        // Parse the XML files.
        ArrayList<String> folders = getAssetsStoriesSubFolders();
        for (int i = 0; i < folders.size(); i++)
            stories.add(StoryXmlParser.parse(context.getAssets().open(STORIES_FOLDER + "/"
                    + folders.get(i) + "/" + getFirstXMLFileName(folders.get(i))), folders.get(i)));
    }

    /**
     * Get a list of the sub folders of {@link StoryReader#STORIES_FOLDER} in the assets containing
     * at least one file or sub folder.
     * @return list of folder names
     * @throws IOException
     */
    private ArrayList<String> getAssetsStoriesSubFolders() throws IOException
    {
        ArrayList<String> folders = new ArrayList<>();
        String[] assets = context.getAssets().list(STORIES_FOLDER);
        for (String asset : assets)
            if (context.getAssets().list(STORIES_FOLDER + "/" + asset).length > 0)
                folders.add(asset);
        return folders;
    }

    /**
     * Get the first encountered XML file among all files in a folder.
     * @param folder sub folder of {@link StoryReader#STORIES_FOLDER} that contains at least one XML
     *               file
     * @return XML file, null if no one is present
     */
    private String getFirstXMLFileName(String folder) throws IOException
    {
        String[] children = context.getAssets().list(STORIES_FOLDER + "/" + folder);
        for (String child : children)
            if (child.endsWith(".xml"))
                return child;
        return null;
    }

    /**
     * Get the stories.
     * @return list of stories
     */
    public ArrayList<Story> getStories()
    {
        return stories;
    }

    /**
     * Get the stories' title.
     * @return list of stories' title, which is empty if there are no stories
     */
    public String[] getStoryTitles()
    {
        String[] titles = new String[stories.size()];
        for (int i = 0; i < stories.size(); i++)
            titles[i] = stories.get(i).getTitle();
        return titles;
    }

    /**
     * Get the story with the given title.
     * @param title story's title
     * @return story if found, null otherwise
     */
    public Story getStory(String title)
    {
        for (int i = 0; i < stories.size(); i++)
            if (stories.get(i).getTitle().equals(title))
                return stories.get(i);
        return null;
    }
}