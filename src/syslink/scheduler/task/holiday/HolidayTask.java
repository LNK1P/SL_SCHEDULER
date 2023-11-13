package syslink.scheduler.task.holiday;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import syslink.lib.db.Update;
import syslink.lib.json.JacksonUtils;
import syslink.lib.lang.CommonUtils;
import syslink.lib.lang.DateUtils;
import syslink.lib.lang.MsgUtils;
import syslink.lib.map.SharedMap;
import syslink.lib.xml.JacksonXmlUtils;
import syslink.scheduler.main.Daemon;

public class HolidayTask extends TimerTask{
	private static Logger logger 				= LoggerFactory.getLogger(Daemon.class);
	
	private static OkHttpClient client 	= null;
	private static final String format = "{}:{}:{}";
	private static final String query = "http://apis.data.go.kr/B090041/openapi/service/SpcdeInfoService/getHoliDeInfo?solYear={}&solMonth={}&ServiceKey=huxFA0%2BVBSGESBc7g0AsykhBwhMeNTBK%2FzyJlSfXgJFHR2tsU6FfmBoYeQhfMqsmypi%2BnZF9j%2BrUW8naKAEHVw%3D%3D";
	
	
	static{
		if(client == null){
			client = new OkHttpClient.Builder()
			.connectTimeout(3000, TimeUnit.MILLISECONDS)
			.writeTimeout(5000, TimeUnit.MILLISECONDS)
			.readTimeout(5000, TimeUnit.MILLISECONDS)
			.retryOnConnectionFailure(false)				
			.followRedirects(false)
			.build();
		}
	}
	
	public HolidayTask() {
		// TODO Auto-generated constructor stub
	}
	
	
	@Override
	public void run() {
		if(DateUtils.getCurrentDay("dd").equals("01") || DateUtils.getCurrentDay("dd").equals("02")) {
		}else {return ;}
		
		logger.info("Holiday Start");
		
		List<String> months = new ArrayList<String>();
		List<String> holidays = new ArrayList<String>();
		
		for(int i=1 ; i < 4;i++) {
			LocalDate ld = DateUtils.getDay();
			ld = ld.plusMonths(i);
			String s = ld.format(DateTimeFormatter.ofPattern("yyyyMM"));
			holidays.addAll(datago(s));
		}
		
		int updated = 0;
		for(String data : holidays) {
			String[] datas = data.split("[:]");
			updated += 
				new Update().table("CODE.HOLIDAY")
				.record("holiday",true)
				.record("summary",datas[2])
				.where("id", CommonUtils.parseInt(datas[0]))
				.update();
		}
		
		logger.warn("휴일정보 업데이트 됨. 업데이트 건수 : {}, \n상세 데이터 : {}",updated,holidays);
		
		logger.info("Holiday End : {}",new JacksonUtils().toJson(holidays));
	}
	
	
	/*http://apis.data.go.kr/B090041/openapi/service/SpcdeInfoService/getHoliDeInfo?solYear={}&solMonth={}&ServiceKey=huxFA0%2BVBSGESBc7g0AsykhBwhMeNTBK%2FzyJlSfXgJFHR2tsU6FfmBoYeQhfMqsmypi%2BnZF9j%2BrUW8naKAEHVw%3D%3D
	 * */
	
	@SuppressWarnings("unchecked")
	public List<String> datago(String month){
		List<String> holidays = new ArrayList<String>();
		
		
		SharedMap<String,Object> map = null;
		Response response= null; 
		String resBuf 	 = "";
		
		logger.info(MsgUtils.format(query,month.substring(0,4),month.substring(4,6)));
		
		try {
			Request reqHttp = new Request.Builder()
					.url(MsgUtils.format(query,month.substring(0,4),month.substring(4,6)))
					.get()
					.build();
			
			response = client.newCall(reqHttp).execute();
			resBuf	= response.body().string();
		}catch(Exception e) {
			logger.info(CommonUtils.getExceptionMessage(e));
			return holidays;
			
		}finally{
			if(!response.isSuccessful()) {
				return holidays;
			}else {
				map = new JacksonXmlUtils().fromJsonSharedObject(resBuf);
				LinkedHashMap<String,Object> header = (LinkedHashMap<String,Object>)map.get("header");
				if(header != null && header.get("resultCode").equals("00")) {
					LinkedHashMap<String,Object> body = (LinkedHashMap<String,Object>)map.get("body");
					
					int count = CommonUtils.parseInt(body.get("totalCount"));
					if(count == 0) {
						return holidays;
					}else if(count == 1) {
						@SuppressWarnings("unchecked")
						LinkedHashMap<String,Object> items = (LinkedHashMap<String,Object>)body.get("items");
						LinkedHashMap<String,Object> item = (LinkedHashMap<String,Object>)items.get("item");
						
						holidays.add(MsgUtils.format(format, item.get("locdate"), item.get("isHoliday"), item.get("dateName")));
					}else {
						@SuppressWarnings("unchecked")
						LinkedHashMap<String,Object> items = (LinkedHashMap<String,Object>)body.get("items");
						
						List<LinkedHashMap<String,Object>> itemList = (List<LinkedHashMap<String,Object>>)items.get("item");
						for(LinkedHashMap<String,Object> item : itemList) {
							holidays.add(MsgUtils.format(format, item.get("locdate"), item.get("isHoliday"), item.get("dateName")));
						}
						
					}
					
				}else {
					logger.info("");
				}
			}
			
		}
		
		
		return holidays;
	}
	
	
	
	
	
	
	public static void main(String[] args) {
		new HolidayTask().run();
	}

}

