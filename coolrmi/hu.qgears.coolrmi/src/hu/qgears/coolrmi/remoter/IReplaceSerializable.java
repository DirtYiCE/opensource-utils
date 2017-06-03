package hu.qgears.coolrmi.remoter;

import java.io.Serializable;

import hu.qgears.coolrmi.serializer.PortableSerializable;

/**
 * This interface must be implemented by serialization replacer objects.
 * See {@link CoolRMIReplaceEntry}
 * @author rizsi
 *
 */
public interface IReplaceSerializable extends Serializable, PortableSerializable {
	/**
	 * This method is called after deserialization to create the equivalent of the original object.
	 * @return
	 */
	Object readResolve();

}
