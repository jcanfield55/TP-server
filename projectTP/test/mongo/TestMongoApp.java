package mongo;
import java.util.ArrayList;

import com.nimbler.tp.TPApplicationContext;
import com.nimbler.tp.TPApplicationContext.SPRING_BEANS;
import com.nimbler.tp.dbobject.EventLog;
import com.nimbler.tp.mongo.PersistanceService;
import com.nimbler.tp.startup.StartupManager;

public class TestMongoApp {

	public static void main(String[] args) {
		new StartupManager();
		PersistanceService service = (PersistanceService) TPApplicationContext.getInstance().getBean(SPRING_BEANS.PERSISTANCE_SERVICE.bean());
		System.out.println(service);
		ArrayList<String> list = new ArrayList<String>();
		list.add("100");
		list.add("200");

		//		Employee employee3 = new Employee("Nikil", "Patel", 23,null, company2);

		//		
		//		User user = new User();
		//		user.setDeviceId("device");
		//		user.setCreateTime(System.currentTimeMillis());


		//		GeoCode code = new GeoCode("rawAddrassFrom0", "rawAddrassTo0", "formattedAddrassFrom0", null);
		EventLog log = new EventLog();
		//Map map = new HashMap();
		//map.put("hello", "hello1");
		//map.put("hi", "hi1");
		//		log.setData(code);
		//		log.setType(EVENT_TYPE.GEO_CODE_FROM.ordinal());

		//		service.addObject(MONGO_TABLES.event_log.name(), log);
		//List<FeedBack> lst = service.getCollectionList(MONGO_TABLES.event_log.name(),AuditLog.class);
		System.out.println(log.getId());

		//		List<EventLog> lst1 = service.getCollectionList(MONGO_TABLES.event_log.name(), EventLog.class);		
		//		for (EventLog user2 : lst1) {
		//			System.out.println(user2);
		//		}

	}
}
