package hu.qgears.coolrmi.multiplexer;

import hu.qgears.coolrmi.messages.AbstractCoolRMIMessage;

/**
 * A datagram source that must be sent.
 * @author rizsi
 *
 */
public class SocketMultiplexerSource {
	private byte[] toSend;
	private AbstractCoolRMIMessage message;

	public byte[] getToSend() {
		return toSend;
	}
	public SocketMultiplexerSource(byte[] toSend, AbstractCoolRMIMessage message) {
		super();
		this.toSend = toSend;
		this.message=message;
	}
	@Override
	public String toString() {
		return "Socket multiplexer source: "+message.getName();
	}
	/**
	 * Callback when the last piece of this message has been sent through the (TCP) channel.
	 */
	public void sent() {
		message.sent();
	}
}
