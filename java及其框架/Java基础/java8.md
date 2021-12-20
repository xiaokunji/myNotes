[toc]

# 介绍

**stream的优势:**  

![image-20210429111143136](https://gitee.com/xiaokunji/my-images/raw/master/myMD/java8-steam.png)

如表1-1中所示，**Stream中的操作可以分为两大类**：<u>中间操作与结束操作</u>，  

&emsp;&emsp;中间操作只是对操作进行了记录，只有结束操作才会触发实际的计算（即惰性求值），这也是Stream在迭代大集合时高效的原因之一。  

&emsp;&emsp;中间操作又可以分为无状态（Stateless）操作与有状态（Stateful）操作，前者是指元素的处理不受之前元素的影响；后者是指该操作只有拿到所有元素之后才能继续下去。  

&emsp;&emsp;结束操作又可以分为短路与非短路操作，这个应该很好理解，前者是指遇到某些符合条件的元素就可以得到最终结果；而后者是指必须处理所有元素才能得到最终结果。

>来自 <http://www.cnblogs.com/Dorae/p/7779246.html> 

大概总结一下: <u>流式迭代集合操作,中间操作不会实际计算,而且会并行处理,(一个数据会同时被处理),等到了结束操作才会触发操作(和spark很像),java8的foreach还有并发处理,在数据量很大时foreach和流式的优势才会体现</u>



# 1. 案例

函数式接口(导读):   那些地方可以用,看流的入参就行了
1. Function     =>  函数,有输入有输出    参入参数T , 返回 R                (用得多)
2. predicate  => 谓词/判定, 有输入,返回布尔值,主要作为一个谓词演算推导真假值存在   (用得多)
3. consumer => 谓词/消费,有输入无输出,
4. supplier  =>     提供, 无输入有输出,
5. (特殊)  Operator  =>    继承于 BiFunction ,所以也属于function, 算子Operator包括：UnaryOperator和BinaryOperator。分别对应单元算子和二元算子。其中BinaryOperator 可用于reduce
> 来自 <https://blog.csdn.net/lz710117239/article/details/76192629>   
>
> 来自 <http://www.sohu.com/a/123958799_465959> 

## 1.1 基本使用

```java
        // Function => 就是一个函数,有输入输出
        // 接收一个参数
        Function<Integer, Integer> add = x -> x + 1;
        System.out.println(add.apply(4)); // 输出: 5
        // 接收两个参数
        BiFunction<String, Integer, Person> addxy2Str = (x, y) -> new Person(x, y);
        System.out.println(addxy2Str.apply("xkj", 6)); // 输出: 对象信息
        String nameStr="xkj,xkq,xkk,qoo";
        List<String> nameList = Arrays.asList(nameStr.split(","));
        List<Person> personList = new ArrayList<>();
        Person p1=new Person("xkj", 4);
        personList.add(p1);
        
        // Predicate  => 可以用来过滤,查找
        //功能:找到含 xkj 的字符串  =>  返回集合  || 返回布尔
        Predicate<String> isConxkj= n->n.contains("xkj");
        List<String> xkjInfo = nameList.stream().filter(isConxkj).collect(Collectors.toList());//按 isConxkj 过滤
        boolean isHaveXkj = nameList.stream().anyMatch(isConxkj); // 按isConxkj 找到任意一个就行
        System.out.println(xkjInfo); // 输出 :  xkj
        System.out.println(isHaveXkj); // 输出: true
        
        // Consumer => 只有入参没有返参,可以做一些数据处理 可以用在forEach中给list修改属性值, 
        Consumer<Person> addPrefix = p->p.setAge(30);;
        personList.forEach(System.out::println);//修改前  // 输出:Person [name=xkj, age=4]
        personList.stream().forEach(addPrefix);
        personList.forEach(System.out::println);//修改后  // 输出 : Person [name=xkj, age=30]
        
        
        //Supplier => 可以用来取配置信息(还是定义变量来爽),还有new对象(我感觉还是直接new爽快)  (暂时不知道场景)
        Supplier<Person> person=Person::new;
        Person p = person.get(); // 每个get都是个新的对象
        System.out.println(p);  //输出: Person [name=null, age=0]
	
/**
* 判断语句是否正确执行,返回语句的返回值,报错则为null
* 比如,传入格式化时间语句,确定时间是否合法
*/
public static <T> T of(Supplier<? extends T> supplier) {
		try {
			return supplier.get();
		} catch (Throwable t) {
			return null;
		}
}

@Test
    public void testCollect() {
        initInfo();
        Predicate<String> isConxkj= n->n.contains("xk");
        //按 isConxkj 计数
        Long xkjCount = nameList.stream().filter(isConxkj).collect(Collectors.counting());
        System.out.println(xkjCount);
        
        //按 isConxkj 分区并对结果计数  (去掉后面的计数函数,就可以得到具体的值)
        Map<Boolean, Long> xkjCountMap = nameList.stream().collect(Collectors.partitioningBy(n->n.contains("xk"),Collectors.counting()));
        System.out.println(xkjCountMap);
        
        //按 isConxkj 过滤,得到的结果用,连接
        String xkJoin = nameList.stream().filter(isConxkj).collect(Collectors.joining(","));
        System.out.println(xkJoin);
        
        //按 isConxkj 过滤,得到的结果用,连接
        Map<Object, Object> xkMap = nameList.stream().filter(isConxkj).collect(Collectors.toMap(k->k+"_zz", v->v));
        System.out.println(xkMap);
        
        //按 名字map,person为val
        Map<Object, Person> xkMapByName = personList.stream().collect(Collectors.toMap(p->p.getName(),p->p));
        System.out.println(xkMapByName);
        
    //分组后去最大值
    Map<Integer, Optional<Users>> collect = users.stream().collect(Collectors.groupingBy(Users::getAge, Collectors.maxBy(Comparator.comparing(Users::getId))));
    // https://www.jianshu.com/p/21b20c375599 更多使用分组
    // https://cloud.tencent.com/developer/article/1863390
    // 按年龄分组
	Map<Integer, List<Person>> groupMapByAge = personList.stream().collect(Collectors.groupingBy(p->p.getAge()));
	System.out.println(groupMapByAge);
    
    // 用map操作数值类型时是有装箱处理的,多少有损耗,用mapToLong就是将值变成基本类型,然后用boxed变成普通流
	Long decrease  = ageList.stream().mapToLong(a->a).boxed().reduce((x,y)->x-y).orElse(0L);
	System.out.println(decrease);
	
	// 合并list
	List<ServiceOrder> serviceOrderList = new ArrayList<>();
	List<String> projectNoAll = serviceOrderList.stream().flatMap(s->s.getProjectNos().stream()).collect(Collectors.toList());
	List<AClass> unionResult = Stream.of(serviceOrderList, serviceOrderList).flatMap(Collection::stream).collect(Collectors.toList());
	

@Test
    public void testMR() {
        initInfo();
        BinaryOperator<String> lk = (x, y) -> x + y;
        Optional<String> reduceStr = nameList.stream().map(n -> n).reduce(lk);//将所有的值拼起来
        System.out.println(reduceStr.get());
    }

```

`T reduce(T identity, BinaryOperator<T> accumulator)`  

相对于一个参数的方法来说，它多了一个T类型的参数；实际上就相当于需要计算的值在Stream的基础上多了一个初始化的值
     eg.  

```java
Stream<String> s = Stream.of("test", "t1", "t2", "teeeee", "aaaa", "taaa");
System.out.println(s.reduce("[value]:", (s1, s2) -> s1.concat(s2)));    // [value]:testt1t2teeeeeaaaataaa
```
> 来自 <https://blog.csdn.net/icarusliu/article/details/79504602> 



## 1.2 其他应用



json 格式化使用, 值不为null的才输出
```java
return JSON.toJSONString(val,(PropertyFilter)((obj, name, value) -> value != null));
// NameFilter: 处理name字段,(比如对name加个后缀)
// ValueFilter: 处理value字段,(比如对value加个后缀)
// PropertyFilter: 处理obj字段
```



# 2. 高阶使用

## 2.1 受检函数式接口:

```java

package com.gree.ecommerce.utils.function;

import java.util.Optional;

/**
 * (检查)抛出lambda中异常情况
 *
 * @author A80080
 * @createDate 2021/4/21
 */
public interface CheckFun {

    interface Function<T, R> {

        /**
         * Function类型
         *
         * @param function 处理函数
         * @param p        入参
         * @return 处理结果, 报错返回 Optional.empty();
         * @author A80080
         * @createDate 2021/4/21
         */
        static <T, R> Optional<R> tryOf(Function<T, R> function, T p) {
            try {
                return Optional.ofNullable(function.apply(p));
            } catch (Throwable t) {
                return Optional.empty();
            }
        }

        /**
         * 参照 java.util.function.Function
         *
         * @param t 入参
         * @return R 类型数据
         * @throws Exception 异常
         * @author A80080
         * @createDate 2021/4/21
         */
        R apply(T t) throws Exception;

    }

    interface Supplier<T> {

        /**
         * Supplier 类型
         *
         * @param supplier 处理函数
         * @return 返回语句的返回值(optional), 报错则为Optional.empty()
         * @author A80080
         */
        static <T> Optional<T> tryOf(CheckFun.Supplier<? extends T> supplier) {
            try {
                return Optional.ofNullable(supplier.get());
            } catch (Throwable t) {
                return Optional.empty();
            }
        }

        /**
         * 参照 java.util.function.Supplier
         *
         * @return T 类型
         * @throws Exception 异常
         * @author A80080
         * @createDate 2021/4/21
         */
        T get() throws Exception;

    }

    interface Consumer<T> {

        /**
         * consumer 类型
         * <p> 报错将忽略
         *
         * @param consumer 处理函数
         * @param p        入参
         * @author A80080
         * @createDate 2021/4/21
         */
        static <T> void tryOf(CheckFun.Consumer<T> consumer, T p) {
            try {
                consumer.accept(p);
            } catch (Throwable ignored) {
            }
        }

        /**
         * 参照 java.util.function.Consumer
         *
         * @param t 入参
         * @throws Exception 异常
         * @author A80080
         * @createDate 2021/4/21
         */
        void accept(T t) throws Exception;

    }

    interface Predicate<T> {

        /**
         * Predicate类型
         *
         * @param predicate 处理函数
         * @param p         入参
         * @return 处理结果, 报错返回 false
         * @author A80080
         * @createDate 2021/4/21
         */
        static <T> boolean tryOf(CheckFun.Predicate<T> predicate, T p) {
            try {
                return predicate.test(p);
            } catch (Throwable t) {
                return false;
            }
        }

        /**
         * 参照 java.util.function.Predicate
         *
         * @param t 入参
         * @return boolean
         * @throws Exception 异常
         * @author A80080
         * @createDate 2021/4/21
         */
        boolean test(T t) throws Exception;


    }

}

// 使用案例
@Test
public void test() {
    Stream.of("uNameCookie", "uidCookie", "loginNameCookie", "loginStatusCookie", "mobileLoginTokenCookie")
        .filter(c->!CheckFun.Predicate.tryOf(a->URLEncoder.encode(c, "utf-8").equals(""),c))
        .peek(c -> c.concat(CheckFun.Supplier.tryOf(() -> URLEncoder.encode(c, "utf-8")).orElse(c)))
        .map(c -> c.concat(CheckFun.Function.tryOf(a -> URLEncoder.encode(a, "utf-8"), c).orElse(c)))
        .peek(c -> CheckFun.Consumer.tryOf(x -> c.concat(URLEncoder.encode(x, "utf-8")), c))
        .forEach(System.out::println);
}
```



## 2.2  根据类中某个字段去重以及分页处理

```java
// 根据类中某个字段去重
	@Test
    public void should_return_2_because_distinct_by_age() {
        userList = userList.stream()
                .filter(distinctByKey(User::getName))
                .collect(Collectors.toList());
        userList.forEach(System.out::println);
        assertEquals(2, userList.size());
    }

    private static <T, R> Predicate<T> distinctByKey(Function<T, R> keyExtractor) {
        Set<R> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }
// 链接：https://hacpai.com/article/1545321970124

divideBatchHandler(nameList,System.out::println);

    }

/**
     * 运用场景:当数据量过大,需要分批处理时
     * @author xkj
     * @param dataList 需要处理的数据
     * @param consumer  处理函数
     * @since 
     */
    public <T> void divideBatchHandler(List<T> dataList, Consumer<List<T>> consumer) {
        Optional.ofNullable(dataList).ifPresent(list ->
                IntStream.range(0, list.size())
                        .mapToObj(i -> new AbstractMap.SimpleImmutableEntry<>(i, list.get(i)))// 给数据编号
                        .collect(Collectors.groupingBy(
                                e -> e.getKey() / 10, 
                                Collectors.mapping(Map.Entry::getValue, Collectors.toList()))) // 按编号分批并合并编号对应的值
                        .values()
                        .parallelStream()// 并行处理
                        .forEach(consumer) // 执行处理函数
        );
    }

```



## 2.3 自定义Collector

Collector主要包含五个参数，它的行为也是由这五个参数来定义的，如下所示：

Collector实现的三个泛型具体是什么：

- T（输入的元素类型）：T
- A（累积结果的容器类型）：
- R（最终生成的结果类型）：

```java
public interface Collector<T, A, R> {
    // supplier参数用于生成结果容器，容器类型为A
    Supplier<A> supplier();
    // accumulator用于消费元素，也就是归纳元素，这里的T就是元素，它会将流中的元素一个一个与结果容器A发生操作
    BiConsumer<A, T> accumulator();
    // combiner用于两个两个合并并行执行的线程的执行结果，将其合并为一个最终结果A
    BinaryOperator<A> combiner();
    // finisher用于将之前整合完的结果R转换成为A
    Function<A, R> finisher();
    // characteristics表示当前Collector的特征值，这是个不可变Set
    Set<Characteristics> characteristics();
}
```

> 枚举常量Characteristics 中共有三个特征值，它们的具体含义如下：
>
> CONCURRENT：表示结果容器只有一个（即使是在并行流的情况下）。只有在并行流且收集器不具备此特性的情况下，combiner()返回的lambda表达式才会执行（中间结果容器只有一个就无需合并）。设置此特性时意味着多个线程可以对同一个结果容器调用，因此结果容器必须是线程安全的。
>
> UNORDERED：表示流中的元素无序。
>
> IDENTITY_FINISH：表示中间结果容器类型与最终结果类型一致。**设置此特性时finiser()方法不会被调用。**



```java
/** 该案例作用: 对集合元素 求和并转成字符串
*/
    @Test
    public void Test(){
        // 1. 第一个参数是临时值的容器
        // 2. 第二个参数是把入参放到容器中
        // 3. 第三个参数是把第二步的值做一个结合放到容器中
        // 4. 第四个参数把容器中的值转化成结果类型
        // 5. 第五个参数是一个特征值

        BiConsumer<List<Long>, Long> accumulator = (x, y) -> {
            x.add(y);
            return;
        };
        BinaryOperator<List<Long>> combiner = (x, y) -> {
            x.addAll(y);
            return x;
        };
        Function<List<Long>, String> func = x -> String.valueOf(x.stream().mapToLong(y-> y).sum());
        Collector<Long, List<Long>, String> longSupplier = Collector.of(ArrayList::new, accumulator, combiner,func, Collector.Characteristics.UNORDERED);

        String collect = Stream.of(1, 1, 2, 2)
                .map(Long::new)
                .collect(longSupplier);
        System.out.println("collect = " + collect);
        
    }
```

[java8新特性(四) Collector（收集器）_戏流年的博客-CSDN博客_collector](https://blog.csdn.net/xiliunian/article/details/88773718)

[Java基础系列-Collector和Collectors - 简书 (jianshu.com)](https://www.jianshu.com/p/7eaa0969b424)









---

