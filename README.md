# Stadtgeschichten 
location-aware radio drama Android app

## Stories
A story consists of different audio files and a describing XML file, which an extra schema definition has been created for.
Per story, there is one folder that contains the audio files and the XML file.
The name of the folder does not play any role.
The XML file has to have the file extension `xml`.

### XML format documentation
The XML file always starts with the following two lines:

    <?xml version="1.0" encoding="utf-8"?>
    <!DOCTYPE story SYSTEM "Story.dtd">

A `<story>` tag follows and encapsulates the complete story.
It has the following attributes:
* `title`: name of the story
* `introfile`: name of the audio file that should be played at the beginning to lead the listener to the starting point
* `introtext`: transcript of the audio file played at the beginning shown on the screen
An optional `<init>` tag follows and holds the initialisation of as many `<assign>` tags as you like.
This tag will be explained a few lines below.

Then, as many `<spot>` tags as you like follow.
One such tag describes one scene of the story and has the following attributes:
* latitude: latitude of the coordinates of the scene
* longitude: longitude of the coordinates of the scene
The author can query the coordinates of a scene using a web service like \emph{Google Maps}.

Every `<spot>` tag contains as many `<circle>` tags as you like.
The latter describe circles with the given coordinates as centre.
Every `<circle>` tag has the following attributes:
* `radius`: radius of the circle in meters; At least 10 meters should be used.
* `title`: optional title of the circle, which would be shown on the screen
Every `<circle>` tag contains has many `<assign>`, `<if>`, `<increment>` and `<play>` tags as you like.

The `<assign>` tag assigns an integral value to a variable and has the following attributes:
* `variable`: name of the variable that must not consist only numbers
* `value`: integral value

The `<increment>` tag raises a variable by the given integral value and has the following attributes:
* `variable`: name of the variable
* `value`: optional: When this attribute is not given, the variable will be risen by 1. When this attribute is given with an integral value, the variable will be risen by this value.

The `<if>` tag describes a conditional instruction and contains the following tags:
* A `<condition>` tag contains the conditions that must be fulfilled.
It contains as many `<equals>` tags as you like.
An `<equals>` tag checks the similarity and the following attributes:
    * `element1`: value or name of a variable
    * `element2`: value or name of a variable
* A `<then>` tag contains the instructions that are executed when the conditions are all fulfilled.
It can contain as many `<assign>` and `<increment>` tags as you like and one `<play>` and one `<end>` tag at maximum.
* An optional `<else>` tag contains the instructions that are executed when the conditions are not fulfilled.
It can contain as many `<assign>` and `<increment>` tags as you like and one `<play>` and one `<end>` tag at maximum.

The `<play>` tag triggers the playback of an audio file and has the following attributes:
* `file`: name of the audio file
* `text`: optional transcript of the audio file, which would be shown on the screen
* `volume`: optional playback volume with values from 0.0 to 1.0 in 0.1 steps

The `<end>` tag marks the end of the story.


## Known bugs
* The title of circles and the transcript of audio files are not shown when the screen is turned off.
