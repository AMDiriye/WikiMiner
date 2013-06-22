import org.wikipedia.miner.util.text.CaseFolder;
import org.wikipedia.miner.util.text.PorterStemmer;
import org.wikipedia.miner.util.text.TextProcessor;


public class TextFolder extends TextProcessor{
	
	private CaseFolder caseFolder = new CaseFolder();
	private PorterStemmer stemmer = new PorterStemmer();
	
	
	@Override
	public String processText(String text){
		return stemmer.processText((caseFolder.processText(text)));
		
	}
	
}
