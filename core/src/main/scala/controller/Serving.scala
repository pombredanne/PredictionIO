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

package io.prediction.controller

import io.prediction.core.BaseAlgorithm
import io.prediction.core.BaseServing

import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.rdd.RDD

import scala.reflect._
import scala.reflect.runtime.universe._

/** Base class of serving. 
  *
  * @tparam Q Input query class.
  * @tparam P Output prediction class.
  * @group Serving
  */
abstract class LServing[Q, P] extends BaseServing[Q, P] {
  def serveBase(q: Q, ps: Seq[P]): P = {
    serve(q, ps)
  }

  /** Implement this method to combine multiple algorithms' predictions to
    * produce a single final prediction.
    *
    * @param query Input query.
    * @param predictions A list of algorithms' predictions.
    */
  def serve(query: Q, predictions: Seq[P]): P
}

/** A concrete implementation of [[LServing]] returning the first algorithm's
  * prediction result directly without any modification.
  *
  * @group Serving
  */
class LFirstServing[Q, P] extends LServing[Q, P] {
  /** Returns the first algorithm's prediction. */
  def serve(query: Q, predictions: Seq[P]): P = predictions.head
}

/** A concrete implementation of [[LServing]] returning the first algorithm's
  * prediction result directly without any modification.
  *
  * @group Serving
  */
object LFirstServing {
  /** Returns an instance of [[LFirstServing]]. */
  def apply[Q, P](a: Class[_ <: BaseAlgorithm[_, _, Q, P]]) =
    classOf[LFirstServing[Q, P]]
}

/** A concrete implementation of [[LServing]] returning the average of all
  * algorithms' predictions. The output prediction class is Double.
  *
  * @group Serving
  */
class LAverageServing[Q] extends LServing[Q, Double] {
  /** Returns the average of all algorithms' predictions. */
  def serve(query: Q, predictions: Seq[Double]): Double = {
    predictions.sum / predictions.length
  }
}

/** A concrete implementation of [[LServing]] returning the average of all
  * algorithms' predictions. The output prediction class is Double.
  *
  * @group Serving
  */
object LAverageServing {
  /** Returns an instance of [[LAverageServing]]. */
  def apply[Q](a: Class[_ <: BaseAlgorithm[_, _, Q, _]]) =
    classOf[LAverageServing[Q]]
}
