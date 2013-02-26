package molmed

import akka.actor._
import java.io.File
import net.sf.samtools.SAMRecord
import molmed.functions._

import scala.collection.JavaConversions._

object Bakka extends App {

    val testFile = new File("/home/MOLMED/dahljo/workspace/gatk/public/testdata/exampleBAM.bam")
    val nrOfWorkers = 8

    import molmed.functions.Flagstat._
    val flagstat = Flagstat

    //    runActors(testFile, nrOfWorkers, flagstat)

    //import molmed.functions.CountReads._
    //val countReads = CountReads

    import molmed.functions.CountLoci._
    val countLoci = CountLoci

    runActors(testFile, nrOfWorkers, countLoci)

    /**
     * Running the actors system
     */

    def runActors(file: File, nrOfWorkers: Int, bakkaFunction: BakkaFunction) = {
        //val actor = new ReadActor(file, nrOfWorkers, bakkaFunction)
        println("Running LocusActor")
        val locusActor = new LocusActor(file, nrOfWorkers, bakkaFunction.asInstanceOf[BakkaLocusFunction])
        locusActor.run()

        println("Running ReadActor")
        val readActor = new ReadActor(file, nrOfWorkers, flagstat.asInstanceOf[BakkaReadFunction])
        readActor.run()
    }
}