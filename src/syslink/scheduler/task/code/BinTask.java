package syslink.scheduler.task.code;

import java.util.List;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.lettuce.core.api.StatefulRedisConnection;
import syslink.lib.db.Retrieve;
import syslink.lib.map.SharedMap;
import syslink.lib.redis.Redis;
import syslink.scheduler.main.Daemon;

public class BinTask extends TimerTask {

	private static Logger logger 				= LoggerFactory.getLogger(Daemon.class);
	private static final String DB_CD_BIN = "DB_CD_BIN";
	
	
	
	public BinTask() {
	}

	@Override
	public void run() {
		logger.info("BinTask Start");
		List<SharedMap<String,Object>> list = new Retrieve()
			.table("SYSLINK.CD_BIN")
			.select().getRows();
		
		if(list.size() > 0) {
			
			StatefulRedisConnection<String, SharedMap<String,Object>> conn = Redis.getSharedMap();
			
			for(SharedMap<String,Object> map : list) {
				if(!map.isEquals("id", "")) {
					conn.async().hset(DB_CD_BIN, map.getString("id"), map);
				}
			}
			
			conn.close();
		}
		logger.info("BinTask End");

	}

}
