/**
  *@author : Ravi kiran Yadavalli , Ragunandan Rao Malangully
  *version : 1.0.0
  *Machine Leaning Part of Audio Analysis
  */

import java.io.File
import javax.sound.sampled.AudioInputStream

import jAudioFeatureExtractor.AudioFeatures._
import jAudioFeatureExtractor.jAudioTools.AudioSamples
import org.apache.spark.mllib.evaluation.MulticlassMetrics
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.tree.DecisionTree
import org.apache.spark.mllib.tree.model.DecisionTreeModel
import org.apache.spark.{SparkConf, SparkContext}


object AudioClassification {

  val AUDIO_CATEGORIES = List("Bear", "Bison","Cat", "Dog","Elephant","Jaguar","Lion")

  def main(args: Array[String]) {
    System.setProperty("hadoop.home.dir", "E:\\WinUtilsForHadoop")
    val sparkConf = new SparkConf().setMaster("local[*]").setAppName("SparkDecisionTree").set("spark.driver.memory", "4g")
    val sc = new SparkContext(sparkConf)
    //training data
  val train = sc.textFile("TrainingData.txt")
    val X_train= train.map ( line =>{
      val parts = line.split(':')
      println(AUDIO_CATEGORIES.indexOf(parts(0)).toDouble, Vectors.dense(parts(1).split(' ').map(_.toDouble)))
      LabeledPoint(AUDIO_CATEGORIES.indexOf(parts(0)).toDouble, Vectors.dense(parts(1).split(' ').map(_.toDouble)))

    })


    val test = sc.textFile("TestingData.txt")
    val X_test= train.map ( f = line => {
      val parts = line.split(':')
      println(AUDIO_CATEGORIES.indexOf(parts(0)).toDouble, Vectors.dense(parts(1).split(' ').map(_.toDouble)))
      LabeledPoint(AUDIO_CATEGORIES.indexOf(parts(0)).toDouble, Vectors.dense(parts(1).split(' ').map(_.toDouble)))

    })

    val numClasses = 7
    val categoricalFeaturesInfo = Map[Int, Int]()
    val impurity = "gini"
    val maxDepth = 5
    val maxBins = 32

    val model = DecisionTree.trainClassifier(X_train, numClasses, categoricalFeaturesInfo,
      impurity, maxDepth, maxBins)



    val labelAndPreds = X_test.map { point =>
      val prediction = model.predict(point.features)
      (point.label, prediction)
    }
    labelAndPreds.foreach(f=>{
      println("prediction" +f._1 + " Actual Label" + f._2)

    })
    val testErr = labelAndPreds.filter(r => r._1 != r._2).count.toDouble / X_test.count()
    println("Test Error = " + testErr)
    println("Learned classification forest model:\n" + model.toDebugString)
                                                                        //val  accuracy = 1.0 * labelAndPreds.filter(x => x._1 == x._2).count() / test.count()



    println("Prediction and label" + labelAndPreds)


    val metrics = new MulticlassMetrics(labelAndPreds)
                                                                        //println("Confusion Matrix \n \n : "+metrics.confusionMatrix)
    println("Fmeasure is:"+metrics.fMeasure + "Precision is:" +metrics.precision)
    println("Accuracy : " + metrics.precision)
                                                                        // Save and load model
    model.save(sc, "ContextDecisionTreeClassificationModel")
    val sameModel = DecisionTreeModel.load(sc, "ContextDecisionTreeClassificationModel")
                                                                       // sameModel.save(sc, "myRandomForestClassificationModel1")


  }
}
