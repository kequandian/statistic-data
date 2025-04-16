## 自动报表配置说明
[自动报表模块说明](./statistic/README.md)


## statistic-data
[![Build Status](https://travis-ci.org/pravega/pravega.svg?branch=master)](https://travis-ci.org/pravega/pravega/builds) [![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0) 

### 基于springboot的报表快速开发模块
基于 [ECHART](https://echarts.apache.org/examples/zh/index.html) 图表展示

### 特性
结合zero-element将统计报表可配置化。只需编写相应sql语句，即可通过相应配置做到展示统计图表，极大程度上减少了工作量。

### 使用
zero-data目前仅作为单独模块提供API支持，使用/api/stat/groups/{group}接口获取数据，通过identifier参数进行数据筛选。


### 状态统计查询例子
```sql
SELECT (CASE 
           WHEN t.status='DELIVERING' THEN '执行中',
           WHEN t.status='DELIVERED' THEN '已下发',
           WHEN t.status='CONFIRMING' THEN '待确认',
           WHEN t.status='VERIFIED' THEN '已确认',
           WHEN t.status='CLOSED' THEN '已关闭' 
	END）as name,
	COUNT(*) as value
FROM t_task_order t
GROUP BY t.status
```

### 月度查询例子
> * 从一月至十二月个字段
```sql
select 	SUM(CASE WHEN MONTH(s.create_time) = 1 THEN 1 ELSE 0 END) as '一月',
				SUM(CASE WHEN MONTH(s.create_time) = 2 THEN 1 ELSE 0 END) as '二月',
				SUM(CASE WHEN MONTH(s.create_time) = 3 THEN 1 ELSE 0 END) as '三月',
				SUM(CASE WHEN MONTH(s.create_time) = 4 THEN 1 ELSE 0 END) as '四月',
				SUM(CASE WHEN MONTH(s.create_time) = 5 THEN 1 ELSE 0 END) as '五月',
				SUM(CASE WHEN MONTH(s.create_time) = 6 THEN 1 ELSE 0 END) as '六月',
				SUM(CASE WHEN MONTH(s.create_time) = 7 THEN 1 ELSE 0 END) as '七月',
				SUM(CASE WHEN MONTH(s.create_time) = 8 THEN 1 ELSE 0 END) as '八月',
				SUM(CASE WHEN MONTH(s.create_time) = 9 THEN 1 ELSE 0 END) as '九月',
				SUM(CASE WHEN MONTH(s.create_time) = 10 THEN 1 ELSE 0 END) as '十月',
				SUM(CASE WHEN MONTH(s.create_time) = 11 THEN 1 ELSE 0 END) as '十一月',
				SUM(CASE WHEN MONTH(s.create_time) = 12 THEN 1 ELSE 0 END) as '十二月'
FROM ca_adverting_plan AS s
```

> * 返回12行数据
```sql
SELECT mon, total FROM (
   SELECT
	CASE 
	WHEN MONTH(t.createtime)=1 then  '一月'
	WHEN MONTH(t.createtime)=2 then  '二月'
	WHEN MONTH(t.createtime)=3 then  '三月'
	WHEN MONTH(t.createtime)=4 then  '四月'
	WHEN MONTH(t.createtime)=5 then  '五月'
	WHEN MONTH(t.createtime)=6 then  '六月'
	WHEN MONTH(t.createtime)=7 then  '七月'
	WHEN MONTH(t.createtime)=8 then  '八月'
	WHEN MONTH(t.createtime)=9 then '九月'
	WHEN MONTH(t.createtime)=10 then '十月'
	WHEN MONTH(t.createtime)=11 then '十一月'
	WHEN MONTH(t.createtime)=12 then  '十二月'
	END as mon,
   COUNT(id) as total, MONTH(t.createtime) as seq FROM t_sys_user t GROUP BY mon
   --
   -- INCLUDE 0 excluded by GROUP BY
   --
   UNION (SELECT '一月' as mon, 0 as total, 1 as seq)
   UNION (SELECT '二月' as mon, 0 as total, 2 as seq)
   UNION (SELECT '三月' as mon, 0 as total, 3 as seq)
   UNION (SELECT '四月' as mon, 0 as total, 4 as seq)
   UNION (SELECT '五月' as mon, 0 as total, 5 as seq)
   UNION (SELECT '六月' as mon, 0 as total, 6 as seq)
   UNION (SELECT '七月' as mon, 0 as total, 7 as seq)
   UNION (SELECT '八月' as mon, 0 as total, 8 as seq)
   UNION (SELECT '九月' as mon, 0 as total, 9 as seq)
   UNION (SELECT '十月' as mon, 0 as total, 10 as seq)
   UNION (SELECT '十一月' as mon, 0 as total, 11 as seq)
   UNION (SELECT '十二月' as mon, 0 as total, 12 as seq)
) t GROUP BY mon ORDER BY seq
```


```SQL
-- --本月之前（包括本月）之前12月
-- SELECT DATE_FORMAT( @cdate := DATE_ADD( @cdate, INTERVAL + 1 MONTH ), '%Y-%m' ) AS month_list 
-- FROM ( SELECT @cdate := DATE_ADD( CURRENT_DATE, INTERVAL - 12 MONTH ) FROM `pcd` LIMIT 12 ) t0 
-- WHERE DATE( @cdate ) <= DATE_ADD( CURRENT_DATE, INTERVAL - 1 DAY ) 
--
-- USE UNION to remove dependency from `pcd`
--
SELECT MONTH( @cdate := DATE_ADD( @cdate, INTERVAL + 1 MONTH )) AS mon
FROM ( SELECT @cdate := DATE_ADD( concat(YEAR(now()),'-12-31'), INTERVAL - 12 MONTH ) FROM 
(SELECT 1 as seq
 UNION (SELECT 2 as seq)
 UNION (SELECT 3 as seq)
 UNION (SELECT 4 as seq)
 UNION (SELECT 5 as seq)
 UNION (SELECT 6 as seq)
 UNION (SELECT 7 as seq)
 UNION (SELECT 8 as seq)
 UNION (SELECT 9 as seq)
 UNION (SELECT 10 as seq)
 UNION (SELECT 11 as seq)
 UNION (SELECT 12 as seq)) t0) t1
-- END 

-- 今年1月到12月
SELECT DATE_FORMAT( @cdate := DATE_ADD( @cdate, INTERVAL + 1 MONTH ), '%Y-%m' ) AS month_list 
FROM ( SELECT @cdate := DATE_ADD( concat(year(now()),'-12-31'), INTERVAL - 12 MONTH ) FROM `pcd` LIMIT 12 ) t0 
WHERE DATE( @cdate ) <= DATE_ADD( concat(year(now()),'-12-31'), INTERVAL - 1 DAY ) 
```

#### 测试方式
```sql
DROP TABLE IF EXISTS t_sink;
CREATE TABLE t_sink (`name` varchar(26), `count` int);
INSERT INTO t_sink VALUES ('aaa', 2), ('bbb', 10);

select name,count as '排序' from t_sink ORDER BY `排序` DESC;

DROP TABLE IF EXISTS t_sink;
```


#### 导出为Excel文件类型报表

GET `/api/adm/stat/meta/{field}/export/excel`

参数列表：

| **参数** |    **描述**    |
| :------: | :------------: |
|  field   | 数据库自动报表域名 |

访问的数据接口API `/api/adm/stat/meta/{field}` ，获取API中的数据作为导出Excel的数据源.
