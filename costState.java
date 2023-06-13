package state;

import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkGenerator;
import org.apache.flink.api.common.eventtime.WatermarkGeneratorSupplier;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.common.state.MapState;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.state.StateTtlConfig;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;
import org.apache.flink.streaming.util.serialization.KeyedSerializationSchema;
import org.apache.flink.util.Collector;
import utils.FlinkUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
/*
    需要说明其中使用到的环境配置和消息队列的代码需要自己配置属于自己的，我的kafka消费生产等代码未展示
*/
public class costState {
    public static void main(String[] args) throws Exception {
        FlinkUtils flinkUtils = new FlinkUtils();
        StreamExecutionEnvironment env = flinkUtils.getEnv(1);
        DataStreamSource<String> data = flinkUtils.kafkaProducer(env, "click");
        SingleOutputStreamOperator<costEvent> stream = data.map(new MapFunction<String, costEvent>() {
            @Override
            public costEvent map(String s) throws Exception {
                String[] str = s.split(" ");
                return new costEvent(str[0], Integer.valueOf(str[1]), Long.valueOf(str[2]));
            }
       }).filter(line->handleDate(line.getTimeStamp())).assignTimestampsAndWatermarks(WatermarkStrategy.<costEvent>forMonotonousTimestamps().withTimestampAssigner(new SerializableTimestampAssigner<costEvent>() {
            @Override
            public long extractTimestamp(costEvent costEvent, long l) {
                return costEvent.getTimeStamp();
            }
        }));
        Properties properties=new Properties();
        properties.setProperty("bootstrap.servers","localhost:9092");
        stream.keyBy(costEvent::getCustomerId).process(new costProcess(3600*1000L)).map(new MapFunction<result, String>() {
            @Override
            public String map(result result) throws Exception {
                return result.getStart()+" "+result.getCost();
            }
        }).addSink(new FlinkKafkaProducer<String>(
                        "res",
                        new SimpleStringSchema(),
                        properties));
        env.execute();
    }
    public static class costProcess extends KeyedProcessFunction<String,costEvent,result>{
        private final Long size;
        private MapState<String,Integer> res;
        public costProcess(Long size){
            this.size=size;
        }
        SimpleDateFormat simpleDateFormat;
        @Override
        public void open(Configuration parameters) throws Exception {
            MapStateDescriptor<String, Integer> descriptor = new MapStateDescriptor<>("cost", String.class, Integer.class);
            StateTtlConfig stateTtlConfig = new StateTtlConfig
                    .Builder(Time.hours(24))
                    .build();
            descriptor.enableTimeToLive(stateTtlConfig);
            res = getRuntimeContext().getMapState(descriptor);
            simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH");
        }

        @Override
        public void processElement(costEvent costEvent, KeyedProcessFunction<String, costEvent, result>.Context context, Collector<result> collector) throws Exception {

            Long start=costEvent.getTimeStamp()/size*size;
            long end=start+size;
            context.timerService().registerEventTimeTimer(end-1);
            if(res.contains(costEvent.getCustomerId()+"_"+start)){
                res.put(costEvent.getCustomerId()+"_"+start,res.get(costEvent.getCustomerId()+"_"+start)+costEvent.getCost());
            }else{
                res.put(costEvent.getCustomerId()+"_"+start,costEvent.getCost());
            }

        }

        @Override
        public void onTimer(long timestamp, KeyedProcessFunction<String, costEvent, result>.OnTimerContext ctx, Collector<result> out) throws Exception {
            long end=timestamp+1;
            long start=end-size;
            Integer cost = res.get(ctx.getCurrentKey()+"_"+start);
            out.collect(new result(ctx.getCurrentKey()+"_"+simpleDateFormat.format(start),cost));
        }
    }
    private static boolean handleDate(long time) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date(time);
        Date now = sdf.parse(sdf.format(new Date()));
        long nowTime = now.getTime();
        if(time<=nowTime){
            return false;
        }else{
            return true;
        }
    }

}
