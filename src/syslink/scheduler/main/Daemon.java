package syslink.scheduler.main;

import java.util.Calendar;
import java.util.Timer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import syslink.lib.lang.CommonUtils;
import syslink.lib.lang.DateUtils;
import syslink.scheduler.task.code.BinTask;

public class Daemon {

	private static Logger logger 				= LoggerFactory.getLogger(Daemon.class);
	private static final long SECOND			= 1000;
	private static final long MINUTE			= 60*1000;
	private static final long HOUR				= 60*60*1000;
	private static final long DAY				= 24*60*60*1000;

	
	
	public Daemon() {
		// TODO Auto-generated constructor stub
	}
	
	
	
	public void execute() {
		
		Calendar calBin = Calendar.getInstance();
		calBin.set(Calendar.AM_PM, Calendar.AM);
		calBin.set(Calendar.HOUR, 1);
		calBin.set(Calendar.MINUTE, 00);
		calBin.set(Calendar.SECOND, 0);
		new Timer().scheduleAtFixedRate(new BinTask(), calBin.getTime(), DAY);
		logger.info("CODE BIN 에 대한 Redis Uploader scheduled : {}", calBin.getTime());

	}
	
	
	//interval 간격 범위에서 미래의 가까운 시간 적용하기
	//interval 은 홀수는 가까운 한시간 후로 적용된다.
	//1,3,4,6,8,10,12 까지만 적용 가능하다.
	public void setNearHourTime(Calendar cal , int interval){
		if(interval == 1 || interval % 2 != 0 ){
			cal.add(Calendar.HOUR,1);
		}else if(interval > 12){
			
		}else{
			int now = CommonUtils.parseInt(DateUtils.getCurrentDay("HH"));
			int nh = ((now/interval)+1) * interval ;
			cal.add(Calendar.HOUR,nh-now);
		}
	}

	
	
	public static void main(String[] args) {
		new Daemon().execute();
	}

}
