package test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;

import com.apprika.otp.util.TpConstants;

public class Test {
	public static String getFeedback(String startDate, String endDate, String from, String to, String feedbackTime, String source) throws IOException, URISyntaxException{
		String filePath ="C:\\Eclipse_workspace\\Pick_One_Workspace\\Test1\\src\\OTP.html";
		File file = new File(Test.class.getClassLoader().getResource(TpConstants.OTP_HTML_FILE_PATH).toURI());
		FileReader fileReader = new FileReader(file);
		BufferedReader bf = new BufferedReader(fileReader);
		String line;
		StringBuilder sb = new StringBuilder();
		while((line=bf.readLine())!=null){
			sb.append(line);
		}
		String otpFeedback = sb.toString().replace("--startdate--", startDate)
				.replace("--enddate--", endDate)
				.replace("--from--", from)
				.replace("--to--", to)
				.replace("--feedbacktime--", feedbackTime)
				.replace("--source--", source);
		System.out.println(otpFeedback);
		return otpFeedback;
	}

	public static void main(String[] args) throws IOException, JDOMException, URISyntaxException {
		/*	String path ="C:\\Documents and Settings\\suresh\\Desktop\\Test.xml";
		FileReader fileReader = new FileReader(path);
		BufferedReader bf = new BufferedReader(fileReader);
		String line;
		StringBuilder sb = new StringBuilder();
		while((line=bf.readLine())!=null){
			sb.append(line);
		}
		String strTrip = sb.toString();
		getXmlData(strTrip.replace("&", "and"));*/
	}

	private static String[] getXmlData(String strTrip) throws JDOMException, IOException, URISyntaxException{
		String[] strResponse = new String[4];
		SAXBuilder builder = new SAXBuilder();
		Document document = builder.build(new StringReader(strTrip));

		Element beans = document.getRootElement();
		strResponse[0] = ((Element)XPath.selectSingleNode(beans, "plan/from/name")).getValue();
		strResponse[1] = ((Element)XPath.selectSingleNode(beans, "plan/to/name")).getValue();
		strResponse[2] = ((Element)XPath.selectSingleNode(beans, "plan/itineraries/itinerary/startTime")).getValue();
		strResponse[3] = ((Element)XPath.selectSingleNode(beans, "plan/itineraries/itinerary/endTime")).getValue();
		getFeedback(strResponse[0], strResponse[0], strResponse[0], strResponse[0], strResponse[0], strResponse[0]);
		return strResponse;
	}
}
