/**
 * com.yosanai.licensescanner.CentralRepoArtifactFinder
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
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

/**
 * @author Saravana Perumal Shanmugam
 * 
 */
public class CentralRepoArtifactFinder implements ArtifactFinder {

    protected Map<String, Artifact> known = new HashMap<String, Artifact>();

    /**
 * 
 */
    public CentralRepoArtifactFinder() {
    }

    /**
     * @param known
     */
    public CentralRepoArtifactFinder(Map<String, Artifact> known) {
        super();
        this.known = known;
    }

    /*
     * (non-Jsdoc)
     * 
     * @see com.yosanai.licensescanner.ArtifactFinder#findArtifact(java.net.URL)
     */
    @Override
    public Artifact findArtifact(URL file) throws Exception {
        Artifact ret = null;
        String fileName = file.getPath().substring(file.getPath().lastIndexOf('/') + 1).replaceAll("\\.jar$", "");
        ret = known.get(fileName);
        if (null != ret) {
            String artifact = fileName.replaceAll("\\-[\\d\\.][-\\d\\w\\.]+$", "");
            String version = fileName.replace(artifact, "").substring(1);
            String searchQuery = "http://search.maven.org/solrsearch/select?q=a:\"" + artifact + "\"%20AND%20v:\"" + version + "\"&wt=json";
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            IOUtils.copy(new URL(searchQuery).openStream(), bos);
            JSONObject jsonObj = new JSONObject(new String(bos.toByteArray()));
            JSONObject response = jsonObj.getJSONObject("response");
            if (null != response && 1 == response.getInt("numFound")) {
                JSONObject jsonObject = response.getJSONArray("docs").getJSONObject(0);
                if (null != jsonObject) {
                    ret = new Artifact(jsonObject.getString("g"), jsonObject.getString("a"), jsonObject.getString("v"));
                }
            } else {
                // multiple matches so ignore for now
            }
        }
        return ret;
    }
}
