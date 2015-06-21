import java.awt.datatransfer.StringSelection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class P2PClient {
	private Socket socket;
	private BufferedReader in;
	private PrintWriter out;
	private boolean connect_success = true;
	
	public static void main(String[] args){
		new P2PClient();
	}
	
	public P2PClient(){
		try {
			socket = new Socket("127.0.0.1", 10000);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(socket.getOutputStream(), true);
			BufferedReader line = new BufferedReader(new InputStreamReader(System.in));
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
					if (infor.length == 2) {
						out.println(infor[0]+"#"+ip+"#"+infor[2]);
						break;
					}else {
						System.out.println("Illegal input, input your nickname#port again");
					}
				}
			}else {
				// 收到的第一句话不是Welcome表示不知名的问题，断开连接
				connect_success = false;
				out.println("exit");
			}
			while (connect_success){
				tell = line.readLine();
				out.println(tell);
				msg = in.readLine();
				System.out.println(msg);
				if (tell.equals("exit"))
					break;
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
}
