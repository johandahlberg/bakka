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

class LocusMaster(file: File, nrOfWorkers: Int, listener: ActorRef, initializer: ResultContainer, function: SamLocusIterator.LocusInfo => ResultContainer)
    extends Master(file, nrOfWorkers, listener, initializer) {

    val workerRouter = context.actorOf(
        Props(new LocusWorker(function)).withRouter(RoundRobinRouter(nrOfWorkers - 1)), name = "workerRouter")

    def receive() = commonReceive orElse {
        case LocusInfoWrapper(locusInfo) =>
            workerRouter ! LocusWork(locusInfo)        
    }

}