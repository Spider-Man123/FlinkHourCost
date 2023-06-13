# FlinkHourCost
基于Flink的状态编程和定时器实现的客户粒度的小时范围内的消耗累积
## 实现流程
1. 消费数据
2. 过滤数据并且将数据转为bean对象，如果时间为前一天的过滤掉, 逻辑为使用事件发生的时间戳和今天零点的时间戳比较。
3. 设置watermark, 之后我们要使用事件时间来触发系统时钟推进
4. 根据客户id进行keyBy，然后使用process函数来实现小时范围内的消耗累积
5. 实现costProcess类继承KeyedProcessFunction
6. 类的构造函数传入小时级别的时间戳
7. 重写open()函数，初始化mapState，并且设置过期时间（24小时）
8. 重写processElement()函数，根据数据时间戳判断所属小时范围，并且根据所属小时结束时间注册定时器，如果mapState中包含了数据中的客户id+所属小时,说明这个小时内还消耗过，因此进行累加，如果不包含，那么就想mapState新加入。key：客户id_小时开始时间
9. 重写onTimer()函数，实现定时器触发后的逻辑。得到所属时间范围的开始时间和根据客户id作为key取出对应消耗，拼接为 客户id_小时开始时间_消耗输出
10. 转换为json字符串写入对应的topic中
## 测试
1. producer
![Image text](https://github.com/Spider-Man123/FlinkHourCost/blob/4c3bc19b1e211c64d21a609cb78ab2857f7fba9c/img/producer.png)
3. consumer
![Image text](https://github.com/Spider-Man123/FlinkHourCost/blob/4c3bc19b1e211c64d21a609cb78ab2857f7fba9c/img/consumer.png)
