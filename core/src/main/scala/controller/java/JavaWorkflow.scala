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

package io.prediction.controller.java;

import io.prediction.workflow.JavaCoreWorkflow;
import io.prediction.controller.Engine
import io.prediction.controller.EngineParams
import io.prediction.controller.WorkflowParams
import io.prediction.core.BaseEvaluator
import io.prediction.controller.Params
import scala.collection.JavaConversions._

/** Collection of workflow creation methods. */
object JavaWorkflow {
  /** Creates a workflow that runs an engine.
    *
    * @tparam EI Evaluation Info class.
    * @tparam TD Training data class.
    * @tparam PD Prepared data class.
    * @tparam Q Input query class.
    * @tparam P Output prediction class.
    * @tparam A Actual value class.
    *
    * @param engine An instance of [[Engine]].
    * @param engineParams Engine parameters.
    * @param params Workflow parameters.
    */
  def runEngine[EI, TD, PD, Q, P, A](
    engine: Engine[TD, EI, PD, Q, P, A],
    engineParams: EngineParams,
    params: WorkflowParams
  ) {
    runEngine(
      engine = engine,
      engineParams = engineParams,
      evaluatorClass = null,
      evaluatorParams = null,
      params = params
    )
  }

  /** Creates a workflow that runs an engine.
    *
    * @tparam EI Evaluation Info class.
    * @tparam TD Training data class.
    * @tparam PD Prepared data class.
    * @tparam Q Input query class.
    * @tparam P Output prediction class.
    * @tparam A Actual value class.
    *
    * @param engine An instance of [[Engine]].
    * @param engineParams Engine parameters.
    * @param evaluatorClass Evaluator class.
    * @param evaluatorParams Evaluator parameters.
    * @param params Workflow parameters.
    */
  def runEngine[EI, TD, PD, Q, P, A, MU, MR, MMR <: AnyRef](
      engine: Engine[TD, EI, PD, Q, P, A],
      engineParams: EngineParams,
      evaluatorClass
        : Class[_ <: BaseEvaluator[EI, Q, P, A, MU, MR, MMR]],
      evaluatorParams: Params,
      params: WorkflowParams
    ) {
    JavaCoreWorkflow.run(
      dataSourceClassMap = engine.dataSourceClassMap,
      dataSourceParams = engineParams.dataSourceParams,
      preparatorClassMap = engine.preparatorClassMap,
      preparatorParams = engineParams.preparatorParams,
      algorithmClassMap = engine.algorithmClassMap,
      algorithmParamsList = engineParams.algorithmParamsList,
      servingClassMap = engine.servingClassMap,
      servingParams = engineParams.servingParams,
      evaluatorClass = evaluatorClass,
      evaluatorParams = evaluatorParams,
      params = params
    )
  }
}
