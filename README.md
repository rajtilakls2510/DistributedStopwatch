# Distributed Stopwatch
### Control your Stopwatch from any other machine on LAN using Java RMI


<img src="https://github.com/rajtilakls2510/DistributedStopwatch/blob/master/diagrams/Demo%20Image.png?raw=true">

#### Description
How cool will be to control one machine from another when they are on LAN? 
This Application tries to simulate that using a Stopwatch. 
This Application let's you remotely control another stopwatch present on another 
computer on the LAN (Of course, you can control your own stopwatch).

### Technology Stack:
- Programming Language: JAVA 
- GUI design: Swing
- Networking: Java RMI

### How to use?
- First, we need a JDK to compile the code and run it. (Recommended: JDK 11 or greater)
- Navigate inside the project to compile the code using the terminal. (Path: "DistributedStopwatch/" )
- Run this line of code to compile the code

`javac ./main/ApplicationController.java ./main/Indexer.java`
  
- To Start the Application, run this line of code from the same directory from where the code was compiled

`java main.ApplicationController`

- Once the application starts, you will see your own stopwatch with your IP and ID.
It's just a normal stopwatch and you can very well control it. 


