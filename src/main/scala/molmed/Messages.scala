package molmed

import net.sf.samtools.SAMRecord
import scala.collection.JavaConversions._
import molmed.functions.ResultContainer
import akka.util.duration._
import akka.util.Duration
import java.io.File
import net.sf.picard.util.SamLocusIterator

object Messages {
    
    sealed trait BamMessage    
    case class Parse() extends BamMessage
    case class ReadWork(recordBuffer: Array[SAMRecord]) extends BamMessage
    case class LocusWork(locusInfo: SamLocusIterator.LocusInfo) extends BamMessage
    case class Result(value: ResultContainer) extends BamMessage
    case class FinalResult[T](value: T, duration: Duration) extends BamMessage
    case class FinisedReading(nbrOfRecords: Int) extends BamMessage
    case class Read(file: File) extends BamMessage
    case class SAMRecordBufferWrapper(rec: Array[SAMRecord]) extends BamMessage
    case class LocusInfoWrapper(locusInfo: SamLocusIterator.LocusInfo) extends BamMessage
    case class RunFinished() extends BamMessage
    case class Error(exception: Exception) extends BamMessage

}