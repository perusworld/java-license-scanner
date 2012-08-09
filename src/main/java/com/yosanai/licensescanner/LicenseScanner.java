/**
 * com.yosanai.licensescanner.LicenseScanner
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
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import de.schlichtherle.truezip.file.TFile;

/**
 * @author Saravana Perumal Shanmugam
 * 
 */
public class LicenseScanner {

    /**
     * 
     */
    public static final String CLASS = "\\.class$";

    /**
     * 
     */
    public static final String COMMON_PACKAGES = "(java|javax)\\..*?$";

    /**
     * 
     */
    public static final String WEB_INF_CLASSES = "WEB-INF/classes";

    /**
     * 
     */
    public static final String WEB_INF_LIB = "WEB-INF/lib";

    /**
     * 
     */
    public static final String JAR = ".*\\.jar$";

    /**
     * 
     */
    public static final String FULL_CLASS = ".*\\.class$";

    /**
     * @author Saravana Perumal Shanmugam
     * 
     */

    private final class SimpleFileNameFilter implements FilenameFilter {

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

    public URL getJar(ScannerClassLoader classLoader, Class<?> classObj) throws Exception {
        URL ret = null;
        URL classURL = classLoader.findResource(classObj.getName().replace('.', '/').concat(".class"));
        if (null != classURL) {
            ret = new URL(classURL.toString().replaceAll("^\\w+\\:", "").replaceAll("\\!.*?$", ""));
        }
        return ret;
    }

    public Distribution load(File file, String proprietary) throws Exception {
        Distribution ret = new Distribution();
        if (file.exists()) {
            loadJars(file, ret, WEB_INF_LIB);
            loadClasses(file, file, ret, WEB_INF_CLASSES, null);
            for (TFile jar : ret.getDependencies()) {
                loadClasses(jar, jar, ret, null, proprietary);
            }
        }
        return ret;
    }

    public ScannerClassLoader load(Distribution distribution) throws Exception {
        ScannerClassLoader ret = null;
        List<URL> jarURLs = new ArrayList<URL>();
        for (TFile jar : distribution.getDependencies()) {
            jarURLs.add(jar.toURL());
        }
        URL[] urls = jarURLs.toArray(new URL[] {});
        ret = new ScannerClassLoader(urls, null);
        return ret;
    }

    public void buildHierarchy(ScannerClassLoader classLoader, ClassDef classDef, String proprietary) throws Exception {
        Class<?> superClass = classDef.getClassObj().getSuperclass();
        while (null != superClass) {
            if (!superClass.getName().matches(proprietary) && !superClass.getName().matches(COMMON_PACKAGES)) {
                classDef.getFilteredHierarchy().put(superClass, getJar(classLoader, superClass));
            }
            classDef.getHierarchy().add(superClass);
            superClass = superClass.getSuperclass();
        }
    }

    public void buildDependencies(ScannerClassLoader classLoader, List<ClassDef> classDefs, String proprietary) throws Exception {
        for (ClassDef classDef : classDefs) {
            buildHierarchy(classLoader, classDef, proprietary);
        }
    }

    public List<ClassDef> analyze(Distribution distribution, ScannerClassLoader classLoader, String proprietary) throws Exception {
        List<ClassDef> ret = new ArrayList<ClassDef>();
        Class<?> loadedClass = null;
        for (String className : distribution.getClasses().keySet()) {
            loadedClass = classLoader.loadClass(className);
            ret.add(new ClassDef(loadedClass, getJar(classLoader, loadedClass)));
        }
        buildDependencies(classLoader, ret, proprietary);
        return ret;
    }

    public Map<URL, String> findLicense(Collection<URL> jars) {
        Map<URL, String> ret = new HashMap<URL, String>();
        return ret;
    }

    public void scan(File file, String proprietary) throws Exception {
        String proprietaryPath = ".*?" + proprietary.replace("\\.", "[\\\\\\/]+") + ".*?$";
        Distribution distribution = load(file, proprietaryPath);
        ScannerClassLoader scannerClassLoader = null;
        try {
            scannerClassLoader = load(distribution);
            List<ClassDef> classDefs = analyze(distribution, scannerClassLoader, proprietary + ".*?$");
            HashSet<String> externals = new HashSet<String>();
            for (ClassDef classDef : classDefs) {
                for (Class<?> external : classDef.getFilteredHierarchy().keySet()) {
                    externals.add(classDef.getFilteredHierarchy().get(external).toString());
                }
            }
        } finally {
            if (null != scannerClassLoader) {
                scannerClassLoader.close();
            }
        }
    }

    protected String cleanClassName(File classDef, File root) {
        return classDef.getAbsolutePath().replace(root.getAbsolutePath(), "").replace('\\', '.').replaceAll(CLASS, "").substring(1);
    }

    protected void loadClasses(File root, File file, Distribution softwarePackage, String child, String pathInclusion) {
        TFile classes = new TFile(null == child ? file : new File(file, child));
        if (classes.exists()) {
            for (TFile classDef : classes.listFiles(null == pathInclusion ? new SimpleFileNameFilter(FULL_CLASS) : new SimpleFileNameFilter(FULL_CLASS, ".*?" + pathInclusion + ".*?$"))) {
                softwarePackage.getClasses().put(cleanClassName(classDef, root), classDef);
            }
            for (TFile fileDef : classes.listFiles()) {
                if (fileDef.isDirectory()) {
                    loadClasses(root, fileDef, softwarePackage, child, pathInclusion);
                }
            }
        }
    }

    protected void loadJars(File file, Distribution softwarePackage, String child) {
        TFile jars = new TFile(null == child ? file : new File(file, child));
        if (jars.exists()) {
            for (TFile jar : jars.listFiles(new SimpleFileNameFilter(JAR))) {
                softwarePackage.getDependencies().add(jar);
            }
        }
    }

}
