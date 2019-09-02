package kr.co.itcen.network.http;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.Files;

public class RequestHandler extends Thread {
	private static String documentRoot = "";
	static {  //class가 loading될때 위치잡는 코드
		documentRoot = RequestHandler.class.getClass().getResource("/webapp").getPath();   //class path 위치에 뜸
	}
	
	private Socket socket;
	private String msg = null;
	
	public RequestHandler( Socket socket ) {
		this.socket = socket;
	}
	
	@Override
	public void run() {
		try {
			// get IOStream
			OutputStream outputStream = socket.getOutputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));

			// logging Remote Host IP Address & Port
			InetSocketAddress inetSocketAddress = ( InetSocketAddress )socket.getRemoteSocketAddress();
			consoleLog( "connected from " + inetSocketAddress.getAddress().getHostAddress() + ":" + inetSocketAddress.getPort() );
			
			String request = null;
			
			while(true) {
				String line = br.readLine();
				
				//브라우저가 연결을 끊으면
				if(line == null) {
					break;
				}
				
				//header만 읽음
				if("".equals(line)) {
					break;
				}
				
				if(request == null) {
					request = line;
					break;
				}
			}
			
			String[] tokens = request.split(" ");
			if("GET".equals(tokens[0])) {
				consoleLog("request: " + request);
				responseStaticResource(outputStream, tokens[1], tokens[2]);
			} else {  //POST, PUT, DELETE 명령은 무시
				consoleLog("bad request: " + request);
				msg = " 400 Bad Request\r\n";
				response400Error(outputStream, tokens[2]);     //과제
			}
			
			
			// 예제 응답입니다.
			// 서버 시작과 테스트를 마친 후, 주석 처리 합니다.
//			outputStream.write( "HTTP/1.1 200 OK\r\n".getBytes( "UTF-8" ) );   //헤더시작
//			outputStream.write( "Content-Type:text/html; charset=utf-8\r\n".getBytes( "UTF-8" ) ); //헤더끝
//			outputStream.write( "\r\n".getBytes() );  //바디
//			outputStream.write( "<h1>이 페이지가 잘 보이면 실습과제 SimpleHttpServer를 시작할 준비가 된 것입니다.</h1>".getBytes( "UTF-8" ) );

		} catch( Exception ex ) {
			consoleLog( "error:" + ex );
		} finally {
			// clean-up
			try{
				if( socket != null && socket.isClosed() == false ) {
					socket.close();
				}
				
			} catch( IOException ex ) {
				consoleLog( "error:" + ex );
			}
		}			
	}
	
	
	private void responseStaticResource(OutputStream outputStream, String url, String protocol) throws IOException {
		
		if("/".equals(url)) {     //"/"가 중요!! /뒤부터 url -> 파일
			url = "/index.html";
			msg = " 200 OK\r\n";
		}
		
		File file = new File(documentRoot + url);
		if(file.exists() == false) {
			msg = " 404 File Not Found\r\n";
			response404Error(outputStream, protocol);    
			consoleLog("File Not Found: " + url);
			return;
		}
		
		//nio
		byte[] body = Files.readAllBytes(file.toPath());
		String contentType = Files.probeContentType(file.toPath());
		
		//응답
		outputStream.write( (protocol + msg).getBytes( "UTF-8" ) );   //헤더시작
		outputStream.write( ("Content-Type:" + contentType + "; charset=utf-8\r\n").getBytes( "UTF-8" ) ); //헤더끝
		outputStream.write( "\r\n".getBytes() );  //브라우져에서 공백이 있으면 그 밑이 바디라고 인식
		outputStream.write( body );
		
	}
	
	private void response400Error(OutputStream outputStream, String protocol) throws IOException { 
		String url = "/error/400.html";
		responseStaticResource(outputStream, url, protocol);
	}
	
	private void response404Error(OutputStream outputStream, String protocol) throws IOException { 
		String url = "/error/404.html";
		responseStaticResource(outputStream, url, protocol);
	}

	public void consoleLog( String message ) {
		System.out.println( "[RequestHandler#" + getId() + "] " + message );
	}
}
