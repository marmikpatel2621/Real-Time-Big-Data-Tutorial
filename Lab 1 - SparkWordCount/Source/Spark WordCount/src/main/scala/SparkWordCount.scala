

import org.apache.spark.{SparkContext, SparkConf}

/**
 * Created by Mayanka on 09-Sep-15.
 */
object SparkWordCount {

  def main(args: Array[String]) {

    System.setProperty("hadoop.home.dir","E:\\WinUtilsForHadoop");

    val sparkConf = new SparkConf().setAppName("SparkWordCount").setMaster("local[*]")

    val sc=new SparkContext(sparkConf)

    val input=sc.textFile("input")

    val wc=input.flatMap(line=>{line.split('.')}).map(word=>(word,1))

    val output1=wc.reduceByKey(_+_)
    val sortedRDD = output1.map(_.swap).sortByKey(true).map(_.swap)
    sortedRDD.saveAsTextFile("output")

    val o=sortedRDD.collect()

    var s:String="Words:Count \n"
    o.foreach{case(word,count)=>{

      s+=word+" : "+count+"\n"

    }}

  }

}
