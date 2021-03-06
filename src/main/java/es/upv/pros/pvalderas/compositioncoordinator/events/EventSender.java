package es.upv.pros.pvalderas.compositioncoordinator.events;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.impl.el.FixedValue;
import org.springframework.stereotype.Component;
import org.json.JSONException;
import org.json.JSONObject;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

@Component
public class EventSender implements JavaDelegate {
	
	FixedValue message;
	FixedValue microservice;

	@Override
	public void execute(DelegateExecution execution) {
		if(EventManager.getBrokerType().equals(EventManager.RABBITMQ)){
			try {
				ConnectionFactory factory = new ConnectionFactory();
				factory.setHost(EventManager.getHost());
				factory.setPort(Integer.parseInt(EventManager.getPort()));
				Connection connection;
			
				connection = factory.newConnection();
				Channel channel = connection.createChannel();
				channel.exchangeDeclare(EventManager.getRABBITMQ_EXCHANGE(), BuiltinExchangeType.TOPIC);
				
				 
				String composition=message.getExpressionText().substring(0,message.getExpressionText().indexOf("_"));
				String client=Clients.currentClient.get(composition.toLowerCase()); 
				String topic=microservice.getExpressionText().toLowerCase()+"."+composition.toLowerCase()+"."+client;
				
				try {
					JSONObject messageJSON = new JSONObject();
					messageJSON.put("message",message.getExpressionText());
					messageJSON.put("client",client);
					channel.basicPublish(EventManager.getRABBITMQ_EXCHANGE(), topic, null, messageJSON.toString().getBytes());
				} catch (JSONException e) {
					e.printStackTrace();
				}

				channel.close();
				connection.close();
				
				System.out.println("Sent Message: "+ message.getExpressionText());
				
			} catch (IOException | TimeoutException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}
}
