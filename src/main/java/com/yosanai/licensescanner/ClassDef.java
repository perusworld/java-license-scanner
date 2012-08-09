/**
 * com.yosanai.licensescanner.ClassDef
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

import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Saravana Perumal Shanmugam
 * 
 */
public class ClassDef {

    protected Class<?> classObj;

    protected URL container;

    protected List<Class<?>> hierarchy = new ArrayList<Class<?>>();

    protected Map<Class<?>, URL> filteredHierarchy = new LinkedHashMap<Class<?>, URL>();

    protected Map<Class<?>, Artifact> filteredArtifact = new LinkedHashMap<Class<?>, Artifact>();

    /**
     * @param classObj
     * @param container
     */
    public ClassDef(Class<?> classObj, URL container) {
        super();
        this.classObj = classObj;
        this.container = container;
    }

    /**
     * @return the classObj
     */
    public Class<?> getClassObj() {
        return classObj;
    }

    /**
     * @param classObj
     *            the classObj to set
     */
    public void setClassObj(Class<?> classObj) {
        this.classObj = classObj;
    }

    /**
     * @return the container
     */
    public URL getContainer() {
        return container;
    }

    /**
     * @param container
     *            the container to set
     */
    public void setContainer(URL container) {
        this.container = container;
    }

    /**
     * @return the hierarchy
     */
    public List<Class<?>> getHierarchy() {
        return hierarchy;
    }

    /**
     * @param hierarchy
     *            the hierarchy to set
     */
    public void setHierarchy(List<Class<?>> hierarchy) {
        this.hierarchy = hierarchy;
    }

    /**
     * @return the filteredHierarchy
     */
    public Map<Class<?>, URL> getFilteredHierarchy() {
        return filteredHierarchy;
    }

    /**
     * @param filteredHierarchy
     *            the filteredHierarchy to set
     */
    public void setFilteredHierarchy(Map<Class<?>, URL> filteredHierarchy) {
        this.filteredHierarchy = filteredHierarchy;
    }

    /**
     * @return the filteredArtifact
     */
    public Map<Class<?>, Artifact> getFilteredArtifact() {
        return filteredArtifact;
    }

    /**
     * @param filteredArtifact
     *            the filteredArtifact to set
     */
    public void setFilteredArtifact(Map<Class<?>, Artifact> filteredArtifact) {
        this.filteredArtifact = filteredArtifact;
    }

}
