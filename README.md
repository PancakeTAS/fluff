# Fluff
Fluff is a non-stop music player for YouTube. 
Fluff will play music obtained from a list of YouTube links in a random order and allows you to *yeet* it from your database when you're getting sick of it.

## How do I install it?
1. Clone the repository
2. Install [Gradle](https://gradle.org/releases/) (recommended version: 8.5) and [Java 21](https://docs.aws.amazon.com/corretto/latest/corretto-21-ug/downloads-list.html)
3. Install and add to path [yt-dlp](https://github.com/yt-dlp/yt-dlp/releases) and [ffmpeg](https://github.com/BtbN/FFmpeg-Builds/releases/tag/latest)
4. Run `gradle shadowJar`
5. Run the jar created in `build/libs/`

## How do I use it?
Before you can run Fluff, you will need to create a fluff.txt in `C:\Users\<you>\Music` and fill each line with a YouTube link.
After that, you can run Fluff and it will play the music in a random order. If you want to select a different audio device, you can run the jar with --list-devices and --set-device accordingly.

Now you can double click the tray icon to close Fluff.

Use the media keys (next, previous, pause/play) to control the current track playing, louder/quieter keys to control the volume and the stop key to remove the current track from the database.
You can also press the search key (if you have one) to open the current track in your browser.

## How does it work?
Fluff uses yt-dlp to download up to twelve tracks at a time, encodes them using ffmpeg and then plays them using the Java Sound API. Pausing will become a feature too.. eventually
