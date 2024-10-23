# SpotWx


## Command Line Args

Command line arguments are of the form `paramName=value`, (no spaces).

loginOnly | boolean | default false
: If true, just connect to GMail and exit. Use from the command line once to pop up a browser for auth and store security tokens in `securityDir`.

pollInterval | long | default 120
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
`/Users/dave/dev/spot-wx/` and java is `/opt/homebrew/opt/openjdk/bin/java`.
Fix these if that's not the case.

To install the service:
```
sudo cp ~/dev/spot-wx/com.srednal.spotwx.plist /Library/LaunchDaemons/
sudo launchctl load -w /Library/LaunchDaemons/com.srednal.spotwx.plist
```

To shut it down:
```
sudo launchctl unload -w /Library/LaunchDaemons/com.srednal.spotwx.plist
```

## Logs

Daily logs for the service are at `./log/spot-wx.%d{yyyy-MM-dd}.log`
(relative to current working directory).
Should keep logs for 15 days, the be purged.
System out and err from the launch daemon will be
`/Users/dave/dev/spot-wx/log/spotwx-stdout.log` and
`/Users/dave/dev/spot-wx/log/spotwx-stderr.log`.
These should normally be empty.