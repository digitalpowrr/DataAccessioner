#!/bin/bash
cd "$(dirname "$0")" || exit

if [ "X${FITS_HOME}" == "X" ]
then
    FITS_HOME="$(dirname "$0")/fits"
    export FITS_HOME
fi

java -Dlog4j.configurationFile=da-log-config.xml -Xmx1024m -jar dataaccessioner.jar
