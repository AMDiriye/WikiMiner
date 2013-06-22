import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;


public class TestServley extends HttpServlet{

    private static final ResourceBundle RB = ResourceBundle.getBundle("LocalStrings");
    
    @Override
    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
        throws IOException, ServletException
    {
        response.setContentType("text/html");
        
        PrintWriter out = response.getWriter();
        out.println("<html>");
        out.println("<body>");

        String clip = request.getParameter("q");
        
        try {
        	Annotator annotator = Annotator.init(clip);
        	JSONObject json = new JSONObject();
        	List<String> score = annotator.getScore();
        	List<String> labels = annotator.getLabels();
        	
        	for(int i=0; i<labels.size(); i++){
        		json.accumulate("score", score.get(i));
        		json.accumulate("label", labels.get(i));
        	}
        
        	out.println(json.toString());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
     
        out.println("</body>");
        out.println("</html>");
    }

    @Override
    public void doPost(HttpServletRequest request,
                      HttpServletResponse response)
        throws IOException, ServletException
    {
        doGet(request, response);
    }
    
}
