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

abstract class Master(file: File, nrOfWorkers: Int, listener: ActorRef, initializer: ResultContainer) extends Actor {

    val readRouter = context.actorOf(
        Props[LocusReader].withRouter(RoundRobinRouter(1)), name = "readRouter")

    // Records is used in the widest terms of the word here and might be reads loci, etc depending on the 
    // implementing Master type 
    var nbrOfRecordsToProcess: Int = -1
    var recordsProcessed: Int = 0
    var result: ResultContainer = initializer
    val start: Long = System.currentTimeMillis

    def isRunFinished: Boolean = nbrOfRecordsToProcess != -1 && recordsProcessed == nbrOfRecordsToProcess

    /**
     * Used along with the implementation classes to provied common methods, such as reading a file
     */
    def commonReceive: PartialFunction[Any, Unit] = {
        case Parse() =>
            readRouter ! Read(file)

        case Result(value) => {
            result += value
            recordsProcessed += 1
            if (isRunFinished) self ! RunFinished()
        }

        case FinisedReading(nbrOfRecords) =>
            this.nbrOfRecordsToProcess = nbrOfRecords
            if (isRunFinished) self ! RunFinished()

        case RunFinished() =>
            // Send the result to the listener
            listener ! FinalResult(result, duration = (System.currentTimeMillis - start).millis)
            // Stops this actor and all its supervised children
            context.stop(self)

        case Error(e) => listener ! e
    }

}