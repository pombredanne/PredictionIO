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

package io.prediction.engines.itemrank

import io.prediction.controller.EmptyParams
import io.prediction.controller.EngineParams
import io.prediction.controller.Workflow
import io.prediction.controller.WorkflowParams

import io.prediction.engines.base.AttributeNames
import io.prediction.engines.base.EventsSlidingEvalParams
import io.prediction.engines.base.BinaryRatingParams

import com.github.nscala_time.time.Imports._

object Runner {

  def main(args: Array[String]) {

    val dsp = EventsDataSourceParams(
      appId = 9,
      itypes = None,
      actions = Set("view", "like", "dislike", "conversion", "rate"),
      startTime = None,
      untilTime = None,
      attributeNames = AttributeNames(
        user = "pio_user",
        item = "pio_item",
        u2iActions = Set("view", "like", "dislike", "conversion", "rate"),
        itypes = "pio_itypes",
        starttime = "pio_starttime",
        endtime = "pio_endtime",
        inactive = "pio_inactive",
        rating = "pio_rating"
      ),
      slidingEval = Some(new EventsSlidingEvalParams(
        firstTrainingUntilTime = new DateTime(1998, 2, 1, 0, 0),
        evalDuration = Duration.standardDays(7),
        evalCount = 8))
    )


    val mp = new DetailedEvaluatorParams(
      optOutputPath = None,
      buckets = 3,
      ratingParams = new BinaryRatingParams(
        actionsMap = Map(
          "view" -> Some(3),
          "like" -> Some(5),
          "conversion" -> Some(4),
          "rate" -> None
        ),
        goodThreshold = 3),
      measureType = MeasureType.PrecisionAtK,
      measureK = 10
    )

    val pp = new PreparatorParams(
      actions = Map(
        "view" -> Some(3),
        "like" -> Some(5),
        "conversion" -> Some(4),
        "rate" -> None
      ),
      conflict = "latest"
    )

    val randomAlgoParams = new RandomAlgoParams()
    val mahoutAlgoParams = new mahout.ItemBasedAlgoParams(
      booleanData = true,
      itemSimilarity = "LogLikelihoodSimilarity",
      weighted = false,
      nearestN = 10,
      threshold = 4.9E-324,
      numSimilarItems = 50,
      numUserActions = 50,
      freshness = 0,
      freshnessTimeUnit = 86400,
      recommendationTime = Some(DateTime.now.millis)
    )
    val ncMahoutAlgoParams = new NCItemBasedAlgorithmParams(
      booleanData = true,
      itemSimilarity = "LogLikelihoodSimilarity",
      weighted = false,
      threshold = Double.MinPositiveValue,
      nearestN = 10,
      freshness = 0,
      freshnessTimeUnit = 86400,
      recommendationTime = Some(DateTime.now.millis)
    )

    val sp = new EmptyParams()

    val engine = ItemRankEngine()
    val engineParams = EngineParams(
      dataSourceParams = dsp,
      preparatorParams = pp,
      //algorithmParamsList = Seq(("mahoutItemBased", mahoutAlgoParams)),
      algorithmParamsList = Seq(
        ("mahoutItemBased", mahoutAlgoParams),
        ("ncMahoutItemBased", ncMahoutAlgoParams)),
      // Seq(("rand", randomAlgoParams))
      // Seq(("mahoutItemBased", mahoutAlgoParams))
      // Seq(("ncMahoutItemBased", ncMahoutAlgoParams))
      servingParams = sp
    )

    Workflow.runEngine(
      params = WorkflowParams(
        batch = "Imagine: Local ItemRank Engine",
        verbose = 0),
      engine = engine,
      engineParams = engineParams,
      //metricsClassOpt = Some(classOf[ItemRankEvaluator]),
      //metricsParams = mp
      evaluatorClassOpt = Some(classOf[ItemRankDetailedEvaluator]),
      evaluatorParams = mp
    )

  }

}
