package com.dailymotion.upload;


import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.GetRequest;
import com.mashape.unirest.request.HttpRequest;
import com.mashape.unirest.request.HttpRequestWithBody;

public class DmUpload {
	
	
	public static String authenticate(String api_key, String api_secret, String user, String password) throws UnirestException  
	{
		
		String url = "https://api.dailymotion.com/oauth/token";
		
		HttpRequestWithBody request = Unirest.post(url);
		
		request.header("accept", "application/json")
			.field("grant_type", "password")
			.field("scope", "manage_videos")
			.field("client_id", api_key)
			.field("client_secret", api_secret)
			.field("username", user)
			.field("password", password);

		HttpResponse<JsonNode> jsonResponse = request.asJson();
		

		log_request(request, jsonResponse, true);
		
		String access_token = jsonResponse.getBody().getObject().getString("access_token"); 
		return access_token;		
		
	}
	
	public static String get_upload_url(String access_token) throws UnirestException 
	{
		
		String url = "https://api.dailymotion.com/file/upload";
		
		GetRequest request = Unirest.get(url);
		
		request.header("accept", "application/json")
			.header("Authorization", "Bearer " + access_token);
		
		HttpResponse<JsonNode> jsonResponse = request.asJson();
		
		log_request(request, jsonResponse, true);
		
		String upload_url = jsonResponse.getBody().getObject().getString("upload_url"); 
		return upload_url;
		
	}
	
	public static String upload_file(String upload_url, File file) throws UnirestException 
	{
		
		System.out.println("Uploading...");
		System.out.println();
		
		HttpRequestWithBody request = Unirest.post(upload_url);
		
		request.header("accept", "application/json")
			.field("file", file);
		
		HttpResponse<JsonNode> jsonResponse = request.asJson();
		
		log_request(request, jsonResponse, false);
		
		String video_url = jsonResponse.getBody().getObject().getString("url"); 
		return video_url;
		
	}
	
	public static void create_video(String access_token, String title, String category, String language, String video_url, boolean published) throws UnirestException 
	{
		String url = "https://api.dailymotion.com/me/videos";
		
		HttpRequestWithBody request = Unirest.post(url);
		
		request.header("accept", "application/json")
		.header("Authorization", "Bearer " + access_token)
		.queryString("title", title)
		.queryString("channel", category)
		.queryString("language", language)
		.queryString("url", video_url)
		.queryString("published", published);
	
		HttpResponse<JsonNode> jsonResponse = request.asJson();
		
		log_request(request, jsonResponse, true);
		
	}
	
	// MAIN
	
	public static void main(String [] args)
	{
		// Secret
		String api_key = "API_KEY";
		String api_secret = "API_SECRET"; 
		String user = "USERNAME_OR_EMAIL"; 
		String password = "PASSWORD";
		
		// Video file
		File file = new File("video.mp4");
		
		// Video metadata
		String title = "Title of the video";
		String category = "news"; 
		String language = "en";
		boolean published = true;
		
		try {
			
			String access_token = authenticate(api_key, api_secret, user, password);
			
			String upload_url = get_upload_url(access_token);
			
			String video_url = upload_file(upload_url, file);
			
			create_video(access_token, title, category, language, video_url, published);
			
		} catch (UnirestException e) {
			e.printStackTrace();
		}
		
	}
	
	// LOGS
	
	public static void log_request(HttpRequest request, HttpResponse<JsonNode> response, boolean log_request_body) 
	{
		System.out.println(request.getHttpMethod() + " " + request.getUrl());
		
		if(log_request_body) {
			try {
				InputStream in = request.getBody().getEntity().getContent(); 
				int n = in.available();
				byte[] bytes = new byte[n];
				in.read(bytes, 0, n);
				String body = new String(bytes, StandardCharsets.UTF_8);
				System.out.println("  REQUEST BODY: " + body);
			} catch (Exception e) {	} 
		}

		System.out.println("  RESPONSE STATUS: " + response.getStatus() + " " + response.getStatusText());
		System.out.println("  RESPONSE BODY: " + response.getBody().toString());
		System.out.println();
	}


}
