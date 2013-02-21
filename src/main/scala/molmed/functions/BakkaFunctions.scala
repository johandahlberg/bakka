package molmed.functions

import akka.actor._
import net.sf.samtools.SAMRecord
import scala.collection.JavaConversions._
import net.sf.picard.util.SamLocusIterator

trait ResultContainer {
    def +(that: ResultContainer): ResultContainer
}

trait BakkaFunction {
    def init: ResultContainer
}

trait BakkaReadFunction extends BakkaFunction {
    def function: SAMRecord => ResultContainer
}

trait BakkaLocusFunction extends BakkaFunction {
    def function: SamLocusIterator.LocusInfo => ResultContainer
}