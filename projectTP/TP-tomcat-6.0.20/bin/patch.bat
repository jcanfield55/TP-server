@echo Stoping server..
CALL shutdown.bat
NETSH Diag Ping Loopback
NETSH Diag Ping Loopback
NETSH Diag Ping Loopback
NETSH Diag Ping Loopback

@echo Applying the patch file
java -classpath patch.jar PatchManagement c:\syslog\SFMany.zip C:\Runtimes\apache-tomcat-6.0.20\

NETSH Diag Ping Loopback
NETSH Diag Ping Loopback
NETSH Diag Ping Loopback
NETSH Diag Ping Loopback
NETSH Diag Ping Loopback
NETSH Diag Ping Loopback
NETSH Diag Ping Loopback

@echo Starting server..
CALL startup.bat