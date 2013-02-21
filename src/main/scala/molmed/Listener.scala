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

class Listener extends Actor {
    def receive = {
        case FinalResult(value, duration) ⇒
            println("Listener recived FinalResult message")
            println(value)
            println("Runtime: %s"
                .format(duration))
            context.system.shutdown()

        case Error(e) =>
            println("Recived exception of type: " + e.getClass() + ". Will abort. \n Stacktrace: \n")
            e.printStackTrace()
            context.system.shutdown()

    }
}