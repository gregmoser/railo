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
import railo.runtime.type.KeyImpl;
import railo.runtime.type.List;
import railo.runtime.type.Struct;



/**
 * A filter which uses the brightness of each pixel to lookup a color from a colormap. 
 */
public class LookupFilter extends PointFilter  implements DynFiltering {
	
	private Colormap colormap = new Gradient();
	
	/**
     * Construct a LookupFilter.
     */
    public LookupFilter() {
		canFilterIndexColorModel = true;
	}

	/**
     * Construct a LookupFilter.
     * @param colormap the color map
     */
	public LookupFilter(Colormap colormap) {
		canFilterIndexColorModel = true;
		this.colormap = colormap;
	}

    /**
     * Set the colormap to be used for the filter.
     * @param colormap the colormap
     * @see #getColormap
     */
	public void setColormap(Colormap colormap) {
		this.colormap = colormap;
	}

    /**
     * Get the colormap to be used for the filter.
     * @return the colormap
     * @see #setColormap
     */
	public Colormap getColormap() {
		return colormap;
	}

	public int filterRGB(int x, int y, int rgb) {
//		int a = rgb & 0xff000000;
		int r = (rgb >> 16) & 0xff;
		int g = (rgb >> 8) & 0xff;
		int b = rgb & 0xff;
		rgb = (r + g + b) / 3;
		return colormap.getColor(rgb/255.0f);
	}

	public String toString() {
		return "Colors/Lookup...";
	}

	public BufferedImage filter(BufferedImage src, BufferedImage dst ,Struct parameters) throws PageException {
		Object o;
		if((o=parameters.removeEL(KeyImpl.init("Colormap")))!=null)setColormap(ImageFilterUtil.toColormap(o,"Colormap"));
		if((o=parameters.removeEL(KeyImpl.init("Dimensions")))!=null){
			int[] dim=ImageFilterUtil.toDimensions(o,"Dimensions");
			setDimensions(dim[0],dim[1]);
		}

		// check for arguments not supported
		if(parameters.size()>0) {
			throw new FunctionException(ThreadLocalPageContext.get(), "ImageFilter", 3, "parameters", "the parameter"+(parameters.size()>1?"s":"")+" ["+List.arrayToList(parameters.keysAsString(),", ")+"] "+(parameters.size()>1?"are":"is")+" not allowed, only the following parameters are supported [Colormap, Dimensions]");
		}

		return filter(src, dst);
	}
}


