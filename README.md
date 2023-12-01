# Fluff
Fluff is a non-stop music player for YouTube. 
Fluff will play music obtained from a list of YouTube links in a random order and allows you to *yeet* it from your database when you're getting sick of it.

## How do I install it?
1. Clone the repository
2. Install [Gradle](https://gradle.org/releases/) (recommended version: 8.5) and [Java 21](https://docs.aws.amazon.com/corretto/latest/corretto-21-ug/downloads-list.html)
3. Install and add to path [yt-dlp](https://github.com/yt-dlp/yt-dlp/releases), [ffplay](https://github.com/BtbN/FFmpeg-Builds/releases/tag/latest) and [SoundVolumeView](https://www.nirsoft.net/utils/sound_volume_view.html)
4. Run `gradle shadowJar`
5. Run the jar created in `build/libs/`

## How do I use it?
Before you can run Fluff, you will need to create a fluff.txt in `C:\Users\<you>\Music` and fill each line with a YouTube link.
After that, you can run Fluff and it will play the music in a random order.

Now you can double click the tray icon to close Fluff. 

Use the media keys (next, previous, pause/play) to control the current track playing, louder/quieter keys to control the volume and the stop key to remove the current track from the database.
You can also press the search key (if you have one) to open the current track in your browser.

## How does it work?
Fluff uses yt-dlp to download up to four tracks at a time and then play them using ffplay.
It also uses SoundVolumeView to control the volume of ffplay. In the future, ffplay will be replaced with a custom player that supports proper pausing...
