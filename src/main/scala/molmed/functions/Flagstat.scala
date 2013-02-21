package molmed.functions

import net.sf.samtools.SAMRecord
import scala.collection.JavaConversions._

object Flagstat extends BakkaReadFunction{

    def function = flagstat
    def init = new FlagstatResultContainer
    
    case class FlagstatResultContainer(total: (Long, Long) = (0, 0),
        duplicates: (Long, Long) = (0, 0),
        mapped: (Long, Long) = (0, 0),
        paired: (Long, Long) = (0, 0),
        read1: (Long, Long) = (0, 0),
        read2: (Long, Long) = (0, 0),
        properlyPaired: (Long, Long) = (0, 0),
        withItSelfAndMateMapped: (Long, Long) = (0, 0),
        singletons: (Long, Long) = (0, 0),
        withMateMappedOnDifferentChromosome: (Long, Long) = (0, 0),
        withMateMappedToDiffrentChrMapQGreatherThan5: (Long, Long) = (0, 0)) extends ResultContainer {

        def +(x: ResultContainer): ResultContainer = {
            def addTupples(x: (Long, Long), y: (Long, Long)): (Long, Long) = {
                (x._1 + y._1, x._2 + y._2)
            }
            x match {
                case FlagstatResultContainer(tot, dup, map, par, r1, r2, propPaired, withItSelf, single, withMateOnDiffChrom, withGoodMapMateOnDiffChorom) =>
                    new FlagstatResultContainer(addTupples(this.total, tot),
                        addTupples(this.duplicates, dup),
                        addTupples(this.mapped, map),
                        addTupples(this.paired, par),
                        addTupples(this.read1, r1),
                        addTupples(this.read2, r2),
                        addTupples(this.properlyPaired, propPaired),
                        addTupples(this.withItSelfAndMateMapped, withItSelf),
                        addTupples(this.singletons, single),
                        addTupples(this.withMateMappedOnDifferentChromosome, withMateOnDiffChrom),
                        addTupples(this.withMateMappedToDiffrentChrMapQGreatherThan5, withGoodMapMateOnDiffChorom))
                case _ =>
                    throw new IllegalArgumentException
            }
        }

        override def toString(): String = {
            ("\n" +
                "%s  in total \n" +
                "%s duplicates \n" +
                "%s mapped \n" +
                "%s paired in sequencing \n" +
                "%s read1 \n" +
                "%s read2 \n" +
                "%s properly paired \n" +
                "%s with itself and mate mapped \n" +
                "%s singletons \n" +
                "%s with mate mapped to a different chr \n" +
                "%s with mate mapped to a different chr (mapQ>=5) \n")
                .format(total, duplicates, mapped, paired, read1, read2, properlyPaired, withItSelfAndMateMapped, singletons, withMateMappedOnDifferentChromosome, withMateMappedToDiffrentChrMapQGreatherThan5)
        }
    }
    
     /**
     * Function to replicate samtools flagstat
     */
    def flagstat(rec: SAMRecord): ResultContainer = {

        if (rec == null)
            FlagstatResultContainer()

        val failsVendorQualCheck: Boolean = rec.getReadFailsVendorQualityCheckFlag()
        def checkFlags(flag: Boolean): (Long, Long) = {
            if (flag && !failsVendorQualCheck)
                (1, 0)
            else if (flag && failsVendorQualCheck)
                (0, 1)
            else
                (0, 0)
        }

        val records = checkFlags(true)
        val duplicate = checkFlags(rec.getDuplicateReadFlag())
        val mapped = checkFlags(!rec.getReadUnmappedFlag())
        val paired = checkFlags(rec.getReadPairedFlag())
        val read1 = checkFlags(rec.getFirstOfPairFlag())
        val read2 = checkFlags(rec.getSecondOfPairFlag())
        val properlyPaired = checkFlags(rec.getProperPairFlag())
        val withIfSelfAndMateMapped = checkFlags(!rec.getReadUnmappedFlag() && !rec.getMateUnmappedFlag())
        val singelton = checkFlags(!rec.getReadUnmappedFlag() && rec.getMateUnmappedFlag())
        def mateOnOtherChromosome = !rec.getMateUnmappedFlag() && rec.getMateReferenceName() != null && rec.getMateReferenceName() != rec.getReferenceName()
        val mateMappedOnDifferentChromosome = checkFlags(mateOnOtherChromosome)
        val mateMappedOnDifferentChromosomeWithHigmMapQ = checkFlags(mateOnOtherChromosome && rec.getMappingQuality() != 255 && rec.getMappingQuality() >= 5)

        FlagstatResultContainer(records, duplicate, mapped, paired, read1, read2, properlyPaired, withIfSelfAndMateMapped, singelton, mateMappedOnDifferentChromosome, mateMappedOnDifferentChromosomeWithHigmMapQ)
    }

}