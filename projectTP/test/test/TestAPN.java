package test;

import java.util.ArrayList;
import java.util.List;

import com.nimbler.tp.service.APNService;
import com.nimbler.tp.service.APNService.APN_CERT_TYPE;
import com.nimbler.tp.service.LoggingService;

public class TestAPN {
	public static void main(String[] args) {
		test();
		//		testMatch();
		//System.out.println(lstStrings.get(1));
	}

	private static void testMatch() {
		List<Integer> lst1 = new ArrayList<Integer>();
		List<Integer> lst2 = new ArrayList<Integer>();

		lst1.add(10);lst2.add(12);
		lst1.add(15);lst2.add(15);
		lst1.add(120);

	}

	private static void test() {
		LoggingService loggingService =  new LoggingService();
		loggingService.init();
		APNService service = new APNService();
		service.setKEYSTORE_P12_FILE("conf/cert/certificateForProduction.p12");
		service.setPassword("password@123");
		service.setLoggerName("console");
		service.setPoolSize(2);
		service.setCertType(APN_CERT_TYPE.PRODUCTION.ordinal());
		service.setLogger(loggingService);
		service.init();
		//		service.push("26d906c5c273446d5f40d2c173ddd3f6869b2666b1c7afd5173d69b6629def70", "Caltrain urgent notification: abc",9,null);
		service.push("b469d6c15483f6eec317149274310d89617dbf1386e78ac6624409d29d3de36b", "test production",7,null);
		//		Map map = service.getService().getInactiveDevices();
		//		System.out.println(map);
		System.out.println("done...");
	}
}