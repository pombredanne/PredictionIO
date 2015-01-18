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

/** Base trait for all kinds of parameters that will be passed to constructors
  * of different controller classes.
  *
  * @group General
  */
trait Params extends Serializable {}

/** Mix in this trait for parameters that contain app ID. Only engines that
  * take data source parameters with this trait would be able to use the
  * feedback loop.
  *
  * @group General
  */
trait ParamsWithAppId extends Serializable {
  val appId: Int
}

/** A concrete implementation of [[Params]] representing empty parameters.
  *
  * @group General
  */
case class EmptyParams() extends Params {
  override def toString(): String = "Empty"
}
