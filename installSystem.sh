#!/bin/bash

# CHANGE THESE
app_package="org.miamplayer.autoairplanemode"
dir_app_name="AutoAirplaneMode"
MAIN_ACTIVITY="MainActivity"

ADB_EXE=/c/ADB2/adb
ADB_SH="$ADB_EXE shell" # see `Caveats` if using `adb su`

path_sysapp="/system/priv-app" # assuming the app is priviledged
apk_host="./app/build/outputs/apk/app-debug.apk"
apk_name=$dir_app_name".apk"
apk_target_dir="$path_sysapp/$dir_app_name"
apk_target_sys="$apk_target_dir/$apk_name"

# Delete previous APK
rm -f $apk_host

# Compile the APK: you can adapt this for production build, flavors, etc.
./gradlew assembleDebug || exit -1 # exit on failure

# Install APK: using adb root
$ADB_EXE root 2> /dev/null
$ADB_EXE remount # mount system
$ADB_EXE push $apk_host $apk_target_sys

# Give permissions
$ADB_SH "chmod 755 $apk_target_dir"
$ADB_SH "chmod 644 $apk_target_sys"

#Unmount system
$ADB_SH "mount -o remount,ro /"

# Stop the app
$ADB_EXE shell "am force-stop $app_package"

# Re execute the app
$ADB_EXE shell "am start -n \"$app_package/$app_package.$MAIN_ACTIVITY\" -a android.intent.action.MAIN -c android.intent.category.LAUNCHER"
