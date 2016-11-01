<<<<<<< HEAD
package com.Storm;

import com.feature.EnvironmentProperties;
import org.apache.log4j.BasicConfigurator;
import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.generated.StormTopology;
import org.apache.storm.kafka.*;
import org.apache.storm.kafka.bolt.KafkaBolt;
import org.apache.storm.kafka.bolt.mapper.FieldNameBasedTupleToKafkaMapper;
import org.apache.storm.kafka.bolt.selector.DefaultTopicSelector;
import org.apache.storm.spout.SchemeAsMultiScheme;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.tuple.Fields;

import java.util.Properties;

/**
 * Created by user on 26/10/2016.
 */
public class StormVideoMain {

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        BasicConfigurator.configure();

        if (args != null && args.length > 0)
        {
            try {
                StormSubmitter.submitTopology(
                        args[0],
                        createConfig(false),
                        createTopology());
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        else
        {
            LocalCluster cluster = new LocalCluster();
            cluster.submitTopology(
                    "Storm_Video_Kafka",
                    createConfig(true),
                    createTopology());
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            cluster.shutdown();
        }
    }

    private static StormTopology createTopology()
    {
        SpoutConfig kafkaConf1 = new SpoutConfig(
                new ZkHosts(EnvironmentProperties.zkServerhost),
                EnvironmentProperties.KAFKA_TOPIC1,
                "/kafka",
                "Video-Spout");
        kafkaConf1.scheme = new SchemeAsMultiScheme(new StringScheme());;

        Properties props = new Properties();
        props.put("bootstrap.servers", EnvironmentProperties.kafka_broker);
        props.put("acks", "1");
        props.put("key.serializer", "org.apache.train.video.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.train.video.kafka.common.serialization.StringSerializer");

        KafkaBolt bolt = new KafkaBolt()
                .withProducerProperties(props)
                .withTopicSelector(new DefaultTopicSelector(EnvironmentProperties.KAFKA_TOPIC_B))
                .withTupleToKafkaMapper(new FieldNameBasedTupleToKafkaMapper());

        TopologyBuilder topology = new TopologyBuilder();

        topology.setSpout("Video-Spout", new KafkaSpout(kafkaConf1), 4);

        topology.setBolt("feature-count",new FeatureCountBolt(),4).shuffleGrouping("Video-Spout");

        return topology.createTopology();
    }
    public static class KafkaBoltKeyValueScheme extends StringKeyValueScheme {
        @Override
        public Fields getOutputFields() {
            return new Fields("message");
        }
    }
    private static Config createConfig(boolean local)
    {
        int workers = 1;
        Config conf = new Config();
        conf.setDebug(true);
        if (local)
            conf.setMaxTaskParallelism(workers);
        else
            conf.setNumWorkers(workers);
        return conf;
    }
}
=======
package com.Storm;

import com.feature.EnvironmentProperties;
import org.apache.log4j.BasicConfigurator;
import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.generated.StormTopology;
import org.apache.storm.kafka.*;
import org.apache.storm.kafka.bolt.KafkaBolt;
import org.apache.storm.kafka.bolt.mapper.FieldNameBasedTupleToKafkaMapper;
import org.apache.storm.kafka.bolt.selector.DefaultTopicSelector;
import org.apache.storm.spout.SchemeAsMultiScheme;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.tuple.Fields;

import java.util.Properties;

/**
 * Created by user on 26/10/2016.
 */
public class StormVideoMain {

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        BasicConfigurator.configure();

        if (args != null && args.length > 0)
        {
            try {
                StormSubmitter.submitTopology(
                        args[0],
                        createConfig(false),
                        createTopology());
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        else
        {
            LocalCluster cluster = new LocalCluster();
            cluster.submitTopology(
                    "Storm_Video_Kafka",
                    createConfig(true),
                    createTopology());
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            cluster.shutdown();
        }
    }

    private static StormTopology createTopology()
    {
        SpoutConfig kafkaConf1 = new SpoutConfig(
                new ZkHosts(EnvironmentProperties.zkServerhost),
                EnvironmentProperties.KAFKA_TOPIC1,
                "/kafka",
                "Video-Spout");
        kafkaConf1.scheme = new SchemeAsMultiScheme(new StringScheme());;

        Properties props = new Properties();
        props.put("bootstrap.servers", EnvironmentProperties.kafka_broker);
        props.put("acks", "1");
        props.put("key.serializer", "org.apache.train.video.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.train.video.kafka.common.serialization.StringSerializer");

        KafkaBolt bolt = new KafkaBolt()
                .withProducerProperties(props)
                .withTopicSelector(new DefaultTopicSelector(EnvironmentProperties.KAFKA_TOPIC_B))
                .withTupleToKafkaMapper(new FieldNameBasedTupleToKafkaMapper());

        TopologyBuilder topology = new TopologyBuilder();

        topology.setSpout("Video-Spout", new KafkaSpout(kafkaConf1), 4);

        topology.setBolt("feature-count",new FeatureCountBolt(),4).shuffleGrouping("Video-Spout");

        return topology.createTopology();
    }
    public static class KafkaBoltKeyValueScheme extends StringKeyValueScheme {
        @Override
        public Fields getOutputFields() {
            return new Fields("message");
        }
    }
    private static Config createConfig(boolean local)
    {
        int workers = 1;
        Config conf = new Config();
        conf.setDebug(true);
        if (local)
            conf.setMaxTaskParallelism(workers);
        else
            conf.setNumWorkers(workers);
        return conf;
    }
}
>>>>>>> a0495d98d387807115d61aa80b1a900d7b9d45b7
