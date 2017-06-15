package hu.qgears.coolrmi.example;

import java.net.InetSocketAddress;

import hu.qgears.coolrmi.CoolRMIServer;
import hu.qgears.coolrmi.CoolRMIService;
import hu.qgears.coolrmi.remoter.AbstractSerializer;
import hu.qgears.coolrmi.remoter.JavaSerializer;
import hu.qgears.coolrmi.serializer.PortableSerializer;

public class Server {
	public static int port=9000;

	public static AbstractSerializer getSerializer(String[] args) {
		String serializer = "portable";
		if (args.length >= 1) {
			serializer = args[0];
		}

		switch (serializer) {
		case "portable":
			PortableSerializer ser = new PortableSerializer();
			ser.addNamespaceMapping("hu.qgears.coolrmi.example",
					"coolrmi.example");
			return ser;
		case "dotnet":
			return new JavaSerializer();
		default:
			throw new RuntimeException("Unknown serializer " + serializer);
		}
	}

	public static void main(String[] args) throws Exception {
		// Create server object coonfigured to a TCP port
		CoolRMIServer s=new CoolRMIServer(Server.class.getClassLoader(), new InetSocketAddress("localhost", port), true);
		s.setSerializer(getSerializer(args));
		// (We don't need to configure RMI on the server side because the server does not send proxy objects or
		// non-serializable replaced objects)
		// Add service to the server
		s.getServiceRegistry().addService(new CoolRMIService(IService.id, IService.class, new Service()));
		// Start the server (TCP port is opened here). The server runs on a non-dameon thread so it keeps the process alive.
		s.start();
	}
}
