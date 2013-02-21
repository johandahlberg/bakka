package molmed.functions


import akka.actor._
import net.sf.samtools.SAMRecord
import scala.collection.JavaConversions._

trait ResultContainer {
    def +(that: ResultContainer): ResultContainer
}

trait BakkaFunction {
    def function: SAMRecord => ResultContainer
    def init: ResultContainer
}