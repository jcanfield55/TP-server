package com.apprika.otp.mongo;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.document.mongodb.MongoOperations;
import org.springframework.data.document.mongodb.query.Criteria;
import org.springframework.data.document.mongodb.query.Query;
import org.springframework.data.document.mongodb.query.Update;

import com.apprika.otp.service.LoggingService;
/**
 * 
 * @author suresh
 *
 */
public class PersistanceService {
	Logger logger = LoggingService.getLoggingService(PersistanceService.class.getName());

	@Autowired
	MongoOperations mongoOpetation; 

	private PersistanceService(){

	}
	/**
	 * add single row object
	 * @param collectionName
	 * @param object
	 */
	public void addObject(String collectionName, Object object){
		mongoOpetation.save(collectionName, object);
	}

	/**
	 * add multiple object for particular collection
	 * @param collectionName
	 * @param list
	 */
	public void addObjects(String collectionName , List<Object> list){
		mongoOpetation.insertList(collectionName, list);
	}

	/**
	 * return only one row data 
	 * @param collectionName
	 * @param column
	 * @param columnValue
	 * @param clazz
	 * @return
	 */
	public Object findOne(String collectionName, String column ,String columnValue, Class clazz){
		return mongoOpetation.findOne(collectionName, new Query(Criteria.where(column).is(columnValue)), clazz);
	}

	/**
	 * Returns all row which match according to search query 
	 * @param collectionName
	 * @param column
	 * @param columnValue
	 * @param clazz
	 * @return
	 */
	public List find(String collectionName, String column, String columnValue, Class clazz){
		Criteria criteria = Criteria.where(column).is(columnValue);
		Query query = new Query();
		query.addCriteria(criteria);
		return mongoOpetation.find(collectionName, query, clazz);
	}

	/**
	 * It returns all rows from collection
	 * @param collectionName
	 * @param clazz
	 * @return
	 */
	public List getCollectionList(String collectionName, Class clazz){
		return mongoOpetation.getCollection(collectionName, clazz);
	}

	/**
	 * Update
	 * @param collectionName
	 * @param column
	 * @param columnValue
	 * @param updataColumn
	 * @param updateValue
	 */
	public void updateSingleObject(String collectionName, String column, String columnValue, String updataColumn, String updateValue){
		Criteria criteria = Criteria.where(column).is(columnValue);
		Query query = new Query();
		query.addCriteria(criteria);

		Update update = Update.update(updataColumn, updateValue);
		//		operations.updateFirst(collectionName, new Query(Criteria.where(column).is(columnValue)), Update.update(updataColumn, toUpdat));
		mongoOpetation.updateMulti(collectionName, query, update);
	}

	/**
	 * Remove particular field.
	 * @param collectionName
	 * @param column
	 * @param columnValue
	 * @param clazz
	 */
	public void removeSingleData(String collectionName, String column , String columnValue, Class clazz){
		mongoOpetation.remove(collectionName, new Query(Criteria.where(column).is(columnValue)), clazz);
	}

	/**
	 * To drop collection
	 * @param collectionName
	 */
	public void deleteCollection(String collectionName){
		mongoOpetation.dropCollection(collectionName);
	}
	public MongoOperations getMongoOpetation() {
		return mongoOpetation;
	}
	public void setMongoOpetation(MongoOperations mongoOpetation) {
		this.mongoOpetation = mongoOpetation;
	}
}
