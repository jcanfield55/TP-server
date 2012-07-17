/**
 * 
 * Copyright (C) 2012 Apprika Systems   Pvt. Ltd. 
 * All rights reserved.
 *
 */
package com.nimbler.tp.mongo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.document.mongodb.MongoOperations;
import org.springframework.data.document.mongodb.query.Criteria;
import org.springframework.data.document.mongodb.query.Index;
import org.springframework.data.document.mongodb.query.Order;
import org.springframework.data.document.mongodb.query.Query;
import org.springframework.data.document.mongodb.query.Update;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.MongoException;
import com.mongodb.WriteResult;
import com.nimbler.tp.common.DBException;
import com.nimbler.tp.service.LoggingService;
import com.nimbler.tp.util.TpConstants;
/**
 * 
 * @author suresh
 *
 */
public class PersistenceService {

	@Autowired
	private LoggingService logger;

	@Autowired
	MongoOperations mongoOpetation;

	private String loggerName;

	/**
	 * add single row object
	 * @param collectionName
	 * @param object
	 */
	public void addObject(String collectionName, Object object)throws DBException {
		try {
			mongoOpetation.save(collectionName, object);
		}  catch (DataAccessResourceFailureException e) {
			logger.error(loggerName, e);
			throw new DBException(e.getMessage()); 
		} catch (MongoException e) {
			logger.error(loggerName, e);
			throw new DBException(e.getMessage());
		} catch (Exception e) {
			logger.error(loggerName, e);
			throw new DBException(e.getMessage());
		}
	}
	/**
	 * add multiple object for particular collection
	 * @param collectionName
	 * @param list
	 */
	public void addObjects(String collectionName , List<?> list) throws DBException {
		try {
			mongoOpetation.insertList(collectionName, list);
		}  catch (DataAccessResourceFailureException e) {
			logger.error(loggerName, e);
			throw new DBException(e.getMessage()); 
		} catch (MongoException e) {
			logger.error(loggerName, e);
			throw new DBException(e.getMessage());
		} catch (Exception e) {
			logger.error(loggerName, e);
			throw new DBException(e.getMessage());
		}
	}
	/**
	 * return only one row data 
	 * @param collectionName
	 * @param column
	 * @param columnValue
	 * @param clazz
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Object findOne(String collectionName, String column ,String columnValue, Class clazz) throws DBException {
		try {
			return mongoOpetation.findOne(collectionName, new Query(Criteria.where(column).is(columnValue)), clazz);
		}  catch (DataAccessResourceFailureException e) {
			logger.error(loggerName, e);
			throw new DBException(e.getMessage()); 
		} catch (MongoException e) {
			logger.error(loggerName, e);
			throw new DBException(e.getMessage());
		} catch (Exception e) {
			logger.error(loggerName, e);
			throw new DBException(e.getMessage());
		}
	}
	/**
	 * Returns all row which match according to search query 
	 * @param collectionName
	 * @param column
	 * @param columnValue
	 * @param clazz
	 * @return
	 */
	public List<?> find(String collectionName, String column, String columnValue, Class<?> clazz) throws DBException {
		try {
			Criteria criteria = Criteria.where(column).is(columnValue);
			Query query = new Query();
			query.addCriteria(criteria);
			return mongoOpetation.find(collectionName, query, clazz);
		}  catch (DataAccessResourceFailureException e) {
			logger.error(loggerName, e);
			throw new DBException(e.getMessage()); 
		} catch (MongoException e) {
			logger.error(loggerName, e);
			throw new DBException(e.getMessage());
		} catch (Exception e) {
			logger.error(loggerName, e);
			throw new DBException(e.getMessage());
		}
	}
	/**
	 * Returns all row which match according to search query 
	 * @param collectionName
	 * @param column
	 * @param columnValue
	 * @param clazz
	 * @return
	 */
	public List find(String collectionName, String column ,String columnValue, String key, Order order, int limit, Class clazz) throws DBException {
		try {
			Criteria criteria = Criteria.where(column).is(columnValue);
			Query query = new Query();
			query.sort().on(key, order);
			query.limit(limit); 
			query.addCriteria(criteria);
			return mongoOpetation.find(collectionName, query, clazz);
		}  catch (DataAccessResourceFailureException e) {
			logger.error(loggerName, e);
			throw new DBException(e.getMessage()); 
		} catch (MongoException e) {
			logger.error(loggerName, e);
			throw new DBException(e.getMessage());
		} catch (Exception e) {
			logger.error(loggerName, e);
			throw new DBException(e.getMessage());
		}
	}
	/**
	 * To create Index on Ascending Order
	 * @param collectionName
	 * @param columnName
	 */
	public void createIndexAscending(String collectionName, String columnName) throws DBException {
		try {
			mongoOpetation.ensureIndex(collectionName, new Index(columnName, Order.ASCENDING));
		}  catch (DataAccessResourceFailureException e) {
			logger.error(loggerName, e);
			throw new DBException(e.getMessage()); 
		} catch (MongoException e) {
			logger.error(loggerName, e);
			throw new DBException(e.getMessage());
		} catch (Exception e) {
			logger.error(loggerName, e);
			throw new DBException(e.getMessage());
		}
	}
	/**
	 * To create On Descending Order
	 * @param collectionName
	 * @param columnName
	 */
	public void createIndexDescending(String collectionName, String columnName) throws DBException {
		try {
			mongoOpetation.ensureIndex(collectionName, new Index(columnName, Order.DESCENDING));
		}  catch (DataAccessResourceFailureException e) {
			logger.error(loggerName, e);
			throw new DBException(e.getMessage()); 
		} catch (MongoException e) {
			logger.error(loggerName, e);
			throw new DBException(e.getMessage());
		} catch (Exception e) {
			logger.error(loggerName, e);
			throw new DBException(e.getMessage());
		}
	}
	/**
	 * It returns all rows from collection
	 * @param collectionName
	 * @param clazz
	 * @return
	 */
	public List getCollectionList(String collectionName, Class clazz) throws DBException {
		try {
			return mongoOpetation.getCollection(collectionName, clazz);
		}  catch (DataAccessResourceFailureException e) {
			logger.error(loggerName, e);
			throw new DBException(e.getMessage()); 
		} catch (MongoException e) {
			logger.error(loggerName, e);
			throw new DBException(e.getMessage());
		} catch (Exception e) {
			logger.error(loggerName, e);
			throw new DBException(e.getMessage());
		}
	}
	/**
	 * 
	 * @param collectionName
	 * @param lastAlertTimeColumn
	 * @param lastTime
	 * @param currentTime
	 * @param alertColumn
	 * @param tweetCount
	 * @param pageSize
	 * @param clazz
	 * @return
	 * @throws DBException
	 */
	public List getUserListByPaging(String collectionName, String lastAlertTimeColumn, long lastTimeLeg, String alertColumn, int tweetCount, int never,int pageSize, Class clazz) throws DBException {
		try {
			Integer numberOfAlert[] = new Integer[Integer.parseInt(TpConstants.TWEET_MAX_COUNT)];
			for(int i=1;i<=tweetCount;i++) {
				numberOfAlert[i-1] = i;
				if(i== Integer.parseInt(TpConstants.TWEET_MAX_COUNT) ) {
					break;
				}
			}
			Criteria criteria = Criteria.where(lastAlertTimeColumn).lt(lastTimeLeg).and(alertColumn).in(numberOfAlert);
			Query query = new Query();
			query.addCriteria(criteria).limit(pageSize);
			return mongoOpetation.find(collectionName, query, clazz);
		}  catch (DataAccessResourceFailureException e) {
			logger.error(loggerName, e);
			throw new DBException(e.getMessage()); 
		} catch (MongoException e) {
			logger.error(loggerName, e);
			throw new DBException(e.getMessage());
		} catch (Exception e) {
			logger.error(loggerName, e);
			throw new DBException(e.getMessage());
		}
	}
	/**
	 * 
	 * @param collectionName
	 * @param lastAlertTimeColumn
	 * @param lastTimeLeg
	 * @param alertColumn
	 * @param tweetCount
	 * @param pageSize
	 * @param clazz
	 * @return
	 * @throws DBException
	 */
	public List getListByPagging(String collectionName, String alertColumn,int never,int pageNumber,  int pageSize, Class clazz) throws DBException {
		try {
			Criteria criteria = Criteria.where(alertColumn).gt(never);
			Query query = new Query();
			query.addCriteria(criteria).skip((pageNumber-1)*pageSize).limit(pageSize);
			return mongoOpetation.find(collectionName, query, clazz);
		}  catch (DataAccessResourceFailureException e) {
			logger.error(loggerName, e);
			throw new DBException(e.getMessage()); 
		} catch (MongoException e) {
			logger.error(loggerName, e);
			throw new DBException(e.getMessage());
		} catch (Exception e) {
			logger.error(loggerName, e);
			throw new DBException(e.getMessage());
		}
	}
	/**
	 * Get the count
	 * @param collectionName
	 * @param lastAlertTimeColumn
	 * @param lastTimeLeg
	 * @param alertColumn
	 * @param tweetCount
	 * @param clazz
	 * @return
	 * @throws DBException
	 */
	public int getCount(String collectionName, BasicDBObject query, Class clazz) throws DBException {
		try {
			DBCollection collection = mongoOpetation.getCollection(collectionName);
			return (int) collection.count(query);
		}  catch (DataAccessResourceFailureException e) {
			logger.error(loggerName, e);
			throw new DBException(e.getMessage()); 
		} catch (MongoException e) {
			logger.error(loggerName, e);
			throw new DBException(e.getMessage());
		} catch (Exception e) {
			logger.error(loggerName, e);
			throw new DBException(e.getMessage());
		}
	}
	/**
	 * 
	 * @param collectionName
	 * @param columnName
	 * @param columnValue
	 * @param map
	 * @throws DBException
	 */
	public void updateMultiColumn(String collectionName,String columnName, Object columnValue, Map<String, Object> map) throws DBException {
		try {
			DBCollection collection = mongoOpetation.getCollection(collectionName);
			BasicDBObject query = new BasicDBObject().append(columnName, columnValue);
			BasicDBObject columnToUpdate = new BasicDBObject();
			Iterator iter = map.keySet().iterator();
			while(iter.hasNext()) {
				String key = (String) iter.next();
				Object value = map.get(key);
				columnToUpdate.append(key, value);
			}
			BasicDBObject multipleColumnUpdate = new BasicDBObject().append(MongoQueryConstant.SET, columnToUpdate);
			collection.update(query, multipleColumnUpdate);
		}  catch (DataAccessResourceFailureException e) {
			logger.error(loggerName, e);
			throw new DBException(e.getMessage()); 
		} catch (MongoException e) {
			logger.error(loggerName, e);
			throw new DBException(e.getMessage());
		} catch (Exception e) {
			logger.error(loggerName, e);
			throw new DBException(e.getMessage());
		}
	}
	/**
	 * 
	 * @param collectionName
	 * @return
	 * @throws DBException
	 */
	public int getRowCount(String collectionName) throws DBException {
		try {
			DBCollection collection = mongoOpetation.getCollection(collectionName);
			return (int) collection.count();
		}  catch (DataAccessResourceFailureException e) {
			logger.error(loggerName, e);
			throw new DBException(e.getMessage()); 
		} catch (MongoException e) {
			logger.error(loggerName, e);
			throw new DBException(e.getMessage());
		} catch (Exception e) {
			logger.error(loggerName, e);
			throw new DBException(e.getMessage());
		}
	}
	/**
	 * Update
	 * @param collectionName
	 * @param column
	 * @param columnValue
	 * @param updataColumn
	 * @param updateValue
	 */
	public void updateSingleObject(String collectionName, String column, String columnValue, String updataColumn, Object updateValue) throws DBException {
		try {
			Criteria criteria = Criteria.where(column).is(columnValue);
			Query query = new Query();
			query.addCriteria(criteria);

			Update update = Update.update(updataColumn, updateValue);
			mongoOpetation.updateMulti(collectionName, query, update);
		}  catch (DataAccessResourceFailureException e) {
			logger.error(loggerName, e);
			throw new DBException(e.getMessage()); 
		} catch (MongoException e) {
			logger.error(loggerName, e);
			throw new DBException(e.getMessage());
		} catch (Exception e) {
			logger.error(loggerName, e);
			throw new DBException(e.getMessage());
		}
	}
	/**
	 * 
	 * @param collectionName
	 * @param column
	 * @param columnValue
	 * @param updataColumn
	 * @param updateValue
	 * @throws DBException
	 */
	public void updateSingleIntObject(String collectionName, String column, String columnValue, String updataColumn, int updateValue) throws DBException {
		try {
			Criteria criteria = Criteria.where(column).is(columnValue);
			Query query = new Query();
			query.addCriteria(criteria);

			Update update = Update.update(updataColumn, updateValue);
			mongoOpetation.updateMulti(collectionName, query, update);
		}  catch (DataAccessResourceFailureException e) {
			logger.error(loggerName, e);
			throw new DBException(e.getMessage()); 
		} catch (MongoException e) {
			logger.error(loggerName, e);
			throw new DBException(e.getMessage());
		} catch (Exception e) {
			logger.error(loggerName, e);
			throw new DBException(e.getMessage());
		}
	}
	/**
	 * 
	 * @param collectionName
	 * @param column
	 * @param columnValue
	 * @param updataColumn
	 * @param updateValue
	 * @throws DBException
	 */
	public void updateSingleObjectById(String collectionName, String id, String updataColumn, long updateValue) throws DBException {
		try {
			Criteria criteria = Criteria.whereId().is((new ObjectId(id)));
			Query query = new Query().addCriteria(criteria);;
			Update update = Update.update(updataColumn, updateValue);
			mongoOpetation.updateMulti(collectionName, query, update);
		}  catch (DataAccessResourceFailureException e) {
			logger.error(loggerName, e);
			throw new DBException(e.getMessage()); 
		} catch (MongoException e) {
			logger.error(loggerName, e);
			throw new DBException(e.getMessage());
		} catch (Exception e) {
			logger.error(loggerName, e);
			throw new DBException(e.getMessage());
		}
	}
	/**
	 * 
	 * @param collectionName
	 * @param ids
	 * @param updataColumn
	 * @param updateValue
	 * @throws DBException
	 */
	public void updateMultiById(String collectionName, List<String> ids, String updataColumn, long updateValue) throws DBException {
		try {
			List<ObjectId> objectIds = new ArrayList<ObjectId>();
			for (String id: ids) {
				objectIds.add(new ObjectId(id));
			}
			Criteria criteria = Criteria.where("_id").in(objectIds.toArray());
			Query query = new Query().addCriteria(criteria);;
			Update update = Update.update(updataColumn, updateValue);
			WriteResult result = mongoOpetation.updateMulti(collectionName, query, update);
		}  catch (DataAccessResourceFailureException e) {
			logger.error(loggerName, e);
			throw new DBException(e.getMessage()); 
		} catch (MongoException e) {
			logger.error(loggerName, e);
			throw new DBException(e.getMessage());
		} catch (Exception e) {
			logger.error(loggerName, e);
			throw new DBException(e.getMessage());
		}
	}
	/**
	 * Remove particular field.
	 * @param collectionName
	 * @param column
	 * @param columnValue
	 * @param clazz
	 */
	@SuppressWarnings("unchecked")
	public void removeSingleData(String collectionName, String column , String columnValue, Class clazz) throws DBException {
		try {
			mongoOpetation.remove(collectionName, new Query(Criteria.where(column).is(columnValue)), clazz);
		}  catch (DataAccessResourceFailureException e) {
			logger.error(loggerName, e);
			throw new DBException(e.getMessage()); 
		} catch (MongoException e) {
			logger.error(loggerName, e);
			throw new DBException(e.getMessage());
		} catch (Exception e) {
			logger.error(loggerName, e);
			throw new DBException(e.getMessage());
		}
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
	public LoggingService getLogger() {
		return logger;
	}
	public void setLogger(LoggingService logger) {
		this.logger = logger;
	}
	public String getLoggerName() {
		return loggerName;
	}
	public void setLoggerName(String loggerName) {
		this.loggerName = loggerName;
	}
}