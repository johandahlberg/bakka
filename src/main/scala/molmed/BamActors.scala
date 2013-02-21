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

sealed trait BamMessage
case class Parse() extends BamMessage
case class Work(recordBuffer: Array[SAMRecord]) extends BamMessage
case class Result(value: ResultContainer) extends BamMessage
case class FinalResult[T](value: T, duration: Duration) extends BamMessage
case class FinisedReading(nbrOfRecords: Int) extends BamMessage
case class Read(file: File) extends BamMessage
case class SAMRecordBufferWrapper(rec: Array[SAMRecord]) extends BamMessage
case class RunFinished() extends BamMessage
case class Error(exception: Exception) extends BamMessage

class Worker[T](function: SAMRecord => ResultContainer) extends Actor {

    def receive = {
        case Work(recordBuffer) ⇒
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

class Master[T](file: File, nrOfWorkers: Int, listener: ActorRef, initializer: ResultContainer, function: SAMRecord => ResultContainer)
    extends Actor {

    var nbrOfRecordsToProcess: Int = -1
    var recordsProcessed: Int = 0
    var result: ResultContainer = initializer
    val start: Long = System.currentTimeMillis

    val workerRouter = context.actorOf(
        Props(new Worker[T](function)).withRouter(RoundRobinRouter(nrOfWorkers - 1)), name = "workerRouter")

    val readRouter = context.actorOf(
        Props[ReadReader].withRouter(RoundRobinRouter(1)), name = "readRouter")

    def receive = {
        case Parse() =>
            readRouter ! Read(file)

        case SAMRecordBufferWrapper(rec) =>
            workerRouter ! Work(rec)

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

    def isRunFinished: Boolean = nbrOfRecordsToProcess != -1 && recordsProcessed == nbrOfRecordsToProcess

}
