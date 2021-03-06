package railo.runtime.dump;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpSession;

import org.apache.xerces.dom.AttributeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import railo.commons.date.TimeZoneUtil;
import railo.commons.io.res.Resource;
import railo.commons.lang.IDGenerator;
import railo.commons.lang.StringUtil;
import railo.runtime.PageContext;
import railo.runtime.converter.WDDXConverter;
import railo.runtime.exp.PageException;
import railo.runtime.op.Caster;
import railo.runtime.op.Decision;
import railo.runtime.text.xml.XMLAttributes;
import railo.runtime.text.xml.XMLCaster;
import railo.runtime.type.Array;
import railo.runtime.type.Collection;
import railo.runtime.type.ObjectWrap;
import railo.runtime.type.QueryImpl;
import railo.runtime.type.dt.DateTimeImpl;

public class DumpUtil {

	
	public static DumpData toDumpData(Object o,PageContext pageContext, int maxlevel, DumpProperties props) {
		if(maxlevel<=0) {
			return new SimpleDumpData("maximal dump level reached");
		}
		// null
		if(o == null) {
			DumpTable table=new DumpTablePro("null","#ff6600","#ffcc99","#000000");
			table.appendRow(new DumpRow(0,new SimpleDumpData("Empty:null")));
			return table;
		}
		if(o instanceof DumpData) {
			return ((DumpData)o);
		}
		// Date
		if(o instanceof Date) {
			return new DateTimeImpl((Date) o).toDumpData(pageContext,maxlevel,props);
		}
		// Calendar
		if(o instanceof Calendar) {
			Calendar c=(Calendar)o;
			
			SimpleDateFormat df = new SimpleDateFormat("EE, dd MMM yyyy HH:mm:ss zz",Locale.ENGLISH);
			df.setTimeZone(c.getTimeZone());
			
			DumpTable table=new DumpTablePro("date","#ff9900","#ffcc00","#000000");
			table.setTitle("java.util.Calendar");
			table.appendRow(1, new SimpleDumpData("Timezone"), new SimpleDumpData(TimeZoneUtil.toString(c.getTimeZone())));
			table.appendRow(1, new SimpleDumpData("Time"), new SimpleDumpData(df.format(c.getTime())));
	        
			return table;
		}
		// StringBuffer
		if(o instanceof StringBuffer) {
			DumpTable dt=(DumpTable)toDumpData(o.toString(), pageContext, maxlevel, props);
			if(StringUtil.isEmpty(dt.getTitle()))
				dt.setTitle(Caster.toClassName(o));
			return dt;
		}
		// String
		if(o instanceof String) {
			String str=(String) o;
			if(str.trim().startsWith("<wddxPacket ")) {
				try {
					WDDXConverter converter =new WDDXConverter(pageContext.getTimeZone(),false);
					converter.setTimeZone(pageContext.getTimeZone());
					Object rst = converter.deserialize(str,false);
					DumpData data = toDumpData(rst, pageContext, maxlevel, props);
					
					DumpTable table = new DumpTablePro("string","#cc9999","#ffffff","#000000");
					table.setTitle("WDDX");
					table.appendRow(1,new SimpleDumpData("encoded"),data);
					table.appendRow(1,new SimpleDumpData("raw"),new SimpleDumpData(str));
					return table;
				}
				catch(Throwable t) {}
			}
			DumpTable table = new DumpTablePro("string","#ff6600","#ffcc99","#000000");
			table.appendRow(1,new SimpleDumpData("string"),new SimpleDumpData(str));
			return table;
		}
		// Character
		if(o instanceof Character) {
			DumpTable table = new DumpTablePro("character","#ff6600","#ffcc99","#000000");
			table.appendRow(1,new SimpleDumpData("character"),new SimpleDumpData(o.toString()));
			return table;
		}
		// Number
		if(o instanceof Number) {
			DumpTable table = new DumpTablePro("numeric","#ff6600","#ffcc99","#000000");
			table.appendRow(1,new SimpleDumpData("number"),new SimpleDumpData(Caster.toString(((Number)o).doubleValue())));
			return table;
		}
		// Boolean
		if(o instanceof Boolean) {
			DumpTable table = new DumpTablePro("boolean","#ff6600","#ffcc99","#000000");
			table.appendRow(1,new SimpleDumpData("boolean"),new SimpleDumpData(((Boolean)o).booleanValue()));
			return table;
		}
		// File
		if(o instanceof File) {
			DumpTable table = new DumpTablePro("file","#ffcc00","#ffff66","#000000");
			table.appendRow(1,new SimpleDumpData("File"),new SimpleDumpData(o.toString()));
			return table;
		}
		// Resource
		if(o instanceof Resource) {
			DumpTable table = new DumpTablePro("resource","#ffcc00","#ffff66","#000000");
			table.appendRow(1,new SimpleDumpData("Resource"),new SimpleDumpData(o.toString()));
			return table;
		}
		// byte[]
		if(o instanceof byte[]) {
			byte[] bytes=(byte[]) o;
			
			DumpTable table = new DumpTablePro("array","#ff9900","#ffcc00","#000000");
			table.setTitle("Native Array  ("+Caster.toClassName(o)+")");
			
			StringBuffer sb=new StringBuffer();
			for(int i=0;i<bytes.length;i++) {
				if(i!=0)sb.append("-");
				sb.append(bytes[i]);
				if(i==1000) {
					sb.append("  [truncated]  ");
					break;
				}
			}
			table.appendRow(0,new SimpleDumpData(sb.toString()));
			return table;	
		}
		// Collection.Key
		if(o instanceof Collection.Key) {
			Collection.Key key=(Collection.Key) o;
			DumpTable table = new DumpTablePro("string","#ff6600","#ffcc99","#000000");
			table.appendRow(1,new SimpleDumpData("Collection.Key"),new SimpleDumpData(key.getString()));
			return table;
		}
		
		
		String id=""+IDGenerator.intId();
		String refid=ThreadLocalDump.get(o);
		if(refid!=null) {
			DumpTablePro table = new DumpTablePro("ref","#ffffff","#cccccc","#000000");
			table.appendRow(1,new SimpleDumpData("Reference"),new SimpleDumpData(refid));
			table.setRef(refid);
			return setId(id,table);
		}
		
		ThreadLocalDump.set(o,id);
		try{
			// Printable
			if(o instanceof Dumpable) {
				return setId(id,((Dumpable)o).toDumpData(pageContext,maxlevel,props));
			}
			// Map
			if(o instanceof Map) {
				Map map=(Map) o;
				Iterator it=map.keySet().iterator();
	
				DumpTable table = new DumpTablePro("struct","#ff9900","#ffcc00","#000000");
				table.setTitle("Map ("+Caster.toClassName(o)+")");
				
				while(it.hasNext()) {
					Object next=it.next();
					table.appendRow(1,toDumpData(next,pageContext,maxlevel,props),toDumpData(map.get(next),pageContext,maxlevel,props));
				}
				return setId(id,table);
			}
		
			// List
			if(o instanceof List) {
				List list=(List) o;
				ListIterator it=list.listIterator();
				
				DumpTable table = new DumpTablePro("array","#ff9900","#ffcc00","#000000");
				table.setTitle("Array (List)");
				
				while(it.hasNext()) {
					table.appendRow(1,new SimpleDumpData(it.nextIndex()+1),toDumpData(it.next(),pageContext,maxlevel,props));
				}
				return setId(id,table);
			}
			// Resultset
			if(o instanceof ResultSet) {
				try {
					DumpData dd = new QueryImpl((ResultSet)o,"query").toDumpData(pageContext,maxlevel,props);
					if(dd instanceof DumpTable)
						((DumpTable)dd).setTitle(Caster.toClassName(o));
					return setId(id,dd);
				} 
				catch (PageException e) {
					
				}
			}
			// Enumeration
			if(o instanceof Enumeration) {
				Enumeration e=(Enumeration)o;
				
				DumpTable table = new DumpTablePro("enumeration","#ff9900","#ffcc00","#000000");
				table.setTitle("Enumeration");
				
				while(e.hasMoreElements()) {
					table.appendRow(0,toDumpData(e.nextElement(),pageContext,maxlevel,props));
				}
				return setId(id,table);
			}
			// Object[]
			if(Decision.isNativeArray(o)) {
				Array arr;
				try {
					arr = Caster.toArray(o);
					DumpTable htmlBox = new DumpTablePro("array","#ff9900","#ffcc00","#000000");
					htmlBox.setTitle("Native Array ("+Caster.toClassName(o)+")");
				
					int length=arr.size();
				
					for(int i=1;i<=length;i++) {
						Object ox=null;
						try {
							ox = arr.getE(i);
						} catch (Exception e) {}
						htmlBox.appendRow(1,new SimpleDumpData(i),toDumpData(ox,pageContext,maxlevel,props));
					}
					return setId(id,htmlBox);
				} 
				catch (PageException e) {
					return setId(id,new SimpleDumpData(""));
				}
			}
			// Node
			if(o instanceof Node) {
			    return setId(id,XMLCaster.toDumpData((Node)o, pageContext,maxlevel,props));			
			}
			// ObjectWrap
			if(o instanceof ObjectWrap) {
				maxlevel++;
			    return setId(id,toDumpData(((ObjectWrap)o).getEmbededObject(null), pageContext,maxlevel,props));			
			}
			// NodeList
			if(o instanceof NodeList) {
				NodeList list=(NodeList)o;
				int len=list.getLength();
				DumpTable table = new DumpTablePro("xml","#cc9999","#ffffff","#000000");
				for(int i=0;i<len;i++) {
					table.appendRow(1,new SimpleDumpData(i),toDumpData(list.item(i),pageContext,maxlevel,props));
				}
				return setId(id,table);
				
			}
		// AttributeMap
		if(o instanceof AttributeMap) {
			return setId(id,new XMLAttributes((AttributeMap)o,false).toDumpData(pageContext, maxlevel,props));			
		}
			// HttpSession
			if(o instanceof HttpSession) {
			    HttpSession hs = (HttpSession)o;
			    Enumeration e = hs.getAttributeNames();
			    
			    DumpTable htmlBox = new DumpTablePro("httpsession","#9999ff","#ccccff","#000000");
				htmlBox.setTitle("HttpSession");
			    while(e.hasMoreElements()) {
			        String key=e.nextElement().toString();
			        htmlBox.appendRow(1,new SimpleDumpData(key),toDumpData(hs.getAttribute(key), pageContext,maxlevel,props));
			    }
			    return setId(id,htmlBox);
			}
		
		
		// reflect
		//else {
			DumpTable table = new DumpTablePro(o.getClass().getName(),"#cc9999","#ffcccc","#000000");
			
			Class clazz=o.getClass();
			if(o instanceof Class) clazz=(Class) o;
			String fullClassName=clazz.getName();
			int pos=fullClassName.lastIndexOf('.');
			String className=pos==-1?fullClassName:fullClassName.substring(pos+1);
			
			table.setTitle(className);
			table.appendRow(1,new SimpleDumpData("class"),new SimpleDumpData(fullClassName));
			
			// Fields
			Field[] fields=clazz.getFields();
			DumpTable fieldDump = new DumpTable("#cc9999","#ffcccc","#000000");
			fieldDump.appendRow(7,new SimpleDumpData("name"),new SimpleDumpData("pattern"),new SimpleDumpData("value"));
			for(int i=0;i<fields.length;i++) {
				Field field = fields[i];
				DumpData value;
				try {//print.out(o+":"+maxlevel);
					value=new SimpleDumpData(Caster.toString(field.get(o), ""));
				} 
				catch (Exception e) {
					value=new SimpleDumpData("");
				}
				fieldDump.appendRow(0,new SimpleDumpData(field.getName()),new SimpleDumpData(field.toString()),value);
			}
			if(fields.length>0)table.appendRow(1,new SimpleDumpData("fields"),fieldDump);
			
			// Methods
			StringBuffer objMethods=new StringBuffer();
			Method[] methods=clazz.getMethods();
			DumpTable methDump = new DumpTable("#cc9999","#ffcccc","#000000");
			methDump.appendRow(7,new SimpleDumpData("return"),new SimpleDumpData("interface"),new SimpleDumpData("exceptions"));
			for(int i=0;i<methods.length;i++) {
				Method method = methods[i];
				
				if(Object.class==method.getDeclaringClass()) {
					if(objMethods.length()>0)objMethods.append(", ");
					objMethods.append(method.getName());
					continue;
				}
				
				// exceptions
				StringBuffer sbExp=new StringBuffer();
				Class[] exceptions = method.getExceptionTypes();
				for(int p=0;p<exceptions.length;p++){
					if(p>0)sbExp.append("\n");
					sbExp.append(Caster.toClassName(exceptions[p]));
				}
				
				// parameters
				StringBuffer sbParams=new StringBuffer(method.getName());
				sbParams.append('(');
				Class[] parameters = method.getParameterTypes();
				for(int p=0;p<parameters.length;p++){
					if(p>0)sbParams.append(", ");
					sbParams.append(Caster.toClassName(parameters[p]));
				}
				sbParams.append(')');
				
				methDump.appendRow(0,
						new SimpleDumpData(Caster.toClassName(method.getReturnType())),

						new SimpleDumpData(sbParams.toString()),
						new SimpleDumpData(sbExp.toString())
				);
			}
			if(methods.length>0)table.appendRow(1,new SimpleDumpData("methods"),methDump);
			
			DumpTable inherited = new DumpTable("#cc9999","#ffcccc","#000000");
			inherited.appendRow(7,new SimpleDumpData("Methods inherited from java.lang.Object"));
			inherited.appendRow(0,new SimpleDumpData(objMethods.toString()));
			table.appendRow(1,new SimpleDumpData(""),inherited);
			return setId(id,table);
		//}
		}
		finally{
			ThreadLocalDump.remove(o);
		}
	}

	private static DumpData setId(String id, DumpData data) {
		if(data instanceof DumpTablePro) {
			((DumpTablePro)data).setId(id);
		}
		// TODO Auto-generated method stub
		return data;
	}

	public static boolean keyValid(DumpProperties props,int level, String key) {
		if(props.getMaxlevel()-level>1) return true;
		
		// show
		Set set = props.getShow();
		if(set!=null && !set.contains(StringUtil.toLowerCase(key)))
			return false;
		
		// hide
		set = props.getHide();
		if(set!=null && set.contains(StringUtil.toLowerCase(key)))
			return false;
		
		return true;
	}
	
	public static boolean keyValid(DumpProperties props,int level, Collection.Key key) {
		if(props.getMaxlevel()-level>1) return true;
		
		// show
		Set set = props.getShow();
		if(set!=null && !set.contains(key.getLowerString()))
			return false;
		
		// hide
		set = props.getHide();
		if(set!=null && set.contains(key.getLowerString()))
			return false;
		
		return true;
	}
	
	
	

	public static DumpProperties toDumpProperties() {
		return DumpProperties.DEFAULT;
	}
}
