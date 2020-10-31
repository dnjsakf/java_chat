import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

public class NonBlockingSocket extends Thread {
    private Selector selector = null;
    private Vector<SocketChannel> room = new Vector<SocketChannel>();

    NonBlockingSocket(int port) throws IOException {
        
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.bind(new InetSocketAddress(port));

        // �������� ä���� �����Ϳ� ����Ѵ�.
        selector = Selector.open();
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
    }

    private void startServer() throws Exception {
        System.out.println("Server Start");

        while (true) {
            selector.select(); //select() �޼ҵ�� �غ�� �̺�Ʈ�� �ִ��� Ȯ���Ѵ�.

            Set<SelectionKey> selectionKeySet = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeySet.iterator();

            while (iterator.hasNext()) {
                SelectionKey selectionKey = iterator.next();

                if (selectionKey.isAcceptable()) {
                    accept(selectionKey);
                }
                else if (selectionKey.isReadable()) {
                    read(selectionKey);
                }

                iterator.remove();
            }
        }
    }

    private void accept(SelectionKey key) throws Exception {
        ServerSocketChannel server = (ServerSocketChannel) key.channel();

        // �������� accept() �޼ҵ�� ���������� �����Ѵ�.
        SocketChannel socketChannel = server.accept();
        // ������ ����ä���� �� ���ŷ�� �б� ���� �����Ϳ� ����Ѵ�.

        if (socketChannel == null)
            return;

        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_READ);

        room.add(socketChannel); // ������ �߰�
        System.out.println(socketChannel.toString() + "Ŭ���̾�Ʈ�� �����߽��ϴ�.");
    }

    private void read(SelectionKey key) {
        // SelectionKey �κ��� ����ä���� ��´�.
        SocketChannel socketChannel = (SocketChannel) key.channel();
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1024); // buffer ����

        try {
            socketChannel.read(byteBuffer); // Ŭ���̾�Ʈ �������κ��� �����͸� ����
        }
        catch (IOException ex) {
            try {
                socketChannel.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }

            room.remove(socketChannel);
            ex.printStackTrace();
        }

        try {
            broadcast(byteBuffer);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        byteBuffer.clear();
    }

    private void broadcast(ByteBuffer byteBuffer) throws IOException {
        byteBuffer.flip();
        Iterator<SocketChannel> iterator = room.iterator();

        while (iterator.hasNext()) {
            SocketChannel socketChannel = iterator.next();

            if (socketChannel != null) {
                socketChannel.write(byteBuffer);
                byteBuffer.rewind();
            }
        }
    }
    
    public void run() {
        try {
            startServer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void main (String[] args) {
        try {
            NonBlockingSocket socket = new NonBlockingSocket(3000);
            
            socket.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
