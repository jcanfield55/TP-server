package mongo;
import java.util.ArrayList;
import java.util.List;

import com.apprika.otp.TPApplicationContext;
import com.apprika.otp.TPApplicationContext.SPRING_BEANS;
import com.apprika.otp.dbobject.FeedBack;
import com.apprika.otp.dbobject.User;
import com.apprika.otp.mongo.PersistanceService;
import com.apprika.otp.startup.StartupManager;
import com.apprika.otp.util.TpConstants.MONGO_TABLES;

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
		User user = new User();
		user.setDeviceId("device");
		//		user.setCreateTime(System.currentTimeMillis());

		//		service.addObject(MONGO_TABLES.users.name(), user);
		List<FeedBack> lst = service.getCollectionList(MONGO_TABLES.feedback.name(),FeedBack.class);


		System.out.println(user.getId());
		//		List<User> lst = service.getCollectionList(MONGO_TABLES.users.name(), User.class);
		for (FeedBack user2 : lst) {
			System.out.println(user2);
		}

	}
}
