package molmed

import java.io.File
import scala.collection.JavaConversions._
import akka.actor._
import akka.routing.RoundRobinRouter
import akka.util.duration._
import akka.util.Duration
import net.sf.samtools.SAMFileReader
import net.sf.samtools.SAMRecord
import molmed.functions.ResultContainer
import molmed.functions.BakkaLocusFunction
import molmed.Messages._
import molmed.functions.CountReads.IntResultContainer
import net.sf.picard.util.SamLocusIterator

class LocusWorker(function: SamLocusIterator.LocusInfo => ResultContainer) extends Actor {

    def receive = {
        case LocusWork(locusInfo) ⇒ {
            try {
                sender ! Result(function(locusInfo))
            } catch {
                case e: Exception => sender ! Error(e)
            }
        }

    }
}