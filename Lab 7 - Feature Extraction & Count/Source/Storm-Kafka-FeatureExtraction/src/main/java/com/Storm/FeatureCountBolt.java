<<<<<<< HEAD
package com.Storm;

import org.apache.storm.topology.BasicOutputCollector;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseBasicBolt;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by user on 26/10/2016.
 */
public class FeatureCountBolt extends BaseBasicBolt {
    Map<String, Integer> counts = new HashMap<String, Integer>();
    private static final Logger LOG = LoggerFactory.getLogger(FeatureCountBolt.class);
    @Override
    public void execute(Tuple tuple, BasicOutputCollector basicOutputCollector) {
        try {
            Integer count = counts.get("Feature");
            if (count == null) {
                        count = 0;
                    }
            count++;
            counts.put("Feature",count);
            BufferedWriter br = new BufferedWriter(new FileWriter(new File("output"), true));
            br.append("No of Feature: " + counts.get("Feature") + "\n");
            LOG.info("No of Feature: "+counts.get("Feature"));
            br.close();
            basicOutputCollector.emit(new Values("Features",count));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {

    }
=======
package com.Storm;

import org.apache.storm.topology.BasicOutputCollector;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseBasicBolt;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by user on 26/10/2016.
 */
public class FeatureCountBolt extends BaseBasicBolt {
    Map<String, Integer> counts = new HashMap<String, Integer>();
    private static final Logger LOG = LoggerFactory.getLogger(FeatureCountBolt.class);
    @Override
    public void execute(Tuple tuple, BasicOutputCollector basicOutputCollector) {
        try {
            Integer count = counts.get("Feature");
            if (count == null) {
                        count = 0;
                    }
            count++;
            counts.put("Feature",count);
            BufferedWriter br = new BufferedWriter(new FileWriter(new File("output"), true));
            br.append("No of Feature: " + counts.get("Feature") + "\n");
            LOG.info("No of Feature: "+counts.get("Feature"));
            br.close();
            basicOutputCollector.emit(new Values("Features",count));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {

    }
>>>>>>> a0495d98d387807115d61aa80b1a900d7b9d45b7
}