#!/bin/bash
# Detect java
if type -p java > /dev/null; then
    _java=java
elif [[ -n "$JAVA_HOME" ]] && [[ -x "$JAVA_HOME/bin/java" ]];  then
    _java="$JAVA_HOME/bin/java"
else
    echo "Java not found. Please install Java 17+"
    exit 1
fi

"$_java" -jar "$(dirname "$0")/target/mdm-cli-1.0-SNAPSHOT.jar" "$@"
