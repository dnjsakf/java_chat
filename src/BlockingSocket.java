import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

public class BlockingSocket {
    public static void main(String[] args)  throws IOException {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(true); // ���ŷ ���
        serverSocketChannel.bind(new InetSocketAddress(3000)); // ��Ʈ 12345�� ����

        while (true) {
            SocketChannel socketChannel = serverSocketChannel.accept(); // �� �κп��� ������ �ɶ����� ���ŷ
            InetSocketAddress inetSocketAddress = (InetSocketAddress) socketChannel.getRemoteAddress();
            System.out.println("Connected : " + inetSocketAddress.getHostName());

            Charset charset = Charset.forName("UTF-8");

            // Client�κ��� ���� �ޱ�
            ByteBuffer byteBuffer = ByteBuffer.allocate(100);
            socketChannel.read(byteBuffer); // Ŭ���̾�Ʈ�κ��� ������ �б�
            byteBuffer.flip();
            System.out.println("Received Data : " + charset.decode(byteBuffer).toString());

            // "Hello Client"�� ���� ������
            byteBuffer = charset.encode("Hello Client");
            socketChannel.write(byteBuffer);

            System.out.println("Sending Success");
        }
    }
}
