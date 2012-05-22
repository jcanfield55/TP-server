/**
 * 
 */
package com.apprika.otp.servlet;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;

import com.apprika.otp.service.LoggingService;
import com.apprika.otp.util.TpConstants;
import com.sun.mail.iap.Response;

/**
 * @author nirmal
 *
 */
public class MainServlet extends HttpServlet {

	public static final String SESSION = "Session";
	public static final String USER = "USER";
	Logger logger = LoggingService.getLoggingService(MainServlet.class.getName());
	Logger wsLogger = LoggingService.getLoggingService("wslog");

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 2962168976433957593L;
	//	XmlTransformUtil xmlUtil = XmlTransformUtil.getInstance();
	//	private PersisterDao persisterDao = PersisterDao.getInstance();
	//	private PersistanceUtil persistanceUtil = new PersistanceUtil();

	/**
	 * @author nirmal
	 * @since Jul 6, 2011
	 */
	public MainServlet() {
	}
	/* (non-Javadoc)
	 * @see javax.servlet.GenericServlet#init()
	 */
	@Override
	public void init() throws ServletException {

		super.init();
	}
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		//		super.doPost(req, resp);
		handleRquest(req, resp);
	}
	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		//		super.doGet(req, resp);
		handleRquest(req, resp);
	}
	/**
	 * Default handler for doGet or doPost request
	 * @author nirmal
	 * @since Jul 6, 2011
	 * @param httpReq
	 * @param httpResp
	 * @throws ServletException
	 * @throws IOException
	 */
	private void handleRquest(HttpServletRequest httpReq, HttpServletResponse httpResp)
			throws ServletException, IOException {
		String xmlReq = "", timeStamp = "";
		HashMap<String, String> lstMediaUrl = new HashMap<String, String>();
		try {
			httpResp.setContentType("text/xml");
			//			Map map = httpReq.getParameterMap();
			//			Set set = map.keySet();
			//			for (Object object : set) {
			//				System.out.println(object+" - "+((String[]) map.get(object))[0]);
			//			}
			//			if(true)
			//				return;

			//			httpResp.setContentType("application/json.");

			//			BufferedReader br = new BufferedReader(new InputStreamReader(httpReq.getInputStream()));
			//			StringBuffer sb = new StringBuffer();
			//			String line;
			//			while((line=br.readLine())!=null)
			//				sb.append(line);
			//			String xmlReq = sb.toString();
			//FIXME remove after testing
			//			String xmlReq= httpReq.getParameter("req");
			//			System.out.println(xmlReq);
			//			xmlReq = URLDecoder.decode(xmlReq,"UTF-8");
			//			System.out.println(xmlReq);
			if(ServletFileUpload.isMultipartContent(httpReq)){
				logger.debug("multipart request..");
				DiskFileItemFactory  fileItemFactory = new DiskFileItemFactory ();
				File tmpDir = new File(System.getProperty("java.io.tmpdir"));
				fileItemFactory.setSizeThreshold(10*1024*1024); //10 MB
				fileItemFactory.setRepository(tmpDir);

				System.out.println(ServletFileUpload.isMultipartContent(httpReq));
				ServletFileUpload uploadHandler = new ServletFileUpload(fileItemFactory);
				try {
					List items = uploadHandler.parseRequest(httpReq);
					Iterator itr = items.iterator();
					while(itr.hasNext()) {
						FileItem item = (FileItem) itr.next();
						if(item.isFormField()) {
							if(item.getFieldName().equalsIgnoreCase("req"))
								xmlReq = item.getString();
						} else {
							String filename = System.currentTimeMillis()+"_"+item.getName();
							File file = new File(TpConstants.FILE_REPOSITORY,filename);
							item.write(file);

							String serverFile = TpConstants.REPO_RELATIVE_PATH+"/"+filename;
							lstMediaUrl.put(item.getName(), serverFile);
							logger.debug("absolute path---->"+file.getAbsolutePath());
						}
					}
				}catch(FileUploadException ex) {
					logger.error("Error encountered while parsing the request",ex);
				} catch(Exception ex) {
					logger.error("Error encountered while uploading file",ex);
				}
			} else{

				xmlReq= httpReq.getParameter("req");
				xmlReq = URLDecoder.decode(xmlReq,"UTF-8");
			}


			wsLogger.debug("Request--->\n"+xmlReq);
			//			request = (Request) XmlTransformUtil.getObjectFromXML(xmlReq);
			//			if(request==null){
			//				Response response = RequestHelper.getResponseFromRequest(request,PickOneOperationCodes.INVALID_REQUEST, PickOneOperationCodes.getMessage(PickOneOperationCodes.INVALID_REQUEST));
			//				sendResponse(httpResp, response);
			//				return;
			//			}
			//			request.setLstMediaUrl(lstMediaUrl);
			//			String strOpcode = request.getCode();
			//			if(strOpcode.equals("LOGIN")){
			//				loginUser(request,httpReq,httpResp);
			//				return;
			//			}
			//			if(strOpcode.equals("LOGOUT")){
			//				httpReq.getSession().removeAttribute(SESSION);
			//				httpReq.getSession().removeAttribute(USER);
			//				Response res = RequestHelper.getResponseFromRequest(request,PickOneOperationCodes.LOGOUT_SUCESS,
			//						PickOneOperationCodes.getMessage(PickOneOperationCodes.LOGOUT_SUCESS));
			//				sendResponse(httpResp, res);
			//				return;
			//			}
			//			if(isSessionRequired(strOpcode)){
			//				Object session = httpReq.getSession(true).getAttribute(SESSION);
			//				if(session==null || session!=null && !(Boolean)session){
			//					Response res = RequestHelper.getResponseFromRequest(request,PickOneOperationCodes.INVALID_SESSION,
			//							PickOneOperationCodes.getMessage(PickOneOperationCodes.INVALID_SESSION));
			//					sendResponse(httpResp, res);
			//					return;
			//				}
			//			}
			//			request.setUser((User) httpReq.getSession().getAttribute(USER));
			//			request.setHttpSession( httpReq.getSession());
			//			AbstractServiceManager service = ServiceFactory.getService(request.getService(),request,logger);
			//			Response res = service.getResponse();
			//			sendResponse(httpResp, res);
			//		} catch (InvalidRequestExeption e) {
			//			Response response = RequestHelper.getResponseFromRequest(request,e.getCode(),e.getMsg());
			//			sendResponse(httpResp, response);
		} catch (Exception e) {
			logger.error("",e);
		}

	}

	/**
	 * @author nirmal
	 * @since Jul 6, 2011
	 */
	private void sendResponse(HttpServletResponse resp,Response objRes) {
		try {
			//			String strXML = XmlTransformUtil.getXMLFromObject(objRes);
			String strXML = null;
			//			String strXML = new Gson().toJson(objRes);
			wsLogger.debug("Response--->\n"+strXML+"\n=========================================================================");
			//			System.out.println("responce------------> "+strXML);
			resp.getWriter().write(strXML);
			resp.getWriter().flush();
			resp.getWriter().close();
		} catch (IOException e) {
			TpConstants.logger.error(e.getMessage());
		}
	}
}
