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

import io.prediction.controller.Engine
import io.prediction.controller.Params
import io.prediction.controller.EngineParams
import io.prediction.controller.IEngineFactory

import java.lang.{ Iterable => JIterable }
import java.util.{ Map => JMap }

import scala.collection.JavaConversions._

/**
 * This class chains up the entire data process. PredictionIO uses this
 * information to create workflows and deployments. In Java, use
 * JavaEngineBuilder to conveniently instantiate an instance of this class.
 *
 * @param <TD> Training Data
 * @param <EI> Evaluation Info
 * @param <PD> Prepared Data
 * @param <Q> Input Query
 * @param <P> Output Prediction
 * @param <A> Actual Value
 */
class JavaEngine[TD, EI, PD, Q, P, A](
    dataSourceClass: Class[_ <: LJavaDataSource[TD, EI, Q, A]],
    preparatorClass: Class[_ <: LJavaPreparator[TD, PD]],
    algorithmClassMap: JMap[String, Class[_ <: LJavaAlgorithm[PD, _, Q, P]]],
    servingClass: Class[_ <: LJavaServing[Q, P]]
) extends Engine(
    dataSourceClass,
    preparatorClass,
    Map(algorithmClassMap.toSeq: _*),
    servingClass)

/**
 * This class serves as a logical grouping of all required engine's parameters.
 */
class JavaEngineParams(
    dataSourceParams: Params,
    preparatorParams: Params,
    algorithmParamsList: JIterable[(String, Params)],
    servingParams: Params
) extends EngineParams(
    ("", dataSourceParams),
    ("", preparatorParams),
    algorithmParamsList.toSeq,
    ("", servingParams))

/**
 * JavaSimpleEngine has only one algorithm, and uses default preparator and
 * serving layer. Current default preparator is IdentityPreparator and serving
 * is FirstServing.
 */
class JavaSimpleEngine[TD, EI, Q, P, A](
    dataSourceClass: Class[_ <: LJavaDataSource[TD, EI, Q, A]],
    preparatorClass: Class[_ <: LJavaPreparator[TD, TD]],
    algorithmClassMap
      : JMap[String, Class[_ <: LJavaAlgorithm[TD, _, Q, P]]],
    servingClass: Class[_ <: LJavaServing[Q, P]]
) extends JavaEngine[TD, EI, TD, Q, P, A](
    dataSourceClass,
    preparatorClass,
    Map(algorithmClassMap.toSeq: _*),
    servingClass)

/** If you intend to let PredictionIO create workflow and deploy serving
  * automatically, you will need to implement an object that extends this trait
  * and return an [[Engine]].
  */
trait IJavaEngineFactory extends IEngineFactory {
  /** Creates an instance of an [[Engine]]. */
  def apply(): Engine[_, _, _, _, _, _]
}
