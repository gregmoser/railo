/*
*

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package railo.runtime.img.filter;
import railo.runtime.type.KeyImpl;
import railo.runtime.engine.ThreadLocalPageContext;
import railo.runtime.exp.PageException;
import railo.runtime.type.Struct;
import java.awt.image.BufferedImage;
import railo.runtime.type.List;
import railo.runtime.exp.FunctionException;

import railo.runtime.type.KeyImpl;
import railo.runtime.engine.ThreadLocalPageContext;
import railo.runtime.exp.PageException;
import railo.runtime.type.Struct;
import java.awt.image.BufferedImage;
import railo.runtime.type.List;
import railo.runtime.exp.FunctionException;

import java.awt.image.BufferedImage;

import railo.runtime.engine.ThreadLocalPageContext;
import railo.runtime.exp.FunctionException;
import railo.runtime.exp.PageException;
import railo.runtime.img.math.Function2D;
import railo.runtime.type.KeyImpl;
import railo.runtime.type.List;
import railo.runtime.type.Struct;

public class MapFilter extends TransformFilter  implements DynFiltering {

	private Function2D xMapFunction;
	private Function2D yMapFunction;

	public MapFilter() {
	}
	
	public void setXMapFunction(Function2D xMapFunction) {
		this.xMapFunction = xMapFunction;
	}

	public Function2D getXMapFunction() {
		return xMapFunction;
	}

	public void setYMapFunction(Function2D yMapFunction) {
		this.yMapFunction = yMapFunction;
	}

	public Function2D getYMapFunction() {
		return yMapFunction;
	}
	
	protected void transformInverse(int x, int y, float[] out) {
		float xMap, yMap;
		xMap = xMapFunction.evaluate(x, y);
		yMap = yMapFunction.evaluate(x, y);
		out[0] = xMap * transformedSpace.width;
		out[1] = yMap * transformedSpace.height;
	}

	public String toString() {
		return "Distort/Map Coordinates...";
	}
	public BufferedImage filter(BufferedImage src, BufferedImage dst ,Struct parameters) throws PageException {
		Object o;
		if((o=parameters.removeEL(KeyImpl.init("XMapFunction")))!=null)setXMapFunction(ImageFilterUtil.toFunction2D(o,"XMapFunction"));
		if((o=parameters.removeEL(KeyImpl.init("YMapFunction")))!=null)setYMapFunction(ImageFilterUtil.toFunction2D(o,"YMapFunction"));
		if((o=parameters.removeEL(KeyImpl.init("EdgeAction")))!=null)setEdgeAction(ImageFilterUtil.toIntValue(o,"EdgeAction"));
		if((o=parameters.removeEL(KeyImpl.init("Interpolation")))!=null)setInterpolation(ImageFilterUtil.toIntValue(o,"Interpolation"));

		// check for arguments not supported
		if(parameters.size()>0) {
			throw new FunctionException(ThreadLocalPageContext.get(), "ImageFilter", 3, "parameters", "the parameter"+(parameters.size()>1?"s":"")+" ["+List.arrayToList(parameters.keysAsString(),", ")+"] "+(parameters.size()>1?"are":"is")+" not allowed, only the following parameters are supported [XMapFunction, YMapFunction, EdgeAction, Interpolation]");
		}

		return filter(src, dst);
	}
}
