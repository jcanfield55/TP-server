package web;

import java.io.BufferedReader;
import java.io.InputStreamReader;

//import org.apache.http.HttpResponse;
//import org.apache.http.client.HttpClient;
//import org.apache.http.client.methods.HttpPost;
//import org.apache.http.impl.client.DefaultHttpClient;



public class TestRestService {
	public static void main(String[] args) {
		testService();
		//ClientConfig config = new DefaultClientConfig();
		//Client client = Client.create(config);
		//WebResource service = client.resource(getBaseURI());
		//// Get XML
		//System.out.println(service.path("user").accept(MediaType.APPLICATION_XML).get(String.class));
		//
		//System.out.println(service.path("user").accept(MediaType.APPLICATION_JSON).get(String.class));	
		//MediaType.APPLICATION_JSON

	}
	public static void testService()  {
		try {
		/*	HttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost("http://localhost:8080/OTPServer/ws/feedback/new");
			post.setHeader("Accept","Application/json");
			HttpResponse response = client.execute(post);

			BufferedReader rd = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent()));
			String line = "";
			while ((line = rd.readLine()) != null) {
				System.out.println(line);
			}*/

		} catch (Exception e) {
			e.printStackTrace();
		}
	}


}