cd .\Phase2
start "Group server" java -jar .\dist\Phase2.jar 0
start "File server" java -jar .\dist\Phase2.jar 1
start "Client" java -jar .\dist\Phase2.jar 2