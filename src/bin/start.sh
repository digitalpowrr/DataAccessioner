#!/bin/bash
cd "$(dirname "$0")" || exit

if [ "X${FITS_HOME}" == "X" ]
then
    FITS_HOME="$(dirname "$0")"
    export FITS_HOME
fi

FITS_HOME="${FITS_HOME}" java -Dlog4j.configurationFile=da-log-config.xml -Xmx1024m -jar dataaccessioner.jar
