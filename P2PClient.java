import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class P2PClient {
	private Socket socket;
	private BufferedReader in;
	private PrintWriter out;
	private BufferedReader line;
	private boolean connect_success = true;
	private FileReceive file_receive;
	
	public static void main(String[] args){
		new P2PClient();
	}
	
	public P2PClient(){
		try {
			socket = new Socket("127.0.0.1", P2PServer.SERVER_PORT);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(socket.getOutputStream(), true);
			line = new BufferedReader(new InputStreamReader(System.in));
			String tell;
			String msg;
			
			msg = in.readLine(); // first word received must be welcome word
			if (msg.equals(P2PServer.WelCome_Word)) {
				System.out.println(msg);
				InetAddress addr = InetAddress.getLocalHost();
				String ip = addr.getHostAddress().toString();
				while (true) {
					tell = line.readLine();
					String[] infor = tell.split("#");
					if (infor.length == 2 && Integer.parseInt(infor[1]) > 0 && Integer.parseInt(infor[1]) < 65535) {
						out.println(infor[0]+"#"+ip+"#"+infor[1]);
						System.out.println(in.readLine());
						file_receive = new FileReceive(Integer.parseInt(infor[1])); // open the file receiver
						break;
					}else {
						System.out.println("Illegal input, input your nickname#port again");
					}
				}
			}else {
				// first receive is not welcome, disconnect
				connect_success = false;
				out.println("exit");
			}
			char[] temp = new char[1024];
			while (connect_success){
				tell = line.readLine();
				if (tell.equals("exit")){
					out.println(tell);
					file_receive.releaseServer();
					break;
				}
				else if (tell.equals("send file")) {
					sendFile();
					continue;
				}
				else {
					
				}
				out.println(tell);
				int count = in.read(temp);
				if (count == -1) {
					break;
				}
				msg = String.valueOf(temp, 0, count);
				System.out.print(msg); // read the byde can read the /n, so no need println any more
			}
			line.close();
			out.close();
			in.close();
			socket.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	public void sendFile(){
		try {
			System.out.println("input the file path");
			String path = line.readLine();
			System.out.println("input ip address and port");
			String[] infor = line.readLine().split("#");
			String ip = infor[0];
			String port = infor[1];
			
			Socket sendFileSocket = new Socket(InetAddress.getByName(ip), Integer.parseInt(port));
			ObjectOutputStream oos = new ObjectOutputStream(sendFileSocket.getOutputStream());
			
			FileInputStream fis = new FileInputStream(path);
			System.out.println("sending file...");
			byte[] buf = new byte[1024];
			int len = fis.read(buf);
			while (len > 0) {
				oos.write(buf, 0, len);
				oos.flush();
				Thread.sleep(50);
				len = fis.read(buf);
			}
			System.out.println("file sended");
			fis.close();
			oos.close();
			sendFileSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
	}
	

	class FileReceive extends Thread{
		private ServerSocket ss;
		private Socket s;
		private int port;
		private String pathName;
		private boolean open_server = true;
		
		public FileReceive(int port){
			this.port = port;
			start();
		}
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				String fileName = "receive.txt";
				ss = new ServerSocket(port);
				while (open_server) {
					s = ss.accept();
					System.out.println("someone attemp to send file, receiving...");
					FileOutputStream fos = new FileOutputStream(fileName);
					ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
					
					byte[] buf = new byte[1024];
					int len;
					while ( (len = ois.read(buf)) != -1) {
						fos.write(buf, 0, len);
						fos.flush();
					}
					System.out.println("received...");
					ois.close();
					fos.close();
					s.close();
				}
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("something wrong with the server thread");
				e.printStackTrace();
			}
		}
		
		public void releaseServer(){
			try {
				ss.close();
				open_server = false;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
}
