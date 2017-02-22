# Realm Pop Demo

<span style="color:red">This demo is fully functional, but rather thrown together quickly for demo.  The code will be cleaned up soon, including using async tx.</span>

Realm Pop is a simple game app designed to show off the collaborative features of the [Realm Mobile Platform](https://realm.io/news/introducing-realm-mobile-platform/).

Any number of users may join and play in any given moment.

This version is the Android version.

## Installation Instructions

1. [Download the Realm Mobile Platform](https://realm.io/docs/realm-mobile-platform/get-started/) Developer Edition.
2. Run a local instance of the Realm Mobile Platform.
3. Create a `default@realm` user with the password `password`.
4. Open the Project level `build.gradle` file with Android Studio, build the app, and deploy it to an Android device.
5. When the app starts you will be automatically be logged in as default@realm and be able to start playing. The Realm Object server address you enter can be local or it can be an instance running on any of our other supported Linux platforms which may also be downloaded from [Realm](https://realm.io). In either case you should ensure your firewall allows access to ports 9080 and 27800 as these are needed by the application in order to communicate wth the Realm Object Server.

#### Running against a local ROS

If you build the project through Android studio, the app you build and launch will automatically point to an instance running on your LAN IP.  (The build will auto update based on your dynamic LAN IP i.e. 192.168.x.x, as part of the build process)  see the app/build.gradle script.

#### Running against a Digital Ocean ROS

If you want to build the version that runs against the cloud, you can just run `./gradlew clean build` from the Android directory of the project.

This will create 3 APKs at ./app/build/outputs/apk/
* `app-cloudRelease.apk` <-- This one points to our cloud container.  The IP is hard coded into the app/build.gradle file in a variable called `cloudHost`
* `app-debug.apk`
* `app-release.apk`

The release apks are signed and able to be distributed.

## Screenshots

Coming soons!
