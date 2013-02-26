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
import molmed.functions.BakkaReadFunction
import molmed.Messages._

class ReadActor(file: File, nrOfWorkers: Int, bakkaFunction: BakkaReadFunction) extends BakkaActorSystem(file, nrOfWorkers, bakkaFunction){

    val init = bakkaFunction.init
    val function = bakkaFunction.function

    // create the master
    val master = system.actorOf(Props(new ReadMaster(file, nrOfWorkers, listener, init, function)),
        name = "master")

    /**
     * Start the actor system
     */
    def run(): Unit = {
        // start the calculation
        master ! Parse()
    }
}

