package hu.qgears.nativeloader;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * NativeLoader which fetches library data from an XML file.
 * 
 * @author rizsi
 * 
 */
public abstract class XmlNativeLoader2 implements INativeLoader {

	private static final Logger LOG = Logger.getLogger(XmlNativeLoader2.class);
	
	public static final String IMPLEMENTATIONS = "implementations.xml";

	protected class ImplementationsHandler extends DefaultHandler
	{
		public NativesToLoad nativesToLoad;
		private boolean loadThis=false;
		public ImplementationsHandler() {
		}
		@Override
		public void startElement(String uri, String localName, String qName,
				Attributes attributes) throws SAXException {
			if("implementation".equals(localName))
			{
				String path=attributes.getValue("path");
				if(path!=null)
				{
					URL resource=getResource(path);
					if(resource!=null)
					{
						try
						{
							NativesToLoad ret=checkAndParse(resource, path);
							if(ret!=null)
							{
								nativesToLoad=ret;
							}
						}catch(Exception e)
						{
							LOG.error("checkAndParse",e);
						}
					}
				}else
				{
					if(matches(attributes)&&nativesToLoad==null)
					{
						nativesToLoad=new NativesToLoad();
						loadThis=true;
					}
				}
			}
			else if(loadThis)
			{
				tryLoad(uri, localName, qName, attributes, nativesToLoad, "");
			}
		}
		@Override
		public void endElement(String uri, String localName, String qName)
				throws SAXException {
			if("implementation".equals(localName))
			{
				loadThis=false;
			}
		}
		
	}
	
	class ImplHandler extends DefaultHandler
	{
		public NativesToLoad result;
		private String prefix;
		public ImplHandler(String prefix) {
			super();
			this.prefix = prefix;
		}
		@Override
		public void startElement(String uri, String localName, String qName,
				Attributes attributes) throws SAXException {
			if("platform".equals(localName))
			{
				if(matches(attributes))
				{
					result=new NativesToLoad();
				}
			}
			if(result!=null)
			{
				tryLoad(uri, localName, qName, attributes, result, prefix);
			}
		}
	}
	
	private boolean matches(Attributes attributes)
	{
		String implName=attributes.getValue("name");
		String implArch=attributes.getValue("arch");
		return name.equals(implName)&&implArch.equals(arch);
	}
	
	private void tryLoad(String uri, String localName, String qName,
			Attributes attributes, NativesToLoad result, String prefix) {
		if("lib".equals(localName))
		{
			String path=attributes.getValue("path");
			String installPath=attributes.getValue("installPath");
			result.getBinaries().add(new NativeBinary(prefix+path, installPath));
		}
	}

	/**
	 * Subclasses may override so they can use different file name.
	 * @return
	 */
	public String getNativesDeclarationResourceName()
	{
		return IMPLEMENTATIONS;
	}

	private NativesToLoad checkAndParse(URL resource, String path) {
		String prefix=getPrefix(path);
		ImplHandler ih=new ImplHandler(prefix);
		parseUsingHandler(resource, ih);
		return ih.result;
	}

	private String getPrefix(String path) {
		int idx=path.lastIndexOf("/");
		if(idx<0)
		{
			return "";
		}else
		{
			return path.substring(0, idx+1);
		}
	}

	protected URL getResource(String path) {
		URL resource=getClass().getResource(path);
		return resource;
	}
	private String arch;
	private String name;
	@Override
	public NativesToLoad getNatives(String arch, String name)
			throws NativeLoadException {
		this.arch=arch;
		this.name=name;
		ImplementationsHandler handler = new ImplementationsHandler();
		parseUsingHandler(getClass().getResource(getNativesDeclarationResourceName()), handler);
		return handler.nativesToLoad;
	}

	private void parseUsingHandler(URL resource, DefaultHandler handler) {
		try {
			XMLReader reader = XMLReaderFactory.createXMLReader();
			InputStream istream = resource.openStream();
			try
			{
				InputSource isource = new InputSource(istream);
				reader.setContentHandler(handler);
				reader.parse(isource);
			}finally
			{
				istream.close();
			}
		} catch (SAXException e) {
			throw new NativeLoadException(e);
		} catch (IOException e) {
			throw new NativeLoadException(e);
		}
	}
}
