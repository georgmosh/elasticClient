import java.math.RoundingMode;
import java.text.DecimalFormat;

/**
 * Tests.java
 * @author Georgios M. Moschovis (p3150113@aueb.gr)
 */
public class Tests {
    public static void main(String[] args) {
        Tests.test();
    }

    /**
     * DEMONSTRATING PHASE 1: IR CODING TASK
     * A test method for generating initial part's whole procedure.
     */
    public static void test() {
        /*
         * Converting the tzk_collection of XML elements to their JSON equivalents.
         */
        final long startTime = System.nanoTime();
        Conversions tzk_collection = new Conversions("ParsedFiles", "OutputFiles");
        tzk_collection.GenerateSearchData();

        long parsingDuration = System.nanoTime() - startTime;
        DecimalFormat df = new DecimalFormat("#.##");
        df.setRoundingMode(RoundingMode.CEILING);
        System.out.println("Conversion process duration: " + df.format((parsingDuration * 1e-9) )+ " sec.\n\n");

        /*
         * Indexing the collection of JSON equivalent elements to the "documents" Lucene index.
         */
        (new Indexing()).EvaluateSearchData();

        long finishLane = System.nanoTime();
        long indexingDuration =  finishLane - parsingDuration - startTime;
        System.out.println("Indexing process duration: " + df.format((indexingDuration * 1e-9)) + " sec.\n\n");

        /*
         * Genereting search results for the trec_eval metrics to evaluate Retrieval System.
         */
        (new Searching(21, "QueriesFiles", "EvalFiles")).PerformDocsQueries();

        finishLane = System.nanoTime();
        long searchingDuration = finishLane - indexingDuration - startTime;
        System.out.println("Searching process duration: " + df.format((searchingDuration * 1e-9)) + " sec.\n\n");

        long nodalDuration = indexingDuration + parsingDuration + searchingDuration;
        System.out.println("Nodal execution duration: " + df.format((nodalDuration * 1e-9)) + " sec.\n");
    }
}
