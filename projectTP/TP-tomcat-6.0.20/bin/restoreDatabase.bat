@echo Stoping server..
CALL shutdown.bat
NETSH Diag Ping Loopback > nul
NETSH Diag Ping Loopback > nul
NETSH Diag Ping Loopback > nul
NETSH Diag Ping Loopback > nul
NETSH Diag Ping Loopback > nul
NETSH Diag Ping Loopback > nul
NETSH Diag Ping Loopback > nul
NETSH Diag Ping Loopback > nul
NETSH Diag Ping Loopback > nul
NETSH Diag Ping Loopback > nul

                         
@echo Restoring database..
cd..
java -classpath webapps/SFMany/WEB-INF/lib/*;webapps/SFMany/WEB-INF/lib/postgresql-8.2-504.jdbc3.jar;webapps/SFMany/WEB-INF/lib/jdom.jar; com.santafe.soa.server.engine.tools.configuration.DatabaseRestore

NETSH Diag Ping Loopback > nul
NETSH Diag Ping Loopback > nul
NETSH Diag Ping Loopback > nul
NETSH Diag Ping Loopback > nul
NETSH Diag Ping Loopback > nul
NETSH Diag Ping Loopback > nul

cd bin
@echo Starting server..
CALL startup.bat
exit
