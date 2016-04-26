# Project Phase 2

A simple file sharing service with a file management server, user management server, and a client.

## Build

1. Open the project folder in NetBeans
2. **R**un -> **B**uild Project (F11)
3. You can find the binary at .\dist\Phase2.jar

## Run

### Using Batch file (Recommanded)

1. Open the project folder in File Explorer
2. Execute start.cmd
3. You will see 3 cmd windows, represeanting 2 servers and client. After setting up servers, you can now log into the server using the client.

### From the command line

1. Open the project folder in cmd.exe
2. Execute cd .\dist\
3. Execute java -jar Phase2.jar, follow the instruction to start one of the server
4. Repeat step 3 to start the other server and the client

### Inside NetBeans

1. Open the project folder in NetBeans
2. On the left side, find Project panel and select Entry.java
3. Right click Entry.java and click Run File. You can also press Shift+F6 in editor to run Entry.java if you already opened it
4. You can find the output panel at the bottom of the NetBeans, follow the instruction to start one of the server
5. Repeat steps 3 and 4 to start the other server and the client
