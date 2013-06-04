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

Changes for adding HTTPS and SSL support 
===============================

Changes to add security through Apache Shiro

1. Add dependencies to the pom.xml file

	<dependency>
		<groupId>org.apache.shiro</groupId>
		<artifactId>shiro-core</artifactId>
		<version>1.2.1</version>
	</dependency>
	<dependency>
		<groupId>org.apache.shiro</groupId>
		<artifactId>shiro-web</artifactId>
		<version>1.2.1</version>
	</dependency>
	
2. Create a shiro.ini file for configuring shiro and add it to the classpath. (Example of shiro.ini file):

		[main]
		
		# name of request parameter with username; if not present filter assumes 'username'
		authc.usernameParam = user
		# name of request parameter with password; if not present filter assumes 'password'
		authc.passwordParam = pass
		authc.failureKeyAttribute = shiroLoginFailure
		
		[users]
		admin = admin, ROLE_ADMIN
		
		[roles]
		ROLE_ADMIN = *
		 
		 
		[urls]
		# enable authc filter for all application pages
		/** = ssl[8443],authcBasic
	
3. Add the following to the web.xml file

	<listener>
	    <listener-class>org.apache.shiro.web.env.EnvironmentLoaderListener</listener-class>
	</listener>
	
	<filter>
	    <filter-name>ShiroFilter</filter-name>
	    <filter-class>org.apache.shiro.web.servlet.ShiroFilter</filter-class>
	</filter>
	
	<filter-mapping>
	    <filter-name>ShiroFilter</filter-name>
	    <url-pattern>/*</url-pattern>
	    <dispatcher>REQUEST</dispatcher> 
	    <dispatcher>FORWARD</dispatcher> 
	    <dispatcher>INCLUDE</dispatcher> 
	    <dispatcher>ERROR</dispatcher>
	</filter-mapping>
	
4. Create a keystore (or use an existing keystore) using the following command and answer the questions:

	keytool -genkey -keyalg RSA -alias jetty -keystore keystore -storepass password -validity 360 -keysize 2048
	
5. Move the keystore to src/main/resources (or change the filepath below in the jetty-config.xml):
	
5. Create a jetty-config.xml file to add additional connectors to jetty. It should look like this:

		<?xml version="1.0"?>
		
		<Configure id="Server" class="org.eclipse.jetty.server.Server">
		
		<Call name="addConnector">
		      <Arg>
		          <New class="org.eclipse.jetty.server.nio.SelectChannelConnector">
		            <Set name="port">8080</Set>
		            <Set name="maxIdleTime">30000</Set>
		          </New>
		      </Arg>
		    </Call>
		<Call name="addConnector">
		    <Arg>
		      <New class="org.eclipse.jetty.server.ssl.SslSelectChannelConnector">
		        <Set name="Port">8443</Set>
		        <Set name="maxIdleTime">60000</Set>
		        <Set name="keystore">src/main/resources/keystore</Set>
		        <Set name="password">password</Set>
		        <Set name="keyPassword">password</Set>
		      </New>
		    </Arg>
		  </Call>
		  
		</Configure>
 
6. Chnage the run.sh bat file to use the config file when launching jetty:

	java -jar jetty-runner-7.6.8.v20121106.jar --config jetty-config.xml target/jettycamelwebsocketredis.war 
	
7. Secure the camel route websocket by adding the following code above the "from" line in the configure method( make sure to change the keystore path ):

		WebsocketComponent wc = getContext().getComponent("websocket", WebsocketComponent.class);
		KeyStoreParameters ksp = new KeyStoreParameters();
		//TODO change keystore path
		ksp.setResource("C:\\Users\\GENE\\git\\jettycamelwebsocketredis\\src\\main\\resources\\keystore");  //change the keystore path here
		ksp.setPassword("password");
		KeyManagersParameters kmp = new KeyManagersParameters();
		kmp.setKeyStore(ksp);
		kmp.setKeyPassword("password");
		TrustManagersParameters tmp = new TrustManagersParameters();
		tmp.setKeyStore(ksp);
		SSLContextParameters scp = new SSLContextParameters();
		scp.setKeyManagers(kmp);
		scp.setTrustManagers(tmp);
		wc.setSslContextParameters(scp);

8. Change the websocket address in the index.html file from ws:// to wss://

9. Compile and run via the run.sh script

	
