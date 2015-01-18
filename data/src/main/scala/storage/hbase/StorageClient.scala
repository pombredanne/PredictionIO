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

package io.prediction.data.storage.hbase

import io.prediction.data.storage.BaseStorageClient
import io.prediction.data.storage.StorageClientConfig

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.hbase.HBaseConfiguration
import org.apache.hadoop.hbase.client.HConnectionManager
import org.apache.hadoop.hbase.client.HConnection
import org.apache.hadoop.hbase.client.HBaseAdmin
//import org.apache.hadoop.hbase.NamespaceDescriptor
//import org.apache.hadoop.hbase.NamespaceExistException

case class HBClient(
  val conf: Configuration,
  val connection: HConnection,
  val admin: HBaseAdmin
)

class StorageClient(val config: StorageClientConfig)
  extends BaseStorageClient {

  val conf = HBaseConfiguration.create()

  if (config.test) {
    // use fewer retries and shorter timeout for test mode
    conf.set("hbase.client.retries.number", "1")
    conf.set("zookeeper.session.timeout", "30000");
    conf.set("zookeeper.recovery.retry", "1")
  }

  if (!config.parallel)
    HBaseAdmin.checkHBaseAvailable(conf)

  val connection =
    if (!config.parallel)
      HConnectionManager.createConnection(conf)
    else
      null

  val client =
    if (!config.parallel)
      HBClient(
        conf = conf,
        connection = connection,
        admin = new HBaseAdmin(connection)
      )
    else
      null
/*
  private val namespace = "predictionio_appdata"

  val nameDesc = NamespaceDescriptor.create(namespace).build()

  try {
    client.admin.createNamespace(nameDesc)
  } catch {
    case e: NamespaceExistException => Unit
    case e: Exception => throw new RuntimeException(e)
  }

  val eventClient = new HBEvents(client, namespace)
*/

  override
  val prefix = "HB"
}
