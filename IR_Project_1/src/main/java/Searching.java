 import org.elasticsearch.action.search.SearchRequest;
 import org.elasticsearch.action.search.SearchResponse;
 import org.elasticsearch.action.admin.indices.analyze.AnalyzeRequest;
 import org.elasticsearch.action.admin.indices.analyze.AnalyzeResponse;
 import org.elasticsearch.index.query.QueryStringQueryBuilder;
 import org.elasticsearch.search.builder.SearchSourceBuilder;
 import org.elasticsearch.client.RequestOptions;
 import org.elasticsearch.search.SearchHits;
 import org.elasticsearch.search.SearchHit;

 import org.apache.logging.log4j.*;

 import java.io.*;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.Set;

 /**
 * Searching.java
 * A class searching a collection of scientific documents to the "documents" Lucene index.
 * Actually makes use of an ElasticSearch client (https://www.elastic.co/guide/en/elasticsearch/client/java-api/current/index.html).
 * @author Georgios M. Moschovis (p3150113@aueb.gr)
 */
public class Searching {
    private ArrayList<String> Query_data_filenames;
    private String inputDataRepository, outputDataRepository, my_results_file;
    private int kappa;

    /**
     * Overloaded Constructor.
     * @param kappa The amount of indexed documents for the searching the "documents" index to retrieve.
     * @param inputRepository The name of the directory to find queries' contained filepaths.
     * @param outputRepository The name of the directory to write search results.
     */
    public Searching(int kappa, String inputRepository, String outputRepository) {
        this.kappa = kappa;
        this.inputDataRepository = inputRepository;
        this.outputDataRepository = outputRepository;
        CollectionDAO.InitializeContainerQuery();
        my_results_file = "";
    }

    /**
     * A method detecting and writing down all filepaths contained in a given directory.
     */
    public void PerformDocsQueries() {
        Query_data_filenames = FileManager.listAllFiles(".\\" + inputDataRepository);

        /*
         * Searching for the above listed files; to perform analysis.
         */
        int numOfTargetFiles = Query_data_filenames.size();
        for(int file = 0; file < numOfTargetFiles; file++) {
            ReadTargetQuery(inputDataRepository + "/" + Query_data_filenames.get(file));
        }
        AnalyzeDocsToQuery();
        PerformQueries();

        /*
         * Closing the communication stream to the client.
         */
        CollectionDAO.ShutClient();
    }

    /**
     * A method reading a specific query file and storing its original content.
     * @param filePath The relative path of the query file.
     */
    public void ReadTargetQuery(String filePath) {
        BufferedReader br = null;
        String sCurrentLine, contents = "";
        try {
            br = new BufferedReader(new FileReader(filePath));
            while ((sCurrentLine = br.readLine()) != null) {
                sCurrentLine = sCurrentLine.trim().replaceAll(" +", " "); // unify whitespaces
                contents += (sCurrentLine + "\n");
            }
            String[] filePathSplit = filePath.split("/");
            filePathSplit = filePathSplit[filePathSplit.length - 1].split("\\.");
            CollectionDAO.GetQueries().add(new Vec2<String, String>(filePathSplit[0], contents));
        } catch (IOException exc) {
            exc.printStackTrace();
        }
    }

    /**
     * A method analyzing all query data documents; the same way with the original indexed context. The English analyzer is really just Lucene¢s EnglishAnalyzer.
     * As a built-in language analyzers are available globally and need not be configured before being used. Lucene EnglishAnalyzer class uses following components:
     *   TOKENIZER: Standard tokenizer
     *   TOKEN FILTERS:
     *      a) Standard token filter
     *      b) English possessive filter, which removes trailings from words
     *      c) Lowercase token filter
     *      d) Stop token filter
     *      e) Keyword marker filter, which protects certain tokens from modification by stemmers
     *      f) Porter stemmer filter, which reduces words down to a base form (?stem?)
     */
    public void AnalyzeDocsToQuery() {
        CollectionDAO.InitializeContainerQAnalyzer();
        for (int doc = 0; doc < CollectionDAO.GetQueries().size(); doc++) {
            try {
                AnalyzeRequest request = new AnalyzeRequest();
                request.text(CollectionDAO.GetQueries().get(doc).getYValue());
                request.analyzer("english");

                /*
                 * Restoring the analyzed query context to the DAO for searching.
                 */
                String analyzedText = "";
                AnalyzeResponse response = CollectionDAO.GetClient().indices().analyze(request, RequestOptions.DEFAULT);
                for(int token = 0; token < response.getTokens().size(); token++)
                    analyzedText += (response.getTokens().get(token).getTerm() + " ");
                analyzedText.trim(); // Eliminating whitespace at the end of the analyzed element.
                CollectionDAO.GetQueriesAnalyzed().add(new Vec2<String, String>(CollectionDAO.GetQueries().get(doc).getTValue(), analyzedText));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * A method querying foreach input file; the content elements of "documents" index.
     * The SearchSourceBuilder, actually contains the options in the search request body of the Rest API.
     * These are propagated to rhe SearchRequest; used for searching documents.
     */
    public void PerformQueries() {
        try {
            for (int doc = 0; doc < CollectionDAO.GetQueries().size(); doc++) {
                /*
                 * Creating a search request to Lucene; to perform searching in the "documents" index.
                 */
                SearchRequest searchRequest = new SearchRequest("documents");
                SearchSourceBuilder sourceBuilder = new SearchSourceBuilder(); // Adding the search options to the SearchRequest.
                QueryStringQueryBuilder queryBuilder = new QueryStringQueryBuilder(CollectionDAO.GetQueriesAnalyzed().get(doc).getYValue());
                sourceBuilder.query(queryBuilder);
                sourceBuilder.size(kappa);

                searchRequest.source(sourceBuilder);

                /*
                 * Retrieving search results to generate context of the similarities statistics.
                 */
                SearchResponse searchResponse = CollectionDAO.GetClient().search(searchRequest, RequestOptions.DEFAULT);
                SearchHits hits = searchResponse.getHits();
                SearchHit[] searchHits = hits.getHits();
                for (int res = 1; res < searchHits.length; res++) {
                    SearchHit hit = searchHits[res];
                    String hitDoc = CollectionDAO.GetDataJSON().get(Integer.parseInt(hit.getId())).getYValue();
                    String[] hitDocSplit = hitDoc.split("\"rcn\":"); hitDocSplit = hitDocSplit[1].split(","); hitDoc = hitDocSplit[0];
                    my_results_file += (CollectionDAO.GetQueries().get(doc).getTValue() + "\tQ0\t" + hitDoc + "\t" + res + "\t  " + hit.getScore()/100 + "\tmyIRmethod\n");
                }
            }
            /*
             * Writing to file information for all generated results from searching "documents" index.
             */
            PrintWriter pw = new PrintWriter(outputDataRepository + "/answers2.txt");
            pw.write(my_results_file); pw.flush(); pw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
