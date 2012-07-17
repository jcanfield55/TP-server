#!/bin/bash
echo 'Stoping server..'
sh shutdown.sh
cd ../
sleep 50
echo 'Restoring database'
currentDir=$(pwd)

java -classpath $currentDir/webapps/SFMany/WEB-INF/lib/*: com.santafe.soa.server.engine.tools.configuration.DatabaseRestore 

sleep 10

cd bin
echo 'Starting server..'

sh startup.sh
exit

