/** Copyright 2014 TappingStone, Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */

package io.prediction.controller.java

import io.prediction.core.BasePreparator
import io.prediction.controller.Params

import java.util.{ List => JList }
import java.lang.{ Iterable => JIterable }
import scala.collection.JavaConversions._

import org.apache.spark.rdd.RDD

import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.api.java.JavaSparkContext

import scala.reflect._

/**
 * Base class of a local preparator.
 *
 * A local preparator runs locally within a single machine and produces prepared
 * data that can fit within a single machine.
 *
 * @param <TD> Training Data
 * @param <PD> Prepared Data
 */
abstract class LJavaPreparator[TD, PD]
  extends BasePreparator[RDD[TD], RDD[PD]]{ 

  def prepareBase(sc: SparkContext, td: RDD[TD]): RDD[PD] = {
    implicit val fakeTdTag: ClassTag[PD] = JavaUtils.fakeClassTag[PD]
    td.map(prepare)
  }

  /**
   * Implement this method to produce prepared data that is ready for model
   * training.
   */
  def prepare(td: TD): PD
}

/**
 * A helper concrete implementation of {@link LJavaPreparator} that pass
 * training data through without any special preparation.
 *
 * @param <TD> Training Data
 */
class LJavaIdentityPreparator[TD] extends LJavaPreparator[TD, TD] {
  override def prepare(td: TD): TD = td
}

/**
 * A helper concrete implementation of {@link LJavaPreparator} that pass
 * training data through without any special preparation.
 */
object LJavaIdentityPreparator {
  /** Produces an instance of {@link LJavaIdentityPreparator}. */
  def apply[TD](ds: Class[_ <: LJavaDataSource[_, TD, _, _]]) =
    classOf[LJavaIdentityPreparator[TD]]

  /**
   * Returns an instance of {@link LJavaIdentityPreparator} by taking a {@link
   * JavaEngineBuilder} as argument.
   */
  def apply[TD, B <: JavaEngineBuilder[TD, _, TD, _, _, _]](b: B) =
    classOf[LJavaIdentityPreparator[TD]]

}

/** Base class of a parallel preparator.
  *
  * A parallel preparator can be run in parallel on a cluster and produces a
  * prepared data that is distributed across a cluster.
  *
  * @param <TD> Training data class.
  * @param <PD> Prepared data class.
  */
abstract class PJavaPreparator[TD, PD]
  extends BasePreparator[TD, PD] {
  def prepareBase(sc: SparkContext, td: TD): PD = {
    implicit val fakeTdTag: ClassTag[PD] = JavaUtils.fakeClassTag[PD]
    prepare(new JavaSparkContext(sc), td)
  }

  /** Implement this method to produce prepared data that is ready for model
    * training.
    *
    * @param jsc A Java Spark context.
    * @param trainingData Training data to be prepared.
    */
  def prepare(jsc: JavaSparkContext, trainingData: TD): PD
}

/**
 * A helper concrete implementation of {@link PJavaPreparator} that pass
 * training data through without any special preparation.
 *
 * @param <TD> Training Data
 */
class PJavaIdentityPreparator[TD] extends PJavaPreparator[TD, TD] {
  override def prepare(jsc: JavaSparkContext, td: TD): TD = td
}

/**
 * A helper concrete implementation of {@link PJavaPreparator} that pass
 * training data through without any special preparation.
 */
object PJavaIdentityPreparator {
  /** Produces an instance of {@link PJavaIdentityPreparator}. */
  def apply[TD](ds: Class[_ <: PJavaDataSource[_, TD, _, _]]) =
    classOf[PJavaIdentityPreparator[TD]]

  /**
   * Returns an instance of {@link PJavaIdentityPreparator} by taking a {@link
   * PJavaEngineBuilder} as argument.
   */
  // TODO tom-chan add this back when the PJavaEngineBuilder is ready
  /*def apply[TD, B <: PJavaEngineBuilder[TD, _, TD, _, _, _]](b: B) =
    classOf[PJavaIdentityPreparator[TD]]*/

}
