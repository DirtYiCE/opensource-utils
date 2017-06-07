package hu.qgears.coolrmi.multiplexer;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Piece of a message on a multiplexed
 * message stream.
 * @author rizsi
 *
 */
public class SocketMultiplexerDatagram implements Serializable {
	private static final long serialVersionUID = 1L;
	private byte[] content;

	public SocketMultiplexerDatagram(byte[] data) {
		this.content=data;
	}

	public byte[] getContent() {
		return content;
	}
	public void setContent(byte[] content) {
		this.content = content;
	}
	@Override
	public String toString() {
		return "datagram: "+content.length;
	}
	public static SocketMultiplexerDatagram readFromStream(InputStream is) throws IOException {
		byte[] header=new byte[4];
		readAll(is, header);
		ByteBuffer bb=ByteBuffer.wrap(header);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		int length=bb.getInt();

		byte[] data=new byte[length];
		readAll(is,  data);
		return new SocketMultiplexerDatagram(data);
	}
	private static void readAll(InputStream is, byte[] header) throws IOException {
		int at=0;
		while(at<header.length)
		{
			int n=is.read(header, at, header.length-at);
			if(n<0)
			{
				throw new EOFException();
			}
			at+=n;
		}
	}
	public void writeToStream(OutputStream os) throws IOException {
		byte[] header=new byte[4];
		ByteBuffer bb=ByteBuffer.wrap(header);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		bb.putInt(content.length);
		os.write(header);
		os.write(content);
	}
}
