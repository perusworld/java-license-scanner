/**
 * com.yosanai.licensescanner.Artifact
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

/**
 * @author Saravana Perumal Shanmugam
 * 
 */
public class Artifact {

    protected String group;

    protected String artifact;

    protected String version;

    /**
     * @param group
     * @param artifact
     * @param version
     */
    public Artifact(String group, String artifact, String version) {
        super();
        this.group = group;
        this.artifact = artifact;
        this.version = version;
    }

    /**
     * @return the group
     */
    public String getGroup() {
        return group;
    }

    /**
     * @return the artifact
     */
    public String getArtifact() {
        return artifact;
    }

    /**
     * @return the version
     */
    public String getVersion() {
        return version;
    }

}
