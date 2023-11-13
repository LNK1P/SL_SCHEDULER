package syslink.scheduler.task.code;

import java.util.List;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import syslink.lib.db.Retrieve;
import syslink.lib.map.SharedMap;
import syslink.lib.redis.Redis;
import syslink.lib.redis.RedisUtils;
import syslink.scheduler.main.Daemon;

public class BinTask extends TimerTask {

	private static Logger logger 				= LoggerFactory.getLogger(Daemon.class);
	private static final String DB_CODE_BIN = "DB_CODE_BIN";
	
	
	
	public BinTask() {
	}

	@Override
	public void run() {
		logger.info("BinTask Start");
		List<SharedMap<String,Object>> list = new Retrieve()
			.table("CODE.BIN_KR")
			.select().getRows();
		
		if(list.size() > 0) {
			logger.info("bin loaded : {}",list.size());
			
			int i =1;
			for(SharedMap<String,Object> map : list) {
				if(!map.isEquals("id", "")) {
					RedisUtils.setHash(DB_CODE_BIN, map.getString("id"), map);
				}
			}
		}
		logger.info("BinTask End");

	}

}
