# Distributed Stopwatch
### Control your Stopwatch from any other machine on LAN using Java RMI


<img src="https://github.com/rajtilakls2510/DistributedStopwatch/blob/master/diagrams/Demo%20Image.png?raw=true">

#### Description
How cool will be to control one machine from another when they are on LAN? 
This Application tries to simulate that using a Stopwatch. 
This Application let's you remotely control another stopwatch present on another 
computer on the LAN (Of course, you can control your own stopwatch). Since, this application is made 
using Java, it can run on any machine running the JVM.

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

<img src="https://github.com/rajtilakls2510/DistributedStopwatch/blob/master/diagrams/Just%20Started1.png?raw=true">

- To see the Stopwatches present on another machines on LAN, we need to have an Index Server in at least anyone of the 
  machines on the LAN. Start the Index Server on any machine using the following code after compiling the code using the 
  compile command given above:
  
`java main.Indexer`

- Once you start the Index Server, you will get the IP of the server in the terminal. You can use this IP to 
  register any Application to this server.
  
- Go back to the Application and type the Index Server IP in the field and hit `Select` button. 
If the registration was successful, all the Applications registered to that server will automatically appear in the 
  Remote Stopwatches section. But if this is the only Application registered to the Index Server, the Remote Stopwatches 
  field will be blank as mine.
  
<img src="https://github.com/rajtilakls2510/DistributedStopwatch/blob/master/diagrams/Just%20Bound.png?raw=true">

- Follow the same process to start the other applications in other machines. But this time you don't need to start any 
  Index Server as one is already running. Just register your application to the Index Server and it will give you the stopwatches 
  of all applications present on the network registered to that Index Server.
  
<img src="https://github.com/rajtilakls2510/DistributedStopwatch/blob/master/diagrams/Demo%20Image.png?raw=true">


