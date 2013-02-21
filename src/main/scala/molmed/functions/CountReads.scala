package molmed.functions

import net.sf.samtools.SAMRecord
import scala.collection.JavaConversions._

object CountReads extends BakkaReadFunction {

    def function = countRecords
    def init = new IntResultContainer(0)
    
    case class IntResultContainer(i: Int) extends ResultContainer {
        def +(x: ResultContainer): ResultContainer = {
            x match {
                case IntResultContainer(value) =>
                    new IntResultContainer(this.i + value)
            }
        }
        override def toString(): String = i.toString()
    }

    def countRecords(rec: SAMRecord): ResultContainer = {
        IntResultContainer(1)
    }

}
