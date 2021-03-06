package is.hail.sparkextras

import is.hail.backend.spark.SparkBackend
import is.hail.utils.Interval
import org.apache.spark.{Dependency, Partition, RangeDependency, SparkContext, TaskContext}
import org.apache.spark.rdd.RDD

import scala.reflect.ClassTag

case class IndexedFilePartition(index: Int, file: String, bounds: Option[Interval]) extends Partition

class IndexReadRDD[T: ClassTag](
  @transient val partFiles: Array[String],
  @transient val intervalBounds: Option[Array[Interval]],
  f: (IndexedFilePartition, TaskContext) => T
) extends RDD[T](SparkBackend.sparkContext("IndexReadRDD"), Nil) {
  def getPartitions: Array[Partition] =
    Array.tabulate(partFiles.length) { i =>
      IndexedFilePartition(i, partFiles(i), intervalBounds.map(_(i)))
    }

  override def compute(
    split: Partition, context: TaskContext
  ): Iterator[T] = {
    Iterator.single(f(split.asInstanceOf[IndexedFilePartition], context))
  }
}
