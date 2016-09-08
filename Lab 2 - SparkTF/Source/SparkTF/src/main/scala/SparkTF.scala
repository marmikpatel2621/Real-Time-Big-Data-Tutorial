import org.apache.spark.{SparkContext, SparkConf}

import java.io._

/**
  * Created by user on 07/09/2016.
  */
object SparkTF {
  def main(args: Array[String]) {
    System.setProperty("hadoop.home.dir", "E:\\WinUtilsForHadoop");

    val sparkConf = new SparkConf().setAppName("SparkTransformation").setMaster("local[*]")

    val sc = new SparkContext(sparkConf)

    val input=sc.textFile("Data/input.txt")

    val wc=input.flatMap(line=>{line.split(" ")}).map(word=>(word,1))

    val output1=wc.reduceByKey(_+_)

    val sortedRDD = output1.map(_.swap).sortByKey(false).map(_.swap)

    val max = sortedRDD.first()._2

    val floatRDD = sortedRDD.mapValues(x => x :Float)

    val tfRDD = floatRDD.mapValues(x => x/max)

    val count = tfRDD.count()

    val top20RDD = tfRDD.take(20)

    val wordRDD = tfRDD.filter{
      case (key,value) => value > 0.7
    }
    tfRDD.saveAsTextFile("OutputTF")

    wordRDD.saveAsTextFile("output0.7")

    val file = new File("Data/output.txt")
    val bw = new BufferedWriter(new FileWriter(file))

    bw.append("Total unique words : " + count)
    bw.newLine()
    bw.append("Top 20 words with highes frequency")
    bw.newLine()
    for( i <- 0 until top20RDD.length){
      bw.append(top20RDD(i)._1)
      bw.newLine()
    }
    bw.close()
  }
}
