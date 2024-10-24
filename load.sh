#!/bin/sh

cp com.srednal.spotwx.plist /Library/LaunchDaemons/
launchctl load -w /Library/LaunchDaemons/com.srednal.spotwx.plist

