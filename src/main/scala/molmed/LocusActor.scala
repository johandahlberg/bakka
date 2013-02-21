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

class LocusActor(bamFile: File, nrOfWorkers: Int, bakkaFunction: BakkaLocusFunction) {

    // Create an Akka system
    val system = ActorSystem("BamSystem")

    // create the result listener, which will print the result and shutdown the system
    val listener = system.actorOf(Props[Listener], name = "listener")

    val init = bakkaFunction.init
    val function = bakkaFunction.function

    // create the master
    val master = system.actorOf(Props(new Master[ResultContainer](bamFile, nrOfWorkers, listener, init, function)),
        name = "master")

    def run(): Unit = {
        // start the calculation
        master ! Parse()
    }

    class Worker[T](function: SamLocusIterator.LocusInfo => ResultContainer) extends Actor {

        def receive = {
            case LocusWork(locusInfo) ⇒
                println("Worker recived LocusWork")
                sender ! Result(function(locusInfo))
        }
    }

    class Master[T](file: File, nrOfWorkers: Int, listener: ActorRef, initializer: ResultContainer, function: SamLocusIterator.LocusInfo => ResultContainer)
        extends Actor {

        var nbrOfLociToProcess: Int = -1
        var lociProcessed: Int = 0
        var result: ResultContainer = initializer
        val start: Long = System.currentTimeMillis

        val workerRouter = context.actorOf(
            Props(new Worker[T](function)).withRouter(RoundRobinRouter(nrOfWorkers - 1)), name = "workerRouter")

        val readRouter = context.actorOf(
            Props[LocusReader].withRouter(RoundRobinRouter(1)), name = "readRouter")

        def receive = {
            case Parse() =>
                println("Parse message")
                readRouter ! Read(file)

            case LocusInfoWrapper(locusInfo) =>
                println("LocusInfoWrapper message")
                workerRouter ! LocusWork(locusInfo)

            case Result(value) => {
                println("Result message")
                result += value
                lociProcessed += 1
                if (isRunFinished) self ! RunFinished()
            }
            case FinisedReading(nbrOfRecords) =>
                println("FinishedReading message")
                this.nbrOfLociToProcess = nbrOfRecords
                println("nbrOfLociToProcess = " + this.nbrOfLociToProcess)
                println("lociProcessed = " + this.lociProcessed)
                if (isRunFinished) self ! RunFinished()

            case RunFinished() =>
                println("FinishedReading message")
                println("nbrOfLociToProcess = " + this.nbrOfLociToProcess)
                println("lociProcessed = " + this.lociProcessed)
                // Send the result to the listener
                listener ! FinalResult(result, duration = (System.currentTimeMillis - start).millis)
                // Stops this actor and all its supervised children
                context.stop(self)

            case Error(e) => listener ! e

        }

        def isRunFinished: Boolean = nbrOfLociToProcess != -1 && lociProcessed == nbrOfLociToProcess

    }

}