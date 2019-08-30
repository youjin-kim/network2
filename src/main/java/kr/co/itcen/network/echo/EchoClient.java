package kr.co.itcen.network.echo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Scanner;

public class EchoClient {
	private static String SERVER_IP = "192.168.1.85";
	private static int SERVER_PORT = 8000;
	
	public static void main(String[] args) {
		Scanner scanner = null;
		Socket socket = null;
	
		try {
			//1. Scanner 생성(표준입력, 키보드 연결)
			scanner = new Scanner(System.in);
			
			//2. 소켓생성
			socket = new Socket();
			
			//3. 서버연결
			socket.connect(new InetSocketAddress(SERVER_IP, SERVER_PORT));
			log("connected");
			
			//4. I/O Stream생성
			BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
			PrintWriter pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);

			while(true) {
				//5. 키보드 입력 받기
				System.out.print(">>");
				String line = scanner.nextLine();
				if("quit".equals(line)) {
					break;
				}

				//6. 데이터 쓰기(전송)
				pw.println(line);
				
				//7. 데이터 읽기(수신)
				String data = br.readLine();
				if(data == null) {
					log("closed by client");
					break;
				}
				
				//8. 콘솔 출력
				System.out.println("<<" + data);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if(scanner != null) {
					scanner.close();
				}
				if(socket != null && socket.isClosed() == false) {
					socket.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}		
	}
	
	private static void log(String log) {
		System.out.println("[Echo Client] " + log);
	}
}