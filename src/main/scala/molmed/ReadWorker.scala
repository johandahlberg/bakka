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

class ReadWorker(function: SAMRecord => ResultContainer) extends Actor {

    def receive = {
        case ReadWork(recordBuffer) ⇒
            try {
                for (rec <- recordBuffer) {
                    val res = function(rec) // perform the work
                    sender ! Result(res)
                }
            } catch {
                case e: Exception => sender ! Error(e)
            }
    }
}

