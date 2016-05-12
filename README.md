# Angular-Universal with Java backend using J2V8
This repository contains a simple (and very initial) example of using angular-universal with java backend using J2V8.

##Supported:
- Rendring using angular universal from java using J2V8
- Serving both the application and other rest endpoint from java using sparkjava
- Basic live-reload support for the universal server build 

##Missing:
- Fetch J2V8 from maven central
- Support other platforms
- Extracting internal implementation details from the angular-universal `server.js` and expose a proper API instead.
- Support for rendering multiple requests at once (currently the render is done one request at a time - this can be easly fixed)
- Performance tests 
- Etc..

##Requirements
- x64 Linux (tested on ubuntu 16.04)
- Java 8
- Maven
 
##Running Instructions
1. Clone the repository
2. Install node dependencies (`npm install`)
3. Build the java server(`mvn clean package`)
4. Build&Watch angular-universal + angular client side code (`npm run watch`)
5. Execute the java server (`mvn -e exec:java -Dexec.mainClass="hello.ngu.j2v8.Server"`)
6. Open your browser on `http://localhost:3000/app`

##Links
[Angular-Universal](https://github.com/angular/universal)
[J2V8](https://github.com/eclipsesource/J2V8)
[Spark-Java](http://sparkjava.com/)

###Thanks and credits
Special thanks to @irbull for the great J2V8 library and he's support.
