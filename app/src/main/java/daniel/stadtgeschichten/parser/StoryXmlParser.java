package daniel.stadtgeschichten.parser;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import daniel.stadtgeschichten.model.AbsOperator;
import daniel.stadtgeschichten.model.AbsStatement;
import daniel.stadtgeschichten.model.AssignmentStatement;
import daniel.stadtgeschichten.model.Circle;
import daniel.stadtgeschichten.model.EndStatement;
import daniel.stadtgeschichten.model.EqualityOperator;
import daniel.stadtgeschichten.model.IfStatement;
import daniel.stadtgeschichten.model.IncrementStatement;
import daniel.stadtgeschichten.model.PlayStatement;
import daniel.stadtgeschichten.model.Spot;
import daniel.stadtgeschichten.model.Story;

/**
 * This parser transforms the story described in an xml file based on our own format into an object.
 */
public class StoryXmlParser
{
    public static final String TAG_STORY = "story";
    public static final String TAG_STORY_ATTR_TITLE = "title";
    public static final String TAG_STORY_ATTR_INTRO_FILE = "introfile";
    public static final String TAG_STORY_ATTR_INTRO_TEXT = "introtext";
    public static final String TAG_INIT = "init";
    public static final String TAG_SPOT = "spot";
    public static final String TAG_SPOT_LATITUDE = "latitude";
    public static final String TAG_SPOT_LONGITUDE = "longitude";
    public static final String TAG_CIRCLE = "circle";
    public static final String TAG_CIRCLE_ATTR_RADIUS = "radius";
    public static final String TAG_CIRCLE_ATTR_TITLE = "title";
    public static final String TAG_ASSIGN = "assign";
    public static final String TAG_ASSIGN_ATTR_VARIABLE = "variable";
    public static final String TAG_ASSIGN_ATTR_VALUE = "value";
    public static final String TAG_PLAY = "play";
    public static final String TAG_PLAY_ATTR_FILE = "file";
    public static final String TAG_PLAY_ATTR_TEXT = "text";
    public static final String TAG_PLAY_ATTR_VOLUME = "volume";
    public static final String TAG_IF = "if";
    public static final String TAG_CONDITION = "condition";
    public static final String TAG_THEN = "then";
    public static final String TAG_ELSE = "else";
    public static final String TAG_EQUALS = "equals";
    public static final String TAG_EQUALS_ATTR_ELEMENT_1 = "element1";
    public static final String TAG_EQUALS_ATTR_ELEMENT_2 = "element2";
    public static final String TAG_INCREMENT = "increment";
    public static final String TAG_INCREMENT_ATTR_VARIABLE = "variable";
    public static final String TAG_INCREMENT_ATTR_VALUE = "value";
    public static final String TAG_END = "end";

    /**
     * Start the parser on the given input stream to get the story as object.
     * @param in input stream
     * @param folderName name of the folder that contains the to be parsed file
     * @return parsed story
     * @throws XmlPullParserException
     * @throws IOException
     */
    public static Story parse(InputStream in, String folderName)
            throws XmlPullParserException, IOException
    {
        try
        {
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(in, null);
            parser.nextTag();

            // Require <story> root start tag.
            parser.require(XmlPullParser.START_TAG, null, TAG_STORY);

            // Get attributes.
            String title = parser.getAttributeValue(null, TAG_STORY_ATTR_TITLE);
            String introAudioFileName = parser.getAttributeValue(null, TAG_STORY_ATTR_INTRO_FILE);
            String introRecord = parser.getAttributeValue(null, TAG_STORY_ATTR_INTRO_TEXT);

            // Get content.
            List<AssignmentStatement> initStatements = null;
            ArrayList<Spot> spots = new ArrayList<>();
            while(parser.next() != XmlPullParser.END_TAG)
            {
                if (parser.getEventType() != XmlPullParser.START_TAG)
                    continue;

                String name = parser.getName();
                switch (name)
                {
                    case TAG_INIT:
                        initStatements = readInit(parser);
                        break;
                    case TAG_SPOT:
                        spots.add(readSpot(parser));
                        break;
                    default:
                        throw new XmlPullParserException(
                                "There is an unknown tag beneath the story tag: " + name);
                }
            }

            // Require </story> root end tag.
            //parser.require(XmlPullParser.END_TAG, null, TAG_STORY); does not work

            AssignmentStatement[] initStatementsArray;
            if (initStatements == null)
                initStatementsArray = new AssignmentStatement[0];
            else
                initStatementsArray = initStatements.toArray(
                        new AssignmentStatement[initStatements.size()]);
            return new Story(title, folderName, introAudioFileName, introRecord,
                    spots.toArray(new Spot[spots.size()]), initStatementsArray);
        }
        finally
        {
            // Close the input stream in case of a thrown exception.
            in.close();
        }
    }

    /**
     * Read an init tag.
     * @param parser parser
     * @return contained statements
     * @throws XmlPullParserException
     * @throws IOException
     */
    private static List<AssignmentStatement> readInit(XmlPullParser parser)
            throws XmlPullParserException, IOException
    {
        ArrayList<AssignmentStatement> statements = new ArrayList<>();

        // Require opening tag.
        parser.require(XmlPullParser.START_TAG, null, TAG_INIT);

        // Get content.
        while(parser.next() != XmlPullParser.END_TAG)
        {
            if (parser.getEventType() != XmlPullParser.START_TAG)
                continue;

            String name = parser.getName();
            switch (name)
            {
                case TAG_ASSIGN:
                    statements.add(readAssignmentStatement(parser));
                    break;
                default:
                    throw new XmlPullParserException(
                            "There is an unknown tag beneath an init tag: " + name);
            }
        }

        // Require closing tag.
        parser.require(XmlPullParser.END_TAG, null, TAG_INIT);

        return statements;
    }

    /**
     * Read a spot tag.
     * @param parser parser
     * @return spot
     * @throws XmlPullParserException
     * @throws IOException
     */
    private static Spot readSpot(XmlPullParser parser) throws XmlPullParserException, IOException
    {
        // Require opening tag.
        parser.require(XmlPullParser.START_TAG, null, TAG_SPOT);

        // Get attributes.
        double latitude = Double.valueOf(parser.getAttributeValue(null, TAG_SPOT_LATITUDE));
        double longitude = Double.valueOf(parser.getAttributeValue(null, TAG_SPOT_LONGITUDE));

        // Get content.
        ArrayList<Circle> circles = new ArrayList<>();
        while(parser.next() != XmlPullParser.END_TAG)
        {
            if (parser.getEventType() != XmlPullParser.START_TAG)
                continue;

            String name = parser.getName();
            switch (name)
            {
                case TAG_CIRCLE:
                    circles.add(readCircle(parser));
                    break;
                default:
                    throw new XmlPullParserException("There is an unknown tag beneath a spot tag: "
                            + name);
            }
        }

        // Require closing tag.
        parser.require(XmlPullParser.END_TAG, null, TAG_SPOT);

        // Make every circle know it's spot.
        Spot spot = new Spot(latitude, longitude, circles.toArray(new Circle[circles.size()]));
        for (Circle circle : circles)
            circle.setSpot(spot);

        return spot;
    }

    /**
     * Read a the content of a tag.
     * @param parser parser
     * @param tag name of the tag
     * @return file name
     * @throws XmlPullParserException
     * @throws IOException
     */
    private static String readTagText(XmlPullParser parser, String tag)
            throws XmlPullParserException, IOException
    {
        // Require <tag>.
        parser.require(XmlPullParser.START_TAG, null, tag);

        // Get value.
        String name = readText(parser);

        // Require </tag>.
        parser.require(XmlPullParser.END_TAG, null, tag);

        return name;
    }

    /**
     * Extract the text value of a tag.
     * @param parser parser
     * @return text contained in tag
     * @throws XmlPullParserException
     * @throws IOException
     */
    private static String readText(XmlPullParser parser) throws XmlPullParserException, IOException
    {
        String text = "";
        if (parser.next() == XmlPullParser.TEXT)
        {
            text = parser.getText();
            parser.nextTag();
        }
        return text;
    }

    /**
     * Read an assignment statement's tag.
     * @param parser parser
     * @return assignment statement
     * @throws XmlPullParserException
     * @throws IOException
     */
    private static AssignmentStatement readAssignmentStatement(XmlPullParser parser)
            throws XmlPullParserException, IOException
    {
        // Require opening tag.
        parser.require(XmlPullParser.START_TAG, null, TAG_ASSIGN);

        // Get attributes.
        String variable = parser.getAttributeValue(null, TAG_ASSIGN_ATTR_VARIABLE);
        int value = Integer.valueOf(parser.getAttributeValue(null, TAG_ASSIGN_ATTR_VALUE));

        // Require closing tag.
        parser.next();
        parser.require(XmlPullParser.END_TAG, null, TAG_ASSIGN);

        return new AssignmentStatement(variable, value);
    }

    /**
     * Read an end statement's tag.
     * @param parser parser
     * @return end statement
     * @throws XmlPullParserException
     * @throws IOException
     */
    private static EndStatement readEndStatement(XmlPullParser parser)
            throws XmlPullParserException, IOException
    {
        // Require opening tag.
        parser.require(XmlPullParser.START_TAG, null, TAG_END);

        // Require closing tag.
        parser.next();
        parser.require(XmlPullParser.END_TAG, null, TAG_END);

        return new EndStatement();
    }

    /**
     * Read an increment statement's tag.
     * @param parser parser
     * @return increment statement
     * @throws XmlPullParserException
     * @throws IOException
     */
    private static IncrementStatement readIncrementStatement(XmlPullParser parser)
            throws XmlPullParserException, IOException
    {
        // Require opening tag.
        parser.require(XmlPullParser.START_TAG, null, TAG_INCREMENT);

        // Get attributes.
        String variable = parser.getAttributeValue(null, TAG_INCREMENT_ATTR_VARIABLE);
        String value = parser.getAttributeValue(null, TAG_INCREMENT_ATTR_VALUE);
        IncrementStatement incrementStatement;
        if (value == null)
            incrementStatement = new IncrementStatement(variable);
        else
            incrementStatement = new IncrementStatement(variable, Integer.valueOf(value));

        // Require closing tag.
        parser.next();
        parser.require(XmlPullParser.END_TAG, null, TAG_INCREMENT);

        return incrementStatement;
    }

    /**
     * Read a circle's tag.
     * @param parser parser
     * @return circle
     * @throws XmlPullParserException
     * @throws IOException
     */
    private static Circle readCircle(XmlPullParser parser)
            throws XmlPullParserException, IOException
    {
        // Require opening tag.
        parser.require(XmlPullParser.START_TAG, null, TAG_CIRCLE);

        // Get attributes.
        int radius = Integer.parseInt(parser.getAttributeValue(null, TAG_CIRCLE_ATTR_RADIUS));
        String title = parser.getAttributeValue(null, TAG_CIRCLE_ATTR_TITLE);

        // Get content.
        ArrayList<AbsStatement> statements = new ArrayList<>();
        while(parser.next() != XmlPullParser.END_TAG)
        {
            if (parser.getEventType() != XmlPullParser.START_TAG)
                continue;

            String name = parser.getName();
            switch (name)
            {
                case TAG_ASSIGN:
                    statements.add(readAssignmentStatement(parser));
                    break;
                case TAG_END:
                    statements.add(readEndStatement(parser));
                    break;
                case TAG_PLAY:
                    statements.add(readPlayStatement(parser));
                    break;
                case TAG_IF:
                    statements.add(readIfStatement(parser));
                    break;
                default:
                    throw new XmlPullParserException(
                            "There is an unknown tag beneath a circle tag: " + name);
            }
        }

        // Require closing tag.
        parser.require(XmlPullParser.END_TAG, null, TAG_CIRCLE);

        return new Circle(radius, title, statements.toArray(new AbsStatement[statements.size()]));
    }

    /**
     * Read an equality operator's tag.
     * @param parser parser
     * @return equality operator
     * @throws XmlPullParserException
     * @throws IOException
     */
    private static EqualityOperator readEqualityOperator(XmlPullParser parser)
            throws XmlPullParserException, IOException
    {
        // Require opening tag.
        parser.require(XmlPullParser.START_TAG, null, TAG_EQUALS);

        // Get attributes.
        String element1 = parser.getAttributeValue(null, TAG_EQUALS_ATTR_ELEMENT_1);
        String element2 = parser.getAttributeValue(null, TAG_EQUALS_ATTR_ELEMENT_2);

        // Require closing tag.
        parser.next();
        parser.require(XmlPullParser.END_TAG, null, TAG_EQUALS);

        return new EqualityOperator(element1, element2);
    }

    /**
     * Read an if statement's tag.
     * @param parser parser
     * @return if statement
     * @throws XmlPullParserException
     * @throws IOException
     */
    private static IfStatement readIfStatement(XmlPullParser parser)
            throws XmlPullParserException, IOException
    {
        // Require opening tag.
        parser.require(XmlPullParser.START_TAG, null, TAG_IF);

        // Get content.
        List<AbsOperator> conditions = null;
        List<AbsStatement> thenStatements = null;
        List<AbsStatement> elseStatements = null;
        parser.nextTag();
        if (parser.getName().equals(TAG_CONDITION))
            conditions = readConditions(parser);
        parser.nextTag();
        if (parser.getName().equals(TAG_THEN))
            thenStatements = readStatements(parser, TAG_THEN);
        parser.nextTag();
        if (parser.getName().equals(TAG_ELSE))
            elseStatements = readStatements(parser, TAG_ELSE);

        // Require closing tag.
        parser.require(XmlPullParser.END_TAG, null, TAG_IF);

        AbsOperator[] conditionsArray;
        if (conditions == null)
            conditionsArray = new AbsOperator[0];
        else
            conditionsArray = conditions.toArray(new AbsOperator[conditions.size()]);
        AbsStatement[] thenStatementsArray;
        if (thenStatements == null)
            thenStatementsArray = new AbsStatement[0];
        else
            thenStatementsArray = thenStatements.toArray(new AbsStatement[thenStatements.size()]);
        AbsStatement[] elseStatementsArray;
        if (elseStatements == null)
            elseStatementsArray = new AbsStatement[0];
        else
            elseStatementsArray = elseStatements.toArray(new AbsStatement[elseStatements.size()]);
        return new IfStatement(conditionsArray, thenStatementsArray, elseStatementsArray);
    }

    /**
     * Read a condition's tag containing conditions.
     * @param parser parser
     * @return list of operators
     * @throws XmlPullParserException
     * @throws IOException
     */
    private static ArrayList<AbsOperator> readConditions(XmlPullParser parser)
            throws XmlPullParserException, IOException
    {
        // Require opening tag.
        parser.require(XmlPullParser.START_TAG, null, TAG_CONDITION);

        // Get content.
        ArrayList<AbsOperator> operators = new ArrayList<>();
        while(parser.next() != XmlPullParser.END_TAG)
        {
            if (parser.getEventType() != XmlPullParser.START_TAG)
                continue;

            String name = parser.getName();
            switch (name)
            {
                case TAG_EQUALS:
                    operators.add(readEqualityOperator(parser));
                    break;
                default:
                    throw new XmlPullParserException(
                            "There is an unknown tag beneath a condition tag: " + name);
            }
        }

        // Require closing tag.
        parser.require(XmlPullParser.END_TAG, null, TAG_CONDITION);

        return operators;
    }

    /**
     * Read the statements contained in the given tag.
     * @param parser parser
     * @param tag name of the tag the statements are contained in
     * @return list of statements
     * @throws XmlPullParserException
     * @throws IOException
     */
    private static ArrayList<AbsStatement> readStatements(XmlPullParser parser, String tag)
            throws XmlPullParserException, IOException
    {
        // Require opening tag.
        parser.require(XmlPullParser.START_TAG, null, tag);

        // Get content.
        ArrayList<AbsStatement> statements = new ArrayList<>();
        while(parser.next() != XmlPullParser.END_TAG)
        {
            if (parser.getEventType() != XmlPullParser.START_TAG)
                continue;

            String name = parser.getName();
            switch (name)
            {
                case TAG_ASSIGN:
                    statements.add(readAssignmentStatement(parser));
                    break;
                case TAG_END:
                    statements.add(readEndStatement(parser));
                    break;
                case TAG_INCREMENT:
                    statements.add(readIncrementStatement(parser));
                    break;
                case TAG_PLAY:
                    statements.add(readPlayStatement(parser));
                    break;
                default:
                    throw new XmlPullParserException(
                            "There is an unknown tag beneath a " + tag + " tag: " + name);
            }
        }

        // Require closing tag.
        parser.require(XmlPullParser.END_TAG, null, tag);

        return statements;
    }

    /**
     * Read the play statement's tag.
     * @param parser parser
     * @return play statement
     * @throws XmlPullParserException
     * @throws IOException
     */
    private static PlayStatement readPlayStatement(XmlPullParser parser)
            throws XmlPullParserException, IOException
    {
        // Require opening tag.
        parser.require(XmlPullParser.START_TAG, null, TAG_PLAY);

        // Get attributes.
        String fileName = parser.getAttributeValue(null, TAG_PLAY_ATTR_FILE);
        String text = parser.getAttributeValue(null, TAG_PLAY_ATTR_TEXT);
        String volume = parser.getAttributeValue(null, TAG_PLAY_ATTR_VOLUME);
        PlayStatement playStatement;
        if (volume == null)
            playStatement = new PlayStatement(fileName, text);
        else
            playStatement = new PlayStatement(fileName, text, Float.valueOf(volume));

        // Require closing tag.
        parser.next();
        parser.require(XmlPullParser.END_TAG, null, TAG_PLAY);

        return playStatement;
    }
}
