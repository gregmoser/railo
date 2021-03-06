package railo.runtime.type.util;

import railo.commons.lang.StringUtil;
import railo.runtime.ComponentImpl;
import railo.runtime.component.Member;
import railo.runtime.component.Property;
import railo.runtime.op.Caster;
import railo.runtime.type.Collection;
import railo.runtime.type.Collection.Key;
import railo.runtime.type.KeyImpl;
import railo.runtime.type.UDF;
import railo.runtime.type.UDFAddProperty;
import railo.runtime.type.UDFGetterProperty;
import railo.runtime.type.UDFHasProperty;
import railo.runtime.type.UDFRemoveProperty;
import railo.runtime.type.UDFSetterProperty;

public class PropertyFactory {

	public static final Collection.Key SINGULAR_NAME = KeyImpl.getInstance("singularName");
	public static final Key FIELD_TYPE = KeyImpl.getInstance("fieldtype");

	public static void addGet(ComponentImpl comp, Property prop) {
		Member m = comp.getMember(ComponentImpl.ACCESS_PRIVATE,KeyImpl.init("get"+prop.getName()),true,false);
		if(!(m instanceof UDF)){
			UDF udf = new UDFGetterProperty(comp,prop);
			comp.registerUDF(udf.getFunctionName(), udf);
		}
	}

	public static void addSet(ComponentImpl comp, Property prop) {
		Member m = comp.getMember(ComponentImpl.ACCESS_PRIVATE,KeyImpl.init("set"+prop.getName()),true,false);
		if(!(m instanceof UDF)){
			UDF udf = new UDFSetterProperty(comp,prop);
			comp.registerUDF(udf.getFunctionName(), udf);
		}
	}
	
	public static void addHas(ComponentImpl comp, Property prop) {
		Member m = comp.getMember(ComponentImpl.ACCESS_PRIVATE,KeyImpl.init("has"+getSingularName(prop)),true,false);
		if(!(m instanceof UDF)){
			UDF udf = new UDFHasProperty(comp,prop);
			comp.registerUDF(udf.getFunctionName(), udf);
		}
	}

	public static void addAdd(ComponentImpl comp, Property prop) {
		Member m = comp.getMember(ComponentImpl.ACCESS_PRIVATE,KeyImpl.init("add"+getSingularName(prop)),true,false);
		if(!(m instanceof UDF)){
			UDF udf = new UDFAddProperty(comp,prop);
			comp.registerUDF(udf.getFunctionName(), udf);
		}
	}

	public static void addRemove(ComponentImpl comp, Property prop) {
		Member m = comp.getMember(ComponentImpl.ACCESS_PRIVATE,KeyImpl.init("remove"+getSingularName(prop)),true,false);
		if(!(m instanceof UDF)){
			UDF udf = new UDFRemoveProperty(comp,prop);
			comp.registerUDF(udf.getFunctionName(), udf);
		}
	}

	public static String getSingularName(Property prop) {
		String singularName=Caster.toString(prop.getMeta().get(SINGULAR_NAME,null),null);
		if(!StringUtil.isEmpty(singularName)) return singularName;
		return prop.getName();
	}
	
	public static String getType(Property prop){
		String type = prop.getType();
		if(StringUtil.isEmpty(type) || "any".equalsIgnoreCase(type) || "object".equalsIgnoreCase(type)){
			String fieldType = Caster.toString(prop.getMeta().get(FIELD_TYPE,null),null);
			if("one-to-many".equalsIgnoreCase(fieldType) || "many-to-many".equalsIgnoreCase(fieldType)){
				return "array";
			}
			return "any";
		}
        return type;
    }

}
