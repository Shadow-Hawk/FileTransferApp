# File transfer application
Authors: Wu Wei (2012220748), Stephan Kölker (2015403217)

This application is split into server and client. The server serves files located in a pre-defined directory to the client who asks for the files. The client stores them in a pre-defined directory.

# How to use it
Start as a client: java -jar FileTransferApp.jar client 
Start as a server: java -jar FileTransferApp.jar server

Before starting the application, copy the configuration file from /resources to this directory and apply its contents to your situation. You should especially adapt SrcHost (Server IP), DestHost (Client IP), SrcDir and DestDir to your current needs.
