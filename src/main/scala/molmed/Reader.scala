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
import net.sf.picard.util.SamLocusIterator
import net.sf.samtools.SAMFileHeader.SortOrder
import net.sf.samtools.SAMFileHeader

trait Reader extends Actor {

    def assertSortOrder(bamFileHeader: SAMFileHeader, sortOrder: SortOrder): Boolean = {
        val actual = bamFileHeader.getSortOrder()
        if (actual.equals(sortOrder)) {
            true
        } else {
            sender ! Error(new Exception("Wrong sort order, required: " + sortOrder.toString() + " found: " + actual))
            false
        }

    }

    def readFile(file: File): Unit

    def receive = {
        case Read(file) =>
            readFile(file)
    }

}

// Reads a bam file horizontally
// Sends Array[SAMRecord] back the the sender
class ReadReader extends Reader {

    def readFile(file: File): Unit = {
        // False, makes sure there's decoding here. Better that the read is decoded when used.
        val fileReader = new SAMFileReader(file, false);
        val iterator = fileReader.iterator()
        var nbrOfRecords = 0
        val bufferSize = 1000
        var buffer = new Array[SAMRecord](bufferSize)

        var counter = 0
        try {
            for (rec <- iterator) {
                nbrOfRecords += 1
                buffer(counter) = rec
                counter += 1
                if (counter == buffer.length) {
                    sender ! SAMRecordBufferWrapper(buffer)
                    counter = 0
                    buffer = new Array[SAMRecord](bufferSize)
                }
            }
        } finally {
            if (!buffer.isEmpty)
                sender ! SAMRecordBufferWrapper(buffer)
        }
        sender ! FinisedReading(nbrOfRecords)
    }
}

class LocusReader extends Reader {

    def readFile(bamFile: File): Unit = {

        var nbrOfLoci = 0
        // False, makes sure there's decoding here. Better that the read is decoded when used.
        val fileReader = new SAMFileReader(bamFile, false);
        val bamHeader = fileReader.getFileHeader()
        if (assertSortOrder(bamHeader, SortOrder.coordinate)) {
            val samLocusIterator: SamLocusIterator = new SamLocusIterator(fileReader)
            samLocusIterator.setEmitUncoveredLoci(false)
            val iterator = samLocusIterator.iterator()
            try {
                for (locus <- iterator) {
                    nbrOfLoci += 1
                    sender ! LocusInfoWrapper(locus)
                }
            }
            sender ! FinisedReading(nbrOfLoci)
        }
    }

}