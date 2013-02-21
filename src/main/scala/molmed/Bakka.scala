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
        val actor = new ReadActor(file, nrOfWorkers, bakkaFunction)
        actor.run()
    }
}