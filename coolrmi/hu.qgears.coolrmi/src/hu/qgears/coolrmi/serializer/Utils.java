package hu.qgears.coolrmi.serializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

public final class Utils {
	private final static ThreadLocal<ByteBuffer> buf = new ThreadLocal<ByteBuffer>() {
		@Override
		protected ByteBuffer initialValue() {
			return ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);
		}
	};

	public static void writeBool(OutputStream os, boolean b) throws IOException {
		os.write(b ? 1 : 0);
	}

	public static boolean readBool(InputStream is) throws IOException {
		return is.read() != 0;
	}


	public static void write16(OutputStream os, short s) throws IOException {
		os.write(buf.get().putShort(0, s).array(), 0, 2);
	}

	public static short read16(InputStream is) throws IOException {
		ByteBuffer b = buf.get();
		is.read(b.array(), 0, 2);
		return b.getShort(0);
	}


	public static void write32(OutputStream os, int i) throws IOException {
		os.write(buf.get().putInt(0, i).array(), 0, 4);
	}

	public static int read32(InputStream is) throws IOException {
		ByteBuffer b = buf.get();
		is.read(b.array(), 0, 4);
		return b.getInt(0);
	}


	public static void write64(OutputStream os, long l) throws IOException {
		os.write(buf.get().putLong(0, l).array(), 0, 8);
	}

	public static long read64(InputStream is) throws IOException {
		ByteBuffer b = buf.get();
		is.read(b.array(), 0, 8);
		return b.getLong(0);
	}


	public static void writeString(OutputStream os, String s) throws IOException {
		if (s == null) {
			write32(os, -1);
			return;
		}
		byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
		write32(os, bytes.length);
		os.write(bytes);
	}

	public static String readString(InputStream is) throws IOException {
		int len = read32(is);
		if (len == -1) {
			return null;
		}

		byte[] bytes = new byte[len];
		is.read(bytes);
		return new String(bytes, StandardCharsets.UTF_8);
	}
}
