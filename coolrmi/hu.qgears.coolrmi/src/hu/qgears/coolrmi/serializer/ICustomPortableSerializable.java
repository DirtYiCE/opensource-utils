package hu.qgears.coolrmi.serializer;

import java.io.IOException;
import java.io.OutputStream;

public interface ICustomPortableSerializable {
	// ctor: PortableSerializer, InputStream
	// or: public static deserialize(PortableSerializer, InputStream)

	void serialize(PortableSerializer serializer, OutputStream os)
			throws IOException;
}
