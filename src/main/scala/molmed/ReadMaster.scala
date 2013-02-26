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

 class ReadMaster(file: File, nrOfWorkers: Int, listener: ActorRef, initializer: ResultContainer, function: SAMRecord => ResultContainer)
        extends Master(file, nrOfWorkers, listener, initializer) {

        val workerRouter = context.actorOf(
            Props(new ReadWorker(function)).withRouter(RoundRobinRouter(nrOfWorkers - 1)), name = "workerRouter")

        val readRouter = context.actorOf(
            Props[ReadReader].withRouter(RoundRobinRouter(1)), name = "readRouter")

        def receive = commonReceive orElse {
            case Parse() =>
                readRouter ! Read(file)

            case SAMRecordBufferWrapper(rec) =>
                workerRouter ! ReadWork(rec)
        }
    }