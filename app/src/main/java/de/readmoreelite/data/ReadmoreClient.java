package de.readmoreelite.data;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

public class ReadmoreClient {
	
	private static HttpClient client;
	
	public static HttpClient getClient() {
		if(client == null) {
			PoolingHttpClientConnectionManager manager = new PoolingHttpClientConnectionManager();
			client = HttpClients.custom()
								.setConnectionManager(manager).build();
								
		}
		
		return client;
	}

}
