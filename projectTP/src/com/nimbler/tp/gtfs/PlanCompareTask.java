/*
 * @author nirmal
 */
package com.nimbler.tp.gtfs;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import com.nimbler.tp.dataobject.Itinerary;
import com.nimbler.tp.dataobject.TripPlan;
import com.nimbler.tp.dataobject.TripResponse;
import com.nimbler.tp.service.LoggingService;
import com.nimbler.tp.util.GtfsUtils;
import com.nimbler.tp.util.HttpUtils;
import com.nimbler.tp.util.JSONUtil;
import com.nimbler.tp.util.TpConstants;
import com.nimbler.tp.util.TpException;
/**
 * 
 * @author nirmal
 *
 */
public class PlanCompareTask implements Runnable{
	private File oldFile;
	private String destDir;
	private LoggingService logger;
	private String loggerName;
	private List<PlanCompareResult> lstCompareResults;
	public static String[] arrParams = new String[]{"optimize","time","maxWalkDistance","wheelchair","fromPlace",
	"toPlace"};

	public PlanCompareTask(File oldFile, String destDir,
			LoggingService logger, String loggerName,List<PlanCompareResult> lstCompareResults) {
		this.oldFile = oldFile;
		this.destDir = destDir;
		this.logger = logger;
		this.loggerName = loggerName;
		this.lstCompareResults=lstCompareResults;
	}


	@Override
	public void run() {
		String plan;		
		PlanCompareResult result = new PlanCompareResult();
		try {
			plan = FileUtils.readFileToString(oldFile, "UTF-8");
			result.setFileName(oldFile.getName());
			TripResponse oldResponse = JSONUtil.getFullPlanObjFromJson(plan);
			if(oldResponse.getPlan()==null)
				throw new TpException("No plan found in old request");

			String baseUrl = TpConstants.SERVER_URL+"ws/plan?";
			String url = GtfsUtils.getPlanUrlFromResponse(baseUrl,oldResponse,arrParams,new Date());
			String strNewPlan = HttpUtils.getHttpResponse(url);
			TripResponse newResponse = JSONUtil.getFullPlanObjFromJson(strNewPlan);
			writeFile(oldFile,strNewPlan);
			if(newResponse==null || newResponse.getPlan()==null)
				throw new TpException("No plan found in new response: "+strNewPlan);

			TripPlan oldPlan = oldResponse.getPlan();
			TripPlan newPlan = newResponse.getPlan();
			result.setOldPlan(oldPlan);
			result.setNewPlan(newPlan);
			result.setUrl(url);
			Itinerary[] itenerarys =  GtfsUtils.getSameItenerary(oldPlan.getItineraries(), newPlan.getItineraries());
			if(itenerarys == null){
				result.addError("No matching itenerary Found");				
				return;
			}
			boolean match = GtfsUtils.isSameLegs(itenerarys[0], itenerarys[1]);
			if(!match){
				result.addError("Legs Do not match");
				return;
			}		
			result.setMatch(true);
		} catch (IOException e) {
			result.addError(e.getMessage());
			logger.error(loggerName,e.getMessage());
		}catch (TpException e) {
			result.addError(e.getMessage());
			logger.error(loggerName,e.getMessage());
		}catch (Exception e) {
			result.addError(e.getMessage());
			logger.error(loggerName,e);			
		}finally{
			lstCompareResults.add(result);
			logger.debug(loggerName, "File: "+oldFile.getName()+" - "+result.isMatch());
		}
	}

	/**
	 * Write file.
	 *
	 * @param oldFile the old file
	 * @param strNewPlan the str new plan
	 */
	private void writeFile(File oldFile, String strNewPlan) {
		try {
			File newFile  =new File(destDir+FilenameUtils.getName(oldFile.getName()));
			if(newFile.exists())
				newFile.delete();			
			FileUtils.writeByteArrayToFile(newFile, strNewPlan.getBytes());
		} catch (IOException e) {
			logger.error(loggerName, "Error Writting new response: "+e.getMessage());
		}catch (Exception e) {
			logger.error(loggerName,"Error Writting new response:"+e.getMessage());
		}

	}
	public File getOldFile() {
		return oldFile;
	}
	public void setOldFile(File oldFile) {
		this.oldFile = oldFile;
	}
}