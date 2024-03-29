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

class LocusActor(bamFile: File, nrOfWorkers: Int, bakkaFunction: BakkaLocusFunction) extends BakkaActorSystem(bamFile, nrOfWorkers, bakkaFunction) {

    val init = bakkaFunction.init
    val function = bakkaFunction.function

    // create the master
    val master = system.actorOf(Props(new LocusMaster(bamFile, nrOfWorkers, listener, init, function)),
        name = "master")

    def run(): Unit = {
        // start the calculation
        master ! Parse()
    }
}