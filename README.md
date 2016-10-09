# Stadtgeschichten 
location-aware radio drama Android app

released under The MIT License, see LICENSE.txt file for more details

No stories and no app icons are included! When you create a story, put its folder under `Stadtgeschichten\app\src\main\assets\stories` (see below for more details). When you create an icon, put it with the name `icon.png` in different resolution folders under `Stadtgeschichten\app\src\main\res` (`drawable-ldpi`, `drawable-mdpi`, `drawable-hdpi`...)



## Abstract

As devices with an integrated GPS sensor like smartphones and tablets are constantly becoming more ubiquitous and as most devices support the playback of audio files, new ways of story telling may emerge.
Here, a prototype of a mobile application enabling authors to create audio guides that trigger the playback of audio fragments upon visitation of pre-defined spots is presented.



## Concept
An interactive audio book is created as GPS-based mobile application called `Stadtgeschichten`.
It offers outdoor audio stories whose fragments are played back at pre defined spots.
The app works off-line, i.e. without relying on an internet connection, once it has been downloaded with the stories.
Linear and branched stories are possible.
The complexity of branched stories may stay hidden to the listener if he only listens these stories once. 

The storyteller can guide the user using environmental sounds whose volume is increased when the user approaches the desired destination and decreased if he heads in the wrong direction.
Another possibility of guiding is the simple description of how to get to the destination.
The storyteller can also create a puzzle by letting the user search the next destination.

A few helping features are offered for the case the user gets lost.
If the listener has missed a detail, e.g. due to the noise of a passing by truck, he can look up the transcript on the device.
If the listener does not know where to go next, he can look the destination up using an external app like *Google Maps*.
This external app could also guide the user to this place.

This concept is open for every classical genre, including urban fantasy, detective novel and love story, but it may also induce the raise of completely new genres.
Stories can be based on real events or completely made up.
Since the narrative style is completely different, most already written stories cannot be used.

Four different target groups have been thought of.
Fans of audio books could delve into a completely new experience of listening.
Authors' fans could discover the places their star had in mind on their own.
Residents would experience anew places they pass everyday.
Tourists would discover new places in a different way.



## Stories
A story consists of different audio files and a describing XML file, which an extra schema definition has been created for.
Per story, there is one folder that contains the audio files and the XML file.
The name of the folder does not play any role.
The XML file has to have the file extension `xml`.


### DTD
With the provided `Story.dtd` file, you can check self-created XML files for validity.
You only need an XML editor that supports this check.
[editix](http://free.editix.com/) supports it for example.
Note that the `Story.dtd` file has to be located in the same folder as the XML file you want to check.


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



## App
The mobile application has been developed for Android devices due to the authors' experience with this platform and since it is widely used.
The app parses the selected story's XML file and executes its different instructions.

The localisation of the user's device is achieved with GPS.
The usage of Bluetooth beacons is renounced although their usage would increase the localisation accuracy.
However, not using them increases the flexibility and expandability and keeps the costs low.
The app can also be published and used around the world without having to distribute beacons.

One main challenge is finding a trade-off between GPS-based localisation accuracy and electricity consumption as the GPS sensor draws a lot of energy.
By registering a `LocationListener` in the `LocationManager` with a location update interval of 500 milliseconds, the accuracy was good enough, and the emptying of the battery was not too obvious.

As the device will stay in the user's pocket during usage, the user interface design is kept minimalistic and functional.
It consists of the following and fits on one screen:
* Dropdown story selector
* Space for the story's transcript
* Play / pause button
* Stop button to exit the story
* Help button to show the map
A notification is shown in the notification area when the app is running.


### Limitations
#### Localisation accuracy
The localisation accuracy works good for circles with a radius of at minimum 10 meters.
Humans typically walk 5 km/h fast and need for a distance of 20 meters, i.e. the minimal diameter of a circle, only 1.44 seconds.
If the localisation needs longer to get accurate enough to trigger the instructions within this circle, humans will not notice the circle and will not come back to this place so easily any more.

When the GPS signal is very bad, the accuracy could be decreased to trigger the instructions of the circles with the minimal radius.
This could happen exponentially fast.
When no scene is found at all, the accuracy would be decreased further and might even mistakenly trigger instructions of spots that are farther away.

#### Next scene on map
Unfortunately, the geographically nearest scene is shown on the helping map instead of the scene that would be next following the story.
A more deep understanding of the scenes still to visit would require a more sophisticated parser.

#### Guiding sounds
Another main challenge has been the navigation using environmental sounds with increasing or decreasing volume.
Unfortunately, guiding the listener this way did not work well enough in tests run in the wild.
First of all, it is difficult to understand why the volume of an environmental sound changes.
Then, it is also hard to form a mental model of the circle and being lead to the center using the constantly changing volume.
Finally, volume changes can often not be perceived.


### Known bugs
* The title of circles and the transcript of audio files are not shown when the screen is turned off.



## Future work
Future work could be done in a few areas.

The story design possibilities could be elaborated.
Background music could be added to make the atmosphere even more captivating.
Timers could be used to help the listener when he has not found a particular spot (in time).
More context information like the current time and weather could be integrated to make the playing more adaptive and surprising.

A web service for creation of stories using a map, markers and uploading audio files could be created to make it possible for everybody to create such stories.
The story could then be directly published to be found and downloaded within the app, distinguishable and sortable using attributes like duration, distance, linear or branched story, puzzle yes or no.

The limitations of the current version could also be tackled by a complete rethinking and rewriting of the exchange format and mobile application.
A few improvements ideas are provided beneath each limitation description above.
At the end or already during development, a user study could be run to test the experience.
