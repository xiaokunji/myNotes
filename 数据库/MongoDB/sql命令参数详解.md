```
2017-11-10T10:28:34.939+0800 I COMMAND [conn57] command DJangoLearn.sale_mongo command: 
aggregate {
	aggregate: "sale_mongo",
	pipeline: [{ 
		$match: {
			id: 45
		}
	}, { 
		$group: {
			_id: "$id",
			maxPrice: { 
				$max: "$sellPrice"
			},
			minPrice: {
				$min: "\$sellPrice"
			},
			avgPrice: {\
				$avg: "\$sellPrice"
			}
		}
	}],
	cursor: {}
} 
planSummary: COLLSCAN 
keysExamined: 0 
docsExamined: 5000000 
cursorExhausted: 1 
numYields: 39194 
nreturned: 1 
reslen: 181 
locks: {
	Global: {
		acquireCount: {
			r: 78396
		}
	},
	Database: {
		acquireCount: {
			r: 39198
		}
	},
	Collection: {
		acquireCount: {
			r: 39197
		}
	}
}
protocol: op_query 3668 ms
```
参数:  
`planSummary`: 查询的计划,集合扫描(CollectionScan)  
`keysExamined`: key的扫描数量 #考虑创建或调整索引以提高查询性能 #IXSCAN 索引扫描(加索引)  
`docsExamined`: 文档的扫描数量 (与结果行数量差太多,考虑加索引)  
`cursorExhausted`: 消耗的游标数  
`numYields:39194`: 查询等待插入的次数   #计数器，报告操作已经完成的次数，
`nreturned`: 返回的行数  
`reslen`: 操作结果文档的字节长度 #如果太长,考虑去除多余字段 
locks:{   
    `Global`: 全局下获取意向共享锁(表/页)的操作次数 (写)  
    `Database`: 数据库获得的锁  
    `Collection`: 集合获得的锁  
}  
`protocol`: 协议

```
2017-11-20T10:13:45.346+0800 I COMMAND [conn4] command DJangoLearn.sale_mongo command: aggregate { aggregate: "sale_mongo", pipeline: [ { match: { id: 43 } }, {match: { id: 43 } }, {group: { _id: "id", maxPrice: {id", maxPrice: {max: "sellPrice" }, minPrice: {sellPrice" }, minPrice: {min: "sellPrice" }, avgPrice: {sellPrice" }, avgPrice: {avg: "$sellPrice" } } } ], cursor: {} } 
	planSummary: IXSCAN { id: 1 } 
	keysExamined:1000 
	docsExamined:1000 
	cursorExhausted:1 
	numYields:8 
	nreturned:1 
	reslen:181 
	locks:{ 
	Global: { acquireCount: { r: 24 } }, 
	Database: { acquireCount: { r: 12 } }, 
	Collection: { acquireCount: { r: 11 } } 
	} 
	protocol:op_query 339ms
```