/*
 * Copyright 2017 Azavea
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

package geotrellis.spark.io.cog

import geotrellis.raster.CellGrid
import geotrellis.spark._
import geotrellis.spark.io._
import geotrellis.util._

import org.apache.spark.rdd._
import spray.json._

import scala.reflect._

abstract class FilteringCOGLayerReader[ID] extends COGLayerReader[ID] {

  /** read
    *
    * This function will read an RDD layer based on a query.
    *
    * @param id              The ID of the layer to be read
    * @param rasterQuery     The query that will specify the filter for this read.
    * @param numPartitions   The desired number of partitions in the resulting RDD.
    * @param indexFilterOnly If true, the reader should only filter out elements who's KeyIndex entries
    *                        do not match the indexes of the query key bounds. This can include keys that
    *                        are not inside the query key bounds.
    * @tparam K              Type of RDD Key (ex: SpatialKey)
    * @tparam V              Type of RDD Value (ex: Tile or MultibandTile )
    */
  def read[
    K: SpatialComponent: Boundable: JsonFormat: ClassTag,
    V <: CellGrid: COGRDDReader: ClassTag
  ](id: ID, rasterQuery: LayerQuery[K, TileLayerMetadata[K]], numPartitions: Int, indexFilterOnly: Boolean): RDD[(K, V)] with Metadata[TileLayerMetadata[K]]

  def read[
    K: SpatialComponent: Boundable: JsonFormat: ClassTag,
    V <: CellGrid: COGRDDReader: ClassTag
  ](id: ID, rasterQuery: LayerQuery[K, TileLayerMetadata[K]], numPartitions: Int): RDD[(K, V)] with Metadata[TileLayerMetadata[K]] =
    read(id, rasterQuery, numPartitions, false)

  def read[
    K: SpatialComponent: Boundable: JsonFormat: ClassTag,
    V <: CellGrid: COGRDDReader: ClassTag
  ](id: ID, rasterQuery: LayerQuery[K, TileLayerMetadata[K]]): RDD[(K, V)] with Metadata[TileLayerMetadata[K]] =
    read(id, rasterQuery, defaultNumPartitions)

  def read[
    K: SpatialComponent: Boundable: JsonFormat: ClassTag,
    V <: CellGrid: COGRDDReader: ClassTag
  ](id: ID, numPartitions: Int): RDD[(K, V)] with Metadata[TileLayerMetadata[K]] =
    read(id, new LayerQuery[K, TileLayerMetadata[K]], numPartitions)

  def query[
    K: SpatialComponent: Boundable: JsonFormat: ClassTag,
    V <: CellGrid: COGRDDReader: ClassTag
  ](layerId: ID): BoundLayerQuery[K, TileLayerMetadata[K], RDD[(K, V)] with Metadata[TileLayerMetadata[K]]] =
    new BoundLayerQuery(new LayerQuery, read(layerId, _))

  def query[
    K: SpatialComponent: Boundable: JsonFormat: ClassTag,
    V <: CellGrid: COGRDDReader: ClassTag
  ](layerId: ID, numPartitions: Int): BoundLayerQuery[K, TileLayerMetadata[K], RDD[(K, V)] with Metadata[TileLayerMetadata[K]]] =
    new BoundLayerQuery(new LayerQuery, read(layerId, _, numPartitions))
}
