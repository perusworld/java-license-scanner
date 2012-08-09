/**
 * com.yosanai.licensescanner.SimpleFileNameFilter
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), 
 * to deal in the Software without restriction, including without limitation the rights to use, 
 * copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, 
 * and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, 
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR 
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, 
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */
package com.yosanai.licensescanner;

import java.io.File;
import java.io.FilenameFilter;

/**
 * @author Saravana Perumal Shanmugam
 * 
 */
public class SimpleFileNameFilter implements FilenameFilter {

    protected String nameInclusion;

    protected String pathInclusion;

    public SimpleFileNameFilter(String nameInclusion) {
        super();
        this.nameInclusion = nameInclusion;
    }

    /**
     * @param nameInclusion
     * @param pathInclusion
     */
    public SimpleFileNameFilter(String nameInclusion, String pathInclusion) {
        super();
        this.nameInclusion = nameInclusion;
        this.pathInclusion = pathInclusion;
    }

    @Override
    public boolean accept(File dir, String name) {
        return name.matches(this.nameInclusion) && (null == pathInclusion || dir.getAbsolutePath().matches(pathInclusion));
    }
}
