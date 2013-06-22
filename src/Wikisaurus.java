import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.*;
import org.wikipedia.miner.model.Article;
import org.wikipedia.miner.model.Label;
import org.wikipedia.miner.model.Wikipedia;
import org.wikipedia.miner.util.WikipediaConfiguration;
import org.wikipedia.miner.model.Category;
import org.wikipedia.miner.comparison.ArticleComparer;
import org.wikipedia.miner.db.WEnvironment;

public class Wikisaurus {

	BufferedReader _input ;
	Wikipedia _wikipedia ;
	Label.Sense sense;
	ArticleComparer _comparer;
	DecimalFormat _df = new DecimalFormat("#0%") ;
	List<Article> _relatedTopics;
	boolean articleExists = false;
	
	public Wikisaurus(String filePath) {
		try{
			WikipediaConfiguration conf = new WikipediaConfiguration(new File(filePath));
			//WEnvironment.prepareTextProcessor(new TextFolder(), conf, new File("temp"), true, 1);
			//_input = new BufferedReader(new InputStreamReader(System.in)) ;
			_wikipedia = new Wikipedia(conf, false);
			_comparer = new ArticleComparer(_wikipedia);
		}
		catch(Exception e){}
		 
	}

	public void findRelatedWikiDocs(String term) throws Exception {
			
		System.out.println("term: "+term);
		Label label = _wikipedia.getLabel(term);
		
		if (!label.exists()) {
			System.out.println("Could not find: " + term ) ;
		} 
		
		else {
			articleExists = true;
			
			Label.Sense[] senses = label.getSenses() ;
			if (senses.length == 0) {
				sense = senses[0];
			} 
			
			else {
				System.out.println("'" + term + "' could mean several things:") ;
				for (int i=0 ; i<senses.length ; i++) {
					System.out.println(" - [" + (i+1) + "] " + senses[i].getTitle()) ;
				}
				
				sense = senses[0];
				_relatedTopics = gatherRelatedTopics(sense);
				_relatedTopics = sortTopics(sense, _relatedTopics) ;
				
				//Integer senseIndex = getInt("So which do you want?", 1, senses.length) ;
				//if (senseIndex == null)
				//	displaySense(senses[senseIndex-1]) ;
			}
		}
	}
	
	protected void displaySense(Label.Sense sense) throws Exception {
		
		System.out.println("==" + sense.getTitle() + "==") ;
		
	//	displayDefinition(sense) ;
	//	displayAlternativeLabels(sense) ;
	//	displayRelatedTopics(sense) ;
	}
	
	public String displayDefinition() throws Exception {
		String defn = "";
		
		//defn += "sentence 1: "+(sense.getParentCategories()[0])+"\r\n";
		defn += "sentence 2: "+(sense.getFirstParagraphMarkup())+"\r\n";
		//defn += "sentence 3: "+(sense.getTitle())+"\r\n";
		//defn += "sentence 1: "+(sense.getMarkup());
		
		return defn;
	}
	
	public String displayAlternativeLabels() throws Exception {
		String altTerms = ""; 
		System.out.println("Alternative Terms");
		System.out.println("-----------------");
		for (Article.Label label:sense.getLabels()) 
			altTerms += label.getText()+" ";
		
		return altTerms;
	}
	
	public String displayRelatedTopicLabels(){
		
		String relatedTopic = "";
		if(_relatedTopics == null)
			return " ";
		
		for (Article art:_relatedTopics) 
		{
			relatedTopic += art.getTitle()+ " ";
			
			for (Article.Label label:art.getLabels()) 
				relatedTopic += label.getText()+" ";
		}
		return relatedTopic;
	}
	
	
	public String displayRelatedTopics() throws Exception {
		String relatedTopics = "";
		

		int count = 0;
		
		//now trim the list if necessary
	    if (_relatedTopics.size() > 100)
	    	_relatedTopics = _relatedTopics.subList(1,100) ;
		
		System.out.println("\nRelated topics:") ;
		
		for (Article art:_relatedTopics) 
		{
			if(art.getWeight()>0.60 && count<10){
				System.out.println(art.getWeight());
				
				try{
					relatedTopics += art.getFirstParagraphMarkup()+" ";
				}
				catch(Exception e){
					System.out.println("Couldn't find article content");
					
				}
			
			}
			String altTerms = "";
			
			for (Article.Label label:art.getLabels()) 
				altTerms += label.getText()+" ";
			
			relatedTopics += altTerms;
		}
		
		return relatedTopics;
	}

	private List<Article> gatherRelatedTopics(Article art) {
		HashSet<Integer> relatedIds = new HashSet<Integer>() ;
		relatedIds.add(art.getId()) ;

		ArrayList<Article> relatedTopics = new ArrayList<Article>() ;

		//gather from out-links
		for (Article outLink:art.getLinksOut()) {
			if (!relatedIds.contains(outLink.getId())) {
				relatedIds.add(outLink.getId()) ;
				relatedTopics.add(outLink) ;
			}
		}

		//gather from in-links
		for (Article inLink:art.getLinksIn()) {
			if (!relatedIds.contains(inLink.getId())) {
				relatedIds.add(inLink.getId()) ;
				relatedTopics.add(inLink) ;
			}
		}   

		//gather from category siblings
		for (Category cat:art.getParentCategories()){
			for (Article sibling:cat.getChildArticles()) {
				if (!relatedIds.contains(sibling.getId())) {
					relatedIds.add(sibling.getId()) ;
					relatedTopics.add(sibling) ;
				}
			}
		}
		
		return relatedTopics ;
	}

	private List<Article> sortTopics(Article queryTopic, List<Article>relatedTopics) throws Exception {
		//weight the related articles according to how strongly they relate to sense article
	    for (Article art:relatedTopics) 
	        art.setWeight(_comparer.getRelatedness(art, queryTopic)) ;
	    
	    //Now that the weight attribute is set, sorting will be in descending order of weight.
	    //If weight was not set, it would be in ascending order of id.  
	    Collections.sort(relatedTopics) ;
	    return relatedTopics ;
	}

	public boolean getArticleExists(){
		return articleExists;
	}
	public static void main(String args[]) throws Exception {
		System.out.println("here");
		
		Wikisaurus thesaurus = new Wikisaurus("/Users/abdigani/Downloads/wikipedia-miner-1.2.0/configs/wikipedia-template.xml") ;
		
		while(true)
		{
			System.out.println("enter a term to look up");
			String term = new BufferedReader(new InputStreamReader(System.in)).readLine();
			thesaurus.findRelatedWikiDocs(term);
		
			if(thesaurus.getArticleExists()){
				System.out.println(thesaurus.displayAlternativeLabels());
				System.out.println(thesaurus.displayDefinition());
				System.out.println(thesaurus.displayRelatedTopicLabels());
				System.out.println(thesaurus.displayRelatedTopics());
			}
			
		}
	}
	
}