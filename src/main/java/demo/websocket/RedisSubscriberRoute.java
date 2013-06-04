package demo.websocket; 

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.websocket.WebsocketComponent;
import org.apache.camel.impl.JndiRegistry;
import org.apache.camel.spi.Registry;
import org.apache.camel.spring.spi.ApplicationContextRegistry;
import org.apache.camel.util.jsse.KeyManagersParameters;
import org.apache.camel.util.jsse.KeyStoreParameters;
import org.apache.camel.util.jsse.SSLContextParameters;
import org.apache.camel.util.jsse.TrustManagersParameters;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.StringRedisSerializer; 

public class RedisSubscriberRoute extends RouteBuilder{


	@Override
	public void configure() throws Exception {

		WebsocketComponent wc = getContext().getComponent("websocket", WebsocketComponent.class);
		KeyStoreParameters ksp = new KeyStoreParameters();
                //IMP: This Path to the keystore should be setup correctly
		ksp.setResource("/tmp/ssl/jettycamelwebsocketredis/src/main/resources/keystore");
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

		//TODO change localhost to the IP of the Redis server 
		//change mychannel to name of the redischannel 
		from("spring-redis://192.168.168.225:6379?command=SUBSCRIBE&channels=mychannel&serializer=#redisserializer") 
		.process(new Processor() {
			@Override
			public void process(Exchange exchange) throws Exception {
				String res = exchange.getIn().getBody().toString();
				System.out.println("************ " + res); 
				exchange.getOut().setBody(res); 
			}
		})
		.to("websocket://0.0.0.0:9292/tweetsfromstorm?sendToAll=true");
	}

}
