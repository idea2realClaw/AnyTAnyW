#!/bin/sh

# Gradle startup script for Unix

# Attempt to set APP_HOME
APP_HOME=$( cd "${APP_HOME:-./}" && pwd -P ) || exit

# Add default JVM options here. You can also use JAVA_OPTS and GRADLE_OPTS to pass JVM options to this script.
DEFAULT_JVM_OPTS=""

# Find java
if [ ! -z "$JAVA_HOME" ] ; then
    JAVA_EXE="$JAVA_HOME/bin/java"
elif [ ! -z "$JAVA_EXE" ] ; then
    JAVA_EXE="$JAVA_EXE"
else
    JAVA_EXE="java"
fi

if ! command -v "$JAVA_EXE" >/dev/null 2>&1 ; then
    echo "ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH."
    echo "Please set the JAVA_HOME variable in your environment to match the location of your Java installation."
    exit 1
fi

# Set GRADLE_HOME
GRADLE_HOME="$APP_HOME/gradle"

# Execute Gradle
exec "$GRADLE_HOME/bin/gradle" "$@"
