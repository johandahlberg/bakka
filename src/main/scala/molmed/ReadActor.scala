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
import molmed.functions.BakkaFunction
import molmed.Messages._

class ReadActor(file: File, nrOfWorkers: Int, bakkaFunction: BakkaFunction) {

    // Create an Akka system
    val system = ActorSystem("BamSystem")

    // create the result listener, which will print the result and shutdown the system
    val listener = system.actorOf(Props[Listener], name = "listener")

    val init = bakkaFunction.init
    val function = bakkaFunction.function

    // create the master
    val master = system.actorOf(Props(new Master[ResultContainer](file, nrOfWorkers, listener, init, function)),
        name = "master")

    def run(): Unit = {
        // start the calculation
        master ! Parse()
    }
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
}

