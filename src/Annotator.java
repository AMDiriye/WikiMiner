import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.wikipedia.miner.annotation.Disambiguator;
import org.wikipedia.miner.annotation.Topic;
import org.wikipedia.miner.annotation.TopicDetector;
import org.wikipedia.miner.annotation.preprocessing.DocumentPreprocessor;
import org.wikipedia.miner.annotation.preprocessing.PreprocessedDocument;
import org.wikipedia.miner.annotation.preprocessing.WikiPreprocessor;
import org.wikipedia.miner.annotation.tagging.DocumentTagger;
import org.wikipedia.miner.annotation.tagging.WikiTagger;
import org.wikipedia.miner.annotation.tagging.DocumentTagger.RepeatMode;
import org.wikipedia.miner.annotation.weighting.LinkDetector;
import org.wikipedia.miner.model.Wikipedia;
import org.wikipedia.miner.util.WikipediaConfiguration;


public class Annotator {
	DocumentPreprocessor _preprocessor ; 
	Disambiguator _disambiguator ;
	TopicDetector _topicDetector ;
	LinkDetector _linkDetector ;
	DocumentTagger _tagger ;
	List<String> scores;
	List<String> labels;
	DecimalFormat _df = new DecimalFormat("#0%") ;

	public Annotator(Wikipedia wikipedia) throws Exception {
		scores = new ArrayList<String>();
		labels = new ArrayList<String>();
		
		_preprocessor = new WikiPreprocessor(wikipedia) ;
		_disambiguator = new Disambiguator(wikipedia);
		File file = new File("/Users/abdigani/Downloads/wikipedia-miner-1.2.0/models/annotate/disambig_en_In.model");
		_disambiguator.loadClassifier(file) ;
		_topicDetector = new TopicDetector(wikipedia, _disambiguator, true, false) ;
		
		_linkDetector = new LinkDetector(wikipedia) ;
		_linkDetector.loadClassifier(new File("/Users/abdigani/Downloads/wikipedia-miner-1.2.0/models/annotate/detect_en_In.model")) ;
		
		_tagger = new WikiTagger() ;
	}

	public void annotate(String originalMarkup) throws Exception {

		PreprocessedDocument doc = _preprocessor.preprocess(originalMarkup) ;
		List<String> stemmedTopics = new ArrayList<String>();
		
		Collection<Topic> allTopics = _topicDetector.getTopics(doc, null) ;
		System.out.println("\nAll detected topics:") ;
		Stemmer stemmer = new Stemmer();
		//String stemmedOriginalMarkup = stemmer.stripAffixes(originalMarkup);
		ArrayList<Topic> bestTopics = _linkDetector.getBestTopics(allTopics, 0.35) ;
		
		//Adds topics found in snippet
		for (Topic t:allTopics){
			//String stemmedTopic = stemmer.stripAffixes(t.getTitle());
			//stemmedTopics.add(stemmedTopic);
			//content += " - " + t.getTitle()+" - "+t.getAverageLinkProbability();
			
			if(t.getAverageLinkProbability() > 0.35 && !bestTopics.contains(t)){
				bestTopics.add(t);
			}
		}
		
		//content += "\nTopics that are probably good links:";
		
		for (Topic t:bestTopics){
			//content.add(t.getTitle());// + " [" + _df.format(t.getWeight()) + "]";
			scores.add(_df.format(t.getWeight()));
			labels.add(t.getTitle());
		}
		//String newMarkup = _tagger.tag(doc, bestTopics, RepeatMode.ALL) ;
		//content += "\nAugmented markup:\n" + newMarkup + "\n";
	

	}

	public static Annotator init (String clip) throws Exception {
		
		File file = new File("/Users/abdigani/Downloads/wikipedia-miner-1.2.0/configs/wikipedia-template.xml");
		WikipediaConfiguration conf = new WikipediaConfiguration(file);
		conf.clearDatabasesToCache() ;
		Wikipedia wikipedia = new Wikipedia(conf, false) ;

		Annotator annotator = new Annotator(wikipedia) ;

		//BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, "UTF-8")) ;

		//while (true) {
		//	System.out.println("Enter snippet to annotate (or ENTER to quit):") ;
		//	String line = reader.readLine();
		//	System.out.println("bla");
		//	if (line.trim().length() == 0)
		//		break ;
			
			 annotator.annotate(clip) ;
			 return annotator;
		//}
	}


	public List<String> getScore(){
		return scores;
	}
	
	public List<String> getLabels(){
		return labels;
	}
	
	
	public static void main(String args[]){
		
		try {
		//	System.out.println(Annotator.run("test"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

