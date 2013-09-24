package geotrellis.raster

import geotrellis._
import geotrellis.raster.op._
import geotrellis.SourceBuilder
import geotrellis.logic.Collect

import geotrellis.statistics.Histogram

import geotrellis.SingleDataSource._

object RasterSource {
  implicit def canBuildSourceFrom: CanBuildSourceFrom[RasterSource, Raster, RasterSource] =
    new CanBuildSourceFrom[RasterSource, Raster, RasterSource] {
      def apply() = new RasterSourceBuilder
      def apply(rasterSrc:RasterSource) =
        RasterSourceBuilder(rasterSrc)
  }
  
  implicit def canBuildSourceFromHistogram:CanBuildSourceFrom[RasterSource,Histogram,SingleDataSource[Histogram,Histogram]] =
    new CanBuildSourceFrom[RasterSource,
                           Histogram,
                           SingleDataSource[Histogram,Histogram]] {
      def apply() = new SingleDataSourceBuilder[Histogram,Histogram]
      def apply(src:RasterSource) = new SingleDataSourceBuilder[Histogram,Histogram]
    }
 
 
  def apply(name:String):RasterSource =
    new RasterSource(
      io.LoadRasterLayerInfo(name).map { info =>
        RasterDefinition(
          info.rasterExtent,
          info.tileLayout,
          (for(tileCol <- 0 until info.tileLayout.tileCols;
            tileRow <- 0 until info.tileLayout.tileRows) yield {
            io.LoadTile(name,tileCol,tileRow)
          }).toSeq
        )
      }
    )
}

class RasterSource(val rasterDef: Op[RasterDefinition]) extends  RasterSourceLike[RasterSource] {
  def partitions = rasterDef.map(_.tiles)
  val rasterDefinition = rasterDef
}

object Foo {
  def main() = {
    val d1 = new RasterSource(null)
    val d2: RasterSource = d1.map(local.Add(_, 3))
    val d3: RasterSource = d1.localAdd(3)
    val d4: LocalRasterSource = d1.converge

    val l1 = new LocalRasterSource(null)
    val l2: LocalRasterSource = l1.map(local.Add(_, 3))

    val l3: LocalRasterSource = l1.localAdd(2)
  }
}