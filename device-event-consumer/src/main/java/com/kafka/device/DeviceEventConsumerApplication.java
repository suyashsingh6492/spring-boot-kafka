package com.kafka.device;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling // it's going to invoke the service layer that's going to retry the failed record.
//If the record fails again, then it gets persisted into the database with the same status.
//If the record succeeds, then in that case the DB update will be made to be successful for the record,
public class DeviceEventConsumerApplication {

	public static void main(String[] args) {
// ./gradlew build   , will build the project , go to build>libs
		//to run the application use java -jar /build/libs/device-event-consumer-0.0.1-SNAPSHOT.jar
		//launch another instance java -jar -Dserver.port=8082 /build/libs/device-event-consumer-0.0.1-SNAPSHOT.jar
		//we have 3 parition so 2 partition willbe assign to 1st application, 1 will be 2 second.
		//check logs it is partition.assignment.strategy=[class org.apache.kafka.clients.consumer.RangeAssignor]
		//use to assign partition
		SpringApplication.run(DeviceEventConsumerApplication.class, args);
	}

}
