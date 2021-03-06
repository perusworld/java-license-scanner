/**
 * com.yosanai.licensescanner.CentralRepoLicenseFinder
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

import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;

/**
 * @author Saravana Perumal Shanmugam
 * 
 */
public class CentralRepoLicenseFinder implements LicenseFinder {

    protected Map<String, Set<License>> known = new HashMap<String, Set<License>>();

    /**
     * 
     */
    public CentralRepoLicenseFinder() {
    }

    /**
     * @param known
     */
    public CentralRepoLicenseFinder(Map<String, Set<License>> known) {
        super();
        this.known = known;
    }

    /*
     * (non-Jsdoc)
     * 
     * @see com.yosanai.licensescanner.LicenseFinder#getLicenses(com.yosanai.
     * licensescanner.Artifact)
     */
    @Override
    public void getLicenses(Artifact artifact) throws Exception {
        if (null != artifact && artifact.getLicenses().isEmpty()) {
            Set<License> licenses = null;
            String key = artifact.getGroup() + "." + artifact.getArtifact() + "." + artifact.getVersion();
            if (null != known && known.containsKey(key)) {
                licenses = known.get(key);
            } else {
                if (null != known) {
                    for (String knownKey : known.keySet()) {
                        if (key.matches(knownKey)) {
                            licenses = known.get(knownKey);
                            break;
                        }
                    }
                }
                if (null == licenses) {
                    licenses = new HashSet<License>();
                }
            }
            if (licenses.isEmpty()) {
                String searchQuery = "http://search.maven.org/remotecontent?filepath=" + artifact.getGroup().replaceAll("\\.", "/") + "/" + artifact.getArtifact() + "/" + artifact.getVersion() + "/"
                        + artifact.getArtifact() + "-" + artifact.getVersion() + ".pom";
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                IOUtils.copy(new URL(searchQuery).openStream(), bos);
                String pom = new String(bos.toByteArray());
            }
            artifact.getLicenses().addAll(licenses);
        }
    }
}
