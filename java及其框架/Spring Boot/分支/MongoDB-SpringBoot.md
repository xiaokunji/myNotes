配置文件,application.yml
```yml
spring:
  data:
    mongodb:
      database: JHT_CPR
      uri: mongodb://10.10.203.16:27017/JHT_CPR
```

工具类:
```java

public interface MongodbMapper<T> {

	public void save(String tableName, T entity);

	public void batchSave(String tableName, List<T> lists);
	// ... 这是接口,后面可以写很多
}
```

实现mongo工具类
```java
@Configuration
@Component
@Slf4j
public abstract class MongodbMapperImpl<T> implements MongodbMapper<T> {

	@Autowired
	private MongoTemplate mongoTemplate;

	protected abstract Class<T> getEntityClass();
	protected abstract String getCollectionName();

	@Override
	public void save(String tableName, T entity) {
		// 获取对象中属性对应的字段及其值
		Map<String, Object> map = ClassUtils.getColumnValue(entity);

		log.info("新增数据：{}", JSON.toJSONString(map));

		// 数据
		Document datas = new Document();

		for (Map.Entry<String, Object> entry : map.entrySet()) {
			datas.append(entry.getKey(), entry.getValue());
		}

		// TODO 带补充
//		MongoCollection<Document> collection = mongoDatabase.getCollection(tableName);
//
//		collection.insertOne(datas);
	}
	
	/**
	 * 分页查询数据
	 * @author xkj 
	 * @param pageIndex
	 * @param pageSize
	 * @param params 查询条件<字段名,字段值>, 字段值中 限定开头(^),限定结尾($),不写则为全匹配
	 * @param order 排序,如[id]、[id asc]、[id asc,name desc]
	 * @return
	 */
	@Override
	public PageInfos<T> pageByProps(int pageIndex, int pageSize, Map<String, Object> params, String order) {
		// 创建分页模型对象
		PageInfos<T> page = new PageInfos<T>(new ArrayList<T>(), 0L, 1, 10);

		// 查询总记录数
		int count = countByCondition(createQuery(params, null));

		// 封装结果数据
		if (count > 0) {
			Query query = createQuery(params, order);
			// 设置分页信息
			query.skip((pageIndex - 1) * pageSize);
			query.limit(pageSize);
			page = new PageInfos<T>(mongoTemplate.find(query, getEntityClass()), count, pageIndex, pageSize);
		}

		return page;
	}
	
	@Override
	public int countByCondition(Query query) {
		Long count = mongoTemplate.count(query, getEntityClass());
		return count.intValue();
	}
	
	/**
	 * 创建带有where条件（只支持等值）和排序的Query对象
	 * 
	 * @param order  排序,如[id]、[id asc]、[id asc,name desc]
	 * @param list	   需要模糊查询的字段  colName-R,colName-L 分别表示限定开头(^),限定结尾($),不写则为全匹配
	 * @param list1	   时间字段做比较(类似排序)  [updateTime >=],[updateTime >], [updateTime <],[updateTime <=]
	 * @return Query对象
	 */
	protected Query createQuery(Map<String,Object> params, String order) {
		Query query = new Query();
// TODO 指定字段查
		// where 条件
		for (Entry<String, Object> param : params.entrySet()) {
			String key = param.getKey();
			Object value = param.getValue();
			if (StringUtils.isNoneBlank(key, value+"")) {
				if (StringUtils.containsAny(value+"", "^", "$")) {
					query.addCriteria(Criteria.where(key).regex(value+""));
				} else {
					query.addCriteria(Criteria.where(key).is(value));
				}
			}
		}
		
		// 排序
		List<Order> orderList = parseOrder(order);

		if (CollectionUtils.isNotEmpty(orderList)) {
			query.with(Sort.by(orderList));
		}
		return query;
	}
	
	/**
	 * propValue,包含匹配字符做模糊查询,  分别表示限定开头(^),限定结尾($),不写则为全匹配
	 * @author xkj 
	 * @param propName 字段名
	 * @param propValue 字段值
	 * @param order 排序值
	 * @return
	 */
	@Override
	public List<T> findByProp(String propName, Object propValue, String order) {
		Query query = new Query();
		// 参数
		if (StringUtils.containsAny(propValue + "", "^", "$")) {
			query.addCriteria(Criteria.where(propName).regex(propValue + ""));
		} else {
			query.addCriteria(Criteria.where(propName).is(propValue));
		}
		// 排序
		List<Order> orderList = parseOrder(order);

		if (CollectionUtils.isNotEmpty(orderList)) {
			// query.with(new Sort(orderList));
			query.with(Sort.by(orderList));
		}
		return mongoTemplate.find(query, getEntityClass());
	}
	
	@Override
	public List<T> findByProps(Map<String, Object> map,String order) {
		Query query = createQuery(map, order);
		return mongoTemplate.find(query, getEntityClass());
	}
	
	@Override
	public List<T> aggregate(Class<T> clazz,AggregationOperation... operations) {
		Aggregation aggregation = Aggregation.newAggregation(operations);
		AggregationResults<T> results = mongoTemplate.aggregate(aggregation,getCollectionName(), clazz);
		return results.getMappedResults();
	}
	
	/**
	 * 使用聚合分页查询
	 * @author xkj 
	 * @param clazz 返参类
	 * @param pageSize 
	 * @param pageIndex
	 * @param order 排序, 
	 * @param operations  聚合类数组
	 * @return
	 */
	@Override
	public <I> PageInfos<I> pageByAggregate(Class<I> clazz, Integer pageSize, Integer pageIndex, String order,AggregationOperation... operations) {
		List<AggregationOperation> countOp = new ArrayList<>(Arrays.asList(operations));
		// count
		countOp.add(Aggregation.count().as("id"));
		Aggregation aggregationCount = Aggregation.newAggregation(countOp);
		AggregationResults<I> resultCount = mongoTemplate.aggregate(aggregationCount, getCollectionName(), clazz);
		long count = 0;
		Pattern pattern = Pattern.compile("(id=)\\d");
		Matcher matcher = pattern.matcher(resultCount.getRawResults().toString());
		if (matcher.find()) {
			count = NumberUtils.toLong(matcher.group().substring(matcher.group().length() - 1));
		}
		PageInfos<I> result = new PageInfos<>(null, count, pageIndex, pageSize);
		if (count > 0) {
			// 排序
			List<AggregationOperation> queryOp = new ArrayList<>(Arrays.asList(operations));
			List<Order> orders = parseOrder(order);
			if (CollectionUtils.isNotEmpty(orders)) {
				SortOperation sortOperation = Aggregation.sort(Sort.by(orders));
				queryOp.add(sortOperation);
			}
			// 分页
			SkipOperation skipOperation = Aggregation.skip((long) (pageIndex - 1) * pageSize);
			LimitOperation limitOperation = Aggregation.limit(pageSize);
			queryOp.add(limitOperation);
			queryOp.add(skipOperation);
			Aggregation aggregation = Aggregation.newAggregation(queryOp);
			// 求值
			AggregationResults<I> results = mongoTemplate.aggregate(aggregation, getCollectionName(), clazz);
			result = new PageInfos<>(results.getMappedResults(), count, pageIndex, pageSize);
		}
		return result;
	}
	
	@Override
	public <I> List<I> executeCommand(Class<I> clazz,String jsonCommand) {
		Document document = mongoTemplate.executeCommand(jsonCommand);
		List<I> results =  ManagerUtil.copyBeans(document.get("results"), clazz) ;
		return results;
	}
	
}
```

业务类
```java
@Autowired
private ResultRecordMapper  resultMapper;
/**
	 * 分页查询服务调用记录
	 * @author xkj 
	 * @param input
	 * @return
	 */
	@Override
	public BaseResponse queryServiceUsedRecord(SerUsedRecordInput input) {
		BaseResponse response = new BaseResponse();
		input.calculateOffset();
		Map<String, Object> params = buildSerUsedQuery(input);
		PageInfos<CallRecordDetail> record = resultMapper.pageByProps(input.getPageIndex(), input.getPageSize(),params, "createTime desc");
		PageInfos<SerUsedRecordDTO> result = new PageInfos<>(ManagerUtil.copyBeans(record.getData(), SerUsedRecordDTO.class), record.getTotalCount(),record.getPageIndex(), record.getPageSize());
		response.setRespData(result);
		resultCode.setSuccessResponse(response);
		return response;
	}
/**
	 * 构建 查询服务调用记录 查询条件
	 * @author xkj
	 * @param input
	 * @return  
	 */
	private Map<String, Object> buildSerUsedQuery(SerUsedRecordInput input) {
		Map<String, Object> params = new HashMap<>();
		if (StringUtils.isNotBlank(input.getName())) {
			params.put(ManagerConstants.MONGO_CALLER_INFO_CALLER_NAME, "^.*" + input.getName() + ".*$");// 全模糊
		}
		if (StringUtils.isNotBlank(input.getSerialNumber())) {
			params.put(ManagerConstants.MONGO_CALLER_INFO_SERIAL_NUMBER, input.getSerialNumber());
		}
		if (input.getCallType() != null) {
			params.put(ManagerConstants.MONGO_CALLER_INFO_CALL_TYPE, input.getCallType());
		}
		if (input.getIdentEffect() != null) {
			params.put(ManagerConstants.MONGO_CALLER_INFO_IDENT_EFFECT, input.getIdentEffect());
		}
		if (StringUtils.isNotBlank(input.getCarNumber())) {
			params.put(ManagerConstants.MONGO_CALLER_INFO_FRONT_IDENT_RESULT, "^.*" + input.getCarNumber() + ".*$");// 全模糊
			params.put(ManagerConstants.MONGO_CALLER_INFO_CLOUD_IDENT_RESULT, "^.*" + input.getCarNumber() + ".*$");// 全模糊
			params.put(ManagerConstants.MONGO_CALLER_INFO_HUMAN_IDENT_RESULT, "^.*" + input.getCarNumber() + ".*$");// 全模糊
		}
		return params;
	}

/**
	 * 分页查询算法识别记录
	 * @author xkj 
	 * @param input
	 * @return
	 */
	@Override
	public BaseResponse queryAgloIdentRecord(SerUsedRecordInput input) {
		BaseResponse response = new BaseResponse();
		input.calculateOffset();
//		StringBuilder  commSB= new StringBuilder(); 
//		commSB.append("{aggregate : 'CPR_CALLER_INFO', pipeline : ");
//		commSB.append("[");
//		commSB.append("{'$unwind':{'path':'$algos','preserveNullAndEmptyArrays':true}},");
//		commSB.append("{'$match':{'algos.algo_id':1}},");
//		commSB.append("{'$group':{'_id':{'algo_id':'$algos.algo_id','serial_number':'$serial_number'},'algoName':{'$last':'$algos.algo_name'},'identResult':{'$last':'$algos.ident_result'},'serialNumber':{'$last':'$serial_number'}}}");
//		commSB.append("{$project:{'algoName':'$algoName','serNumber':'$serNumber','identResult':'$identResult','identEffect':'$identEffect','humanIdentResult':'$humanIdentResult','createTime':'$create_time','_id':0}},");
//		commSB.append("]}");
//		List<AlgoIdentPageDTO> list = resultMapper.executeCommand(commSB.toString());
		PageInfos<AlgoIdentPageDTO> list = resultMapper.pageByAggregate(AlgoIdentPageDTO.class, input.getPageSize(), input.getPageIndex(), "create_time desc", buildAlgoIdentRecordOperation(input));
		response.setRespData(list);
		resultCode.setSuccessResponse(response);
		return response;
	}
	
	private AggregationOperation[] buildAlgoIdentRecordOperation(SerUsedRecordInput input) {
		// 平铺数组
		UnwindOperation unwindOperation = Aggregation.unwind(ManagerConstants.MONGO_CALLER_INFO_ALGOS, true);
		// 匹配条件
		Criteria criteria = new Criteria();
		if(input.getAlgoId() != null) {// 算法id
			criteria.and(ManagerConstants.MONGO_CALLER_INFO_ALGOS_ALGOID).is(input.getAlgoId());
		}
		if(StringUtils.isNotBlank(input.getSerialNumber())) {// 业务流水号
			criteria.and(ManagerConstants.MONGO_CALLER_INFO_SERIAL_NUMBER).is(input.getSerialNumber());
		}
		if(StringUtils.isNotBlank(input.getCarNumber())) {// 车牌号
			// 人工识别车牌 or 算法识别车牌
			criteria.orOperator(
					Criteria.where(ManagerConstants.MONGO_CALLER_INFO_ALGOS_IDENT_RESULT)
							.regex("^.*" + input.getCarNumber() + ".*$"),
					Criteria.where(ManagerConstants.MONGO_CALLER_INFO_HUMAN_IDENT_RESULT)
							.regex("^.*" + input.getCarNumber() + ".*$")) ;// 全模糊
		}
		MatchOperation matchOperation = Aggregation.match(criteria);
		// 分组
		GroupOperation groupOperation = Aggregation.group(ManagerConstants.MONGO_CALLER_INFO_ALGOS_ALGOID,ManagerConstants.MONGO_CALLER_INFO_SERIAL_NUMBER)
				.last(ManagerConstants.MONGO_CALLER_INFO_ALGOS_NAME).as("algo_name")
				.last(ManagerConstants.MONGO_CALLER_INFO_ALGOS_IDENT_RESULT).as("ident_result")
				.last(ManagerConstants.MONGO_CALLER_INFO_HUMAN_IDENT_RESULT).as(ManagerConstants.MONGO_CALLER_INFO_HUMAN_IDENT_RESULT)
				.last(ManagerConstants.MONGO_CALLER_INFO_IDENT_EFFECT).as(ManagerConstants.MONGO_CALLER_INFO_IDENT_EFFECT)
				.last(ManagerConstants.MONGO_CALLER_INFO_SERIAL_NUMBER).as(ManagerConstants.MONGO_CALLER_INFO_SERIAL_NUMBER)
				.last("create_time").as("create_time");
		// 过滤字段
//		ProjectionOperation projectionOperation = Aggregation.project("serialNumber","identResult").andExclude("_id");
		return ArrayUtils.toArray(unwindOperation,matchOperation,groupOperation);
		
	}
```

mongodbMapper类
```java
public interface ResultRecordMapper extends MongodbMapper<CallRecordDetail> {

}

```

mongodbMapper实现类
```java
@Component
public class ResultRecordMapperImpl extends MongodbMapperImpl<CallRecordDetail> implements ResultRecordMapper  {

	@Override
	protected Class<CallRecordDetail> getEntityClass() {
		return CallRecordDetail.class;
	}
	
	@Override
	protected String getCollectionName() {
		return ManagerConstants.MONGO_CALLER_INFO;
	}
}
```



> **以后可能会遇到更好的工具类吧,这是借鉴主数据中心的代码**