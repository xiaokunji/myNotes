**查询和" 01 "号的同学学习的课程   完全相同的其他同学的信息**

​		-- 不存在这样的课程 , 01学了,但是学生x没学  ->   蕴含逻辑运算

​		-- (像这种蕴含逻辑运算,涉及到离散,做一些简单倒是可以,比如"完全相同","没有全部拥有的"等全量字眼)

```sql
SELECT *
FROM student
WHERE  NOT EXISTS( 
	SELECT * 
	FROM stucou  sc1
	WHERE sc1.SId='01' AND NOT EXISTS( 
		SELECT * 
		FROM stucou sc2 
		WHERE sc2.SId=student.SId AND sc1.CId=sc2.CId)
);
```



> 详解:https://blog.csdn.net/qsyzb/article/details/12525955

**查询学过"张三"老师讲授的全部课程的学生姓名**

​	-- 不存在这样的课程,张三老师教了,但是学生没学

```sql
SELECT
	s.*
FROM student s 
WHERE NOT EXISTS (
	SELECT 1 FROM teacher t 
	INNER JOIN course c ON  c.TId= t.TId
	WHERE t.Tname='张三' AND  NOT EXISTS (
		SELECT 1 FROM stucou sc 
		WHERE c.CId = sc.CId AND sc.SId=s.SId
	)
)
```



**对于employees表中，给出奇数行的first_name**

```sql
select e.first_name
from employees e
where 
(select count(1) from employees e1 where  e.first_name >= e1.first_name)%2!=0
```



> 来自: https://www.nowcoder.com/practice/e3cf1171f6cc426bac85fd4ffa786594?tpId=82&tqId=29829&rp=0&ru=%2Fta%2Fsql&qru=%2Fta%2Fsql%2Fquestion-ranking&tPage=4

**对所有员工的当前(to_date='9999-01-01')薪水按照salary进行按照1-N的排名，相同salary并列且按照emp_no升序排列**

-- 排名: 小于自己的有多少个,就是排第几,有点类似计数排序

```sql
select s.emp_no,s.salary,count(distinct s1.salary) as rank
from salaries s,salaries s1
where s.to_date='9999-01-01' and s1.to_date='9999-01-01' and s.salary<=s1.salary
group by s.emp_no
order by s.salary desc,s.emp_no asc
```

