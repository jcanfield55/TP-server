package com.nimbler.tp.service.livefeeds;

import java.util.concurrent.Callable;

import com.nimbler.tp.service.livefeeds.stub.WmataApiClient.WmataFunction;

public class WmataApiTask implements Callable<String>{
	WmataFunction<String,String> function;
	String url;

	public WmataApiTask(WmataFunction<String,String> function,String url) {
		this.function = function;
		this.url = url;
	}

	@Override
	public String call() throws Exception {
		return function.call(url);
	}

}
