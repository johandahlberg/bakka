package molmed.functions

import scala.collection.JavaConversions._
import net.sf.picard.util.SamLocusIterator
import molmed.functions.CountReads.IntResultContainer

object CountLoci extends BakkaLocusFunction {

    def function = countLoci
    def init = new IntResultContainer(0)
    
    def countLoci(locusInfo: SamLocusIterator.LocusInfo): ResultContainer = {
        IntResultContainer(1)
    }

}
