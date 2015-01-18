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

package io.prediction.data.examples

import io.prediction.data.storage.Event
import io.prediction.data.storage.StorageClientConfig
import io.prediction.data.storage.hbase.HBLEvents
import io.prediction.data.storage.hbase.StorageClient

import scala.concurrent.ExecutionContext.Implicits.global

object HBLEventsTest {
  def main(args: Array[String]) {
    val appId = args(0).toInt
    val eventDb = new HBLEvents(
      new StorageClient(new StorageClientConfig(Seq(), Seq(), false)).client,
      "predictionio_eventdata")
    eventDb.aggregateProperties(appId, "user").right.get.foreach(println(_))
  }
}
