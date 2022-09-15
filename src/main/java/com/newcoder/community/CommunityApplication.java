package com.newcoder.community;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class CommunityApplication {

	/**
	 * 这个注解是用来管理bean的生命周期的 初始化
	 * 这个注解修饰的方法 会在初始化化以后被执行
	 * @param
	 */
	@PostConstruct
	public void init(){
		//解决netty启动冲突的问题
		//see Netty4Utils.setAvailableProcessors()
		System.setProperty("es.set.netty.runtime.available.processors","false");
	}

	public static void main(String[] args) {
		SpringApplication.run(CommunityApplication.class, args);
	}

}
