#!/bin/sh
cd "$(dirname "$0")"
java -Dlog4j.configurationFile=da-log-config.xml -Xmx1024m -jar dataaccessioner.jar
