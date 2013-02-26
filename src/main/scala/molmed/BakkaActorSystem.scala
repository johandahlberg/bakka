package molmed

import java.io.File
import scala.collection.JavaConversions._
import akka.actor._
import akka.routing.RoundRobinRouter
import akka.util.duration._
import akka.util.Duration
import net.sf.samtools.SAMFileReader
import net.sf.samtools.SAMRecord
import molmed.Messages._
import molmed.functions.BakkaFunction

class BakkaActorSystem(bamFile: File, nrOfWorkers: Int, bakkaFunction: BakkaFunction) {
    
    // Create an Akka system
    val system = ActorSystem("BamSystem")

    // create the result listener, which will print the result and shutdown the system
    val listener = system.actorOf(Props[Listener], name = "listener")    
}
