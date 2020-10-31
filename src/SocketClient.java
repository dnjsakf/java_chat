import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

public class SocketClient {
    
    public static void blockingClient() throws IOException {
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(true);

        System.out.println("Require Connection");
        socketChannel.connect(new InetSocketAddress("localhost", 3000));
        System.out.println("Connection Success");

        ByteBuffer byteBuffer;
        Charset charset = Charset.forName("UTF-8");

        // "Hello Server"�� ���� ������
        byteBuffer = charset.encode("Hello Server");
        socketChannel.write(byteBuffer);
        System.out.println("Sending Success");

        // Server�κ��� ������ �ޱ�
        byteBuffer = ByteBuffer.allocate(100);
        socketChannel.read(byteBuffer);
        byteBuffer.flip();

        String data = charset.decode((byteBuffer)).toString();
        System.out.println("Received Data : " + data);

        if (socketChannel.isOpen()) {
            socketChannel.close();
        }
    }
    
    public static void main(String[] args) {
    }
}
