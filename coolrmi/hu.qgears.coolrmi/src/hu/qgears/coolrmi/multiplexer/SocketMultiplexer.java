package hu.qgears.coolrmi.multiplexer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import hu.qgears.coolrmi.messages.AbstractCoolRMIMessage;



/**
 * Multiplexes several messages on a single TCP
 * connection to both direction.
 * 
 * Both write and read has an own thread.
 * 
 * @author rizsi
 *
 */
public class SocketMultiplexer {
	private ISocketMultiplexerListener messageListener;
	private InputStream is;
	private OutputStream os;
	private boolean exit=false;
	/**
	 * True means that the next messages can not be sent.
	 */
	private boolean disconnected;
	private LinkedList<SocketMultiplexerSource> messagesToSend=new LinkedList<SocketMultiplexerSource>();
	public SocketMultiplexer(InputStream is, OutputStream os,
			ISocketMultiplexerListener messageListener) throws IOException {
		super();
		this.is = is;
		this.messageListener=messageListener;
		this.os=os;
	}
	public void start()
	{
		new ReadThread().start();
		new WriteThread().start();
	}

	class ReadThread extends Thread
	{
		public ReadThread() {
			super("Cool RMI read thread");
		}

		@Override
		public void run() {
			try {
				try
				{
					while(!exit)
					{
						SocketMultiplexerDatagram datagram=SocketMultiplexerDatagram.readFromStream(is);
						messageListener.messageReceived(datagram.getContent());
					}
				}finally
				{
					is.close();
				}
			} catch (Exception e) {
				messageListener.pipeBroken(e);
			}
		}
	}

	class WriteThread extends Thread
	{
		public WriteThread() {
			super("Cool RMI write thread");
		}

		@Override
		public void run() {
			while(!exit)
			{
				SocketMultiplexerSource source = null;
				synchronized (messagesToSend) {
					if(messagesToSend.isEmpty())
					{
						try {
							messagesToSend.wait();
						} catch (InterruptedException e) {}
					}

					if (!messagesToSend.isEmpty()) {
						source = messagesToSend.removeFirst();
					}
				}
				if(source!=null)
				{
					SocketMultiplexerDatagram datagram=new SocketMultiplexerDatagram(source.getToSend());

					try {
						datagram.writeToStream(os);
						os.flush();
					} catch (IOException e) {
						synchronized (messagesToSend) {
							disconnected=true;
						}
						messageListener.pipeBroken(e);
					}

					source.sent();
				}
			}
		}
	}
	public void addMessageToSend(byte[] messageContent, AbstractCoolRMIMessage message)
	{
		boolean b;
		synchronized (messagesToSend) {
			messagesToSend.add(new SocketMultiplexerSource(messageContent, message));
			messagesToSend.notifyAll();
			b=disconnected;
		}
		if(b)
		{
			message.sent();
		}
	}
	public void stop()
	{
		exit=true;
		List<SocketMultiplexerSource> toCancel=null;
		synchronized (messagesToSend) {
			messagesToSend.notifyAll();
			toCancel=new ArrayList<SocketMultiplexerSource>(messagesToSend);
		}
		if(toCancel!=null)
		{
			for(SocketMultiplexerSource s: toCancel)
			{
				s.sent();
			}
		}
	}
}
