# MyPlayer

------

This is a program I developed based on [ExoPlayer](https://github.com/google/ExoPlayer).
And I change the UI of the ExoPlayer by using some opensource library. like [Material Design Library](https://github.com/navasmdc/MaterialDesignLibrary),
[material-design-icons](https://github.com/google/material-design-icons).

##Function
-Support the video format that Android System support.
-Scanning the local video and play it.
-play online stream video.Support DASH protocol.

##DASH
Here is some [specification](https://en.wikipedia.org/wiki/Dash)
totally, we can do less work if we use DASH as the media stream transfer protocol。
##How to test it
I use this program to learn about DASH protocol, So i need Tomcat and video samples to test it.

[Sample Video](http://dash-mse-test.appspot.com/media.html)
[Tomcat](http://tomcat.apache.org/)

I used a real android phone and computer to do the test.Both devices are in the same Local area network。

Here is the step:
1.download one video from the SAMPLE link, for example Car(including all `.mp4`,`mpd` files));
2.deploy all the file you download to tomcat server( I just place the resource into a self-defined folder:`Tomcat7\webapps\source`, then restart tomcat).
3.use `cmd`, type `ipconfig` to get your computer's ip address.
4.On phone, input the address of `.mpd` file, and play it. for example: `http://192.168.1.100:8080/source/xxx.mpd`

>if your phone could access the sample video, you could just input the link of the `.mpd` file.

