# SpotWx

Queries a gmail account for new messages from a SpotX device.
Replies with a 2-hour and 2-day weather forecast for the device's location.

Location is determined from the email headers
`X-SPOT-Latitude` and `X-SPOT-Longitude`.

Alternately, if the message body contains only
a longitude and latitude (as in "40.31043, -105.64559"),
then this longitude and latitude will be used (but headers
take precedence). This is useful for testing or planning: copy the
coordinates from google maps, gaiagps, etc and paste them into an email.

Weather is queried from [open-meteo.com](https://open-meteo.com/en/docs),
and summarized to be less than the SpotX 140 character text limit.

The hourly report will look something like:
```
12:00: Clear | 40F | 0.0" (20%) | W@19mph
13:00: Pt Cloudy | 40F | 0.0" (19%) | W@15mph
```

The daily report will look something like:
```
11/26: Snow | 40/24F | 2.0" (53%) | W@27mph
11/27: Hvy Snow | 31/17F | 4.7" (54%) | W@17mph
```
(That is: On the 26<sup>th</sup>, the forecast is for Snow with a high of 40 and low of 24ºF.
There's a 53% chance of 2". Winds from the W at 27 mph.)

Two reply emails back to the device (sender) are sent containing the weather summaries,
one with two hours, and one with two days
(they are too long for a single 140 character message).

To be run in the background (i.e. from `/Library/LaunchDaemons/`).
Default polling for new email is every 2 minutes.

## Command Line Args

Command line arguments are of the form `paramName=value`, (no spaces).

loginOnly | boolean | default false
: If true, just connect to GMail and exit. Use from the command line once to pop up a browser for auth and store security tokens in `securityDir`.

pollInterval | long (seconds) | default 120
: Frequency to poll GMail for new mail, in seconds.
To avoid hammering the service if there's an error, first
poll delays for half this time.

securityDir | string (path) | default ./security 
: Where authorization file is stored after initial browser-based authorization.

credentialsFile | string (path) | default ./security/credentials.json
: OAuth2 credentials json file downloaded from google cloud.
See https://developers.google.com/gmail/api/quickstart/java.

## LaunchDaemon

The plist `com.srednal.spotwx.plist` assumes that the project is at
`/Users/dave/dev/spot-wx/` and java is `/usr/bin/java`.
Fix these if that's not the case.

To install the service:

```
sudo ./load/sh
```

To shut it down:
```
sudo ./unload.sh
```
To see if it's running, check the logs or run `jps`
and look for `spot-wx-1.0-all.jar`.

## Logs

Daily logs for the service are at `./log/spot-wx.%d{yyyy-MM-dd}.log`
(relative to current working directory).
Should keep logs for 15 days, the be purged.
System out and err from the launch daemon should be empty,
but in any case are sent to
`/Users/dave/dev/spot-wx/log/spotwx-stdout.log` and
`/Users/dave/dev/spot-wx/log/spotwx-stderr.log`.

## Build

### TL;DR
```
gradle build shadowJar
```

### Gradle Tasks

clean
: Deletes the build directory.

build
: Assembles and tests this project.

plutil
: Checks the plist format with `plutil`. 

run
: Runs it.

shadowJar
: Create a combined JAR of project and runtime dependencies.

runShadow
: Runs it using the shadow jar.
