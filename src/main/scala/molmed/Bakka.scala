package molmed

import akka.actor._
import java.io.File
import net.sf.samtools.SAMRecord
import molmed.functions._

import scala.collection.JavaConversions._


object Bakka extends App {

    val testFile = new File("/local/data/gatk_bundle/b37/NA12878.HiSeq.WGS.bwa.cleaned.recal.hg19.20.bam")
    val nrOfWorkers = 8  

    
    import molmed.functions.Flagstat._
    val flagstat = Flagstat

    runActors(testFile, nrOfWorkers, flagstat)

    /**
     * Running the actors system
     */

    def runActors(file: File, nrOfWorkers: Int, bakkaFunction: BakkaFunction) = {

        // Create an Akka system
        val system = ActorSystem("BamSystem")

        // create the result listener, which will print the result and shutdown the system
        val listener = system.actorOf(Props[Listener], name = "listener")

        val init = bakkaFunction.init
        val function = bakkaFunction.function
        
        // create the master
        val master = system.actorOf(Props(new Master[FlagstatResultContainer](file, nrOfWorkers, listener, init, function)),
            name = "master")

        // start the calculation
        master ! Parse()
    }
}