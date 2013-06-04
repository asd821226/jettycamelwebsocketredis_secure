Simple demo that subscribes to a redis channel and puts the output to a
websocket

IMPORTANT: 
=========

You will need to make changes to the Camel route in order to get this to work. 

In src/main/java/demo/websocket/RedisSubscriberRoute.java

In this java line from("spring-redis://localhost:6379?command=SUBSCRIBE&channels=mychannel&serializer=#redisserializer")
       1.  change localhost to the IP of the Redis server 
       2.  change mychannel to name of the redischannel 
         
In src/main/webapp/index.html change localhost to the IP of the machine where
Jetty is running 

To complile 
===================

1. $mvn clean install
2. If everything goes fine you should see 
   target/jettycamelwebsocketredis.war file 


To start the server 
===================
1. To start the server use run.sh script (it just invokes the war file with the
   jettyrun jar included with the project) 

If the server starts properly then you should something like the following: 

2013-05-31 15:40:32.372:INFO:/:Initializing Spring root WebApplicationContext
[                          main] SpringCamelContext             INFO  Apache Camel 2.11.0 (CamelContext: camel-1) is starting
[                          main] SpringCamelContext             INFO  Tracing is enabled on CamelContext: camel-1
[                          main] ManagementStrategyFactory      INFO  JMX enabled.
[                          main] DefaultTypeConverter           INFO  Loaded 176 type converters
[                          main] WebsocketComponent             INFO  Jetty Server starting on host: 0.0.0.0:9292
[                          main] Server                         INFO  jetty-7.x.y-SNAPSHOT
[                          main] ContextHandler                 INFO  started o.e.j.s.ServletContextHandler{/,null}
[                          main] AbstractConnector              INFO  Started SelectChannelConnector@0.0.0.0:9292
[                          main] SpringCamelContext             INFO  Route: route1 started and consuming from: Endpoint[spring-redis://localhost:6379?channels=mychannel&command=SUBSCRIBE&serializer=%23redisserializer]
[                          main] ultManagementLifecycleStrategy INFO  Load performance statistics enabled.
[                          main] SpringCamelContext             INFO  Total 1 routes, of which 1 is started.
[                          main] SpringCamelContext             INFO  Apache Camel 2.11.0 (CamelContext: camel-1) started in 5.584 seconds
2013-05-31 15:40:39.314:INFO:oejsh.ContextHandler:started o.e.j.w.WebAppContext{/,file:/private/tmp/jettycamelwebsocketredis/target/jettycamelwebsocketredis/},file:/private/tmp/jettycamelwebsocketredis/target/jettycamelwebsocketredis.war


To see the output on the webpage 
================================

Try 
http://<ip of the jettyserver>:8080/index.html 

You should see a stream of tweets that are being pushed into the Redis channel
on the browser 


