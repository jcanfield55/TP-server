package web;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPDigestAuthFilter;
import com.sun.jersey.core.util.Base64;


//import org.apache.http.HttpResponse;
//import org.apache.http.client.HttpClient;
//import org.apache.http.client.methods.HttpPost;
//import org.apache.http.impl.client.DefaultHttpClient;



public class TestRestService {
	public static void main(String[] args) {
		//ClientConfig config = new DefaultClientConfig();
		//Client client = Client.create(config);
		//WebResource service = client.resource(getBaseURI());
		//// Get XML
		//System.out.println(service.path("user").accept(MediaType.APPLICATION_XML).get(String.class));
		//
		//System.out.println(service.path("user").accept(MediaType.APPLICATION_JSON).get(String.class));	
		//MediaType.APPLICATION_JSON
		testPassword();
		//		testJerseyClient();

	}
	private static void testJerseyClient() {
		Client client = Client.create();
		client.addFilter(new HTTPDigestAuthFilter("admin", "password"));
		WebResource webResource = client.resource("http://localhost:8080/TPServer/ws/test/test");

		String res = webResource.get(String.class);
		System.out.println(res);
		//		MultivaluedMap queryParams = new MultivaluedMapImpl();
		//		queryParams.add("param1", "val1");
		//		queryParams.add("param2", "val2");
		//		String s = webResource.queryParams(queryParams).get(String.class);

	}
	private static void testPassword() {
		try {
			String created=  "2012-06-06T09:53:24Z";
			String nonce = "hellohello";
			String password="password";
			MessageDigest sha1;	

			SimpleDateFormat dateFormat =  new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
			dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
			sha1 = MessageDigest.getInstance("MD5");
			created = dateFormat.format(new Date());
			sha1.reset();
			//			byte[] digest = sha1.digest((nonce + created + password).getBytes());
			byte[] digest = sha1.digest((password).getBytes());
			//			byte[] digest = sha1.digest(("admin:OTP API:password").getBytes());
			byte[] base64Digest = Base64.encode(digest);
			String strDigest = new String(base64Digest);
			testService(strDigest,created,nonce);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

	}
	public static void testService(String strDigest, String created, String nonce)  {
		/*	try {
			DefaultHttpClient client = new DefaultHttpClient();
			HttpGet get = new HttpGet("http://localhost:8080/TPServer/ws/test/test");
			get.setHeader("Authorization","WSSE profile=\"UsernameToken\"");
			get.setHeader("X-WSSE","UsernameToken Username=admin, PasswordDigest=password");
			//			get.setHeader("Set-Cookie","JSESSIONID=94E33EE41DD6877D98314D14EEBD2AA8");
			HttpResponse response = client.execute(get);
			Header[] headers  =  response.getAllHeaders();
			for (int i = 0; i < headers.length; i++) {
				Header header = headers[i];
				System.out.println(header.getName()+":"+header.getValue());
			}

			BufferedReader rd = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent()));
			String line = "";
			while ((line = rd.readLine()) != null) {
				System.out.println(line);
			}		 

		} catch (Exception e) {
			e.printStackTrace();
		}*/
	}


}