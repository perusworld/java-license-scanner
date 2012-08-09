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
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    protected ArtifactFinder artifactFinder = new CentralRepoArtifactFinder();

    protected LicenseFinder licenseFinder = new CentralRepoLicenseFinder();

    /**
     * 
     */
    public LicenseScanner() {
    }

    /**
     * @return the artifactFinder
     */
    public ArtifactFinder getArtifactFinder() {
        return artifactFinder;
    }

    /**
     * @param artifactFinder
     *            the artifactFinder to set
     */
    public void setArtifactFinder(ArtifactFinder artifactFinder) {
        this.artifactFinder = artifactFinder;
    }

    /**
     * @return the licenseFinder
     */
    public LicenseFinder getLicenseFinder() {
        return licenseFinder;
    }

    /**
     * @param licenseFinder
     *            the licenseFinder to set
     */
    public void setLicenseFinder(LicenseFinder licenseFinder) {
        this.licenseFinder = licenseFinder;
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

    public Map<URL, Artifact> findArtifacts(Collection<URL> jars) throws Exception {
        Map<URL, Artifact> ret = new HashMap<URL, Artifact>();
        Artifact artifact = null;
        for (URL jar : jars) {
            artifact = artifactFinder.findArtifact(jar);
            if (null != artifact) {
                licenseFinder.getLicenses(artifact);
            }
            ret.put(jar, artifact);
        }
        return ret;
    }

    public void scan(File file, String proprietary, Set<License> filterLicenses) throws Exception {
        String proprietaryPath = ".*?" + proprietary.replace("\\.", "[\\\\\\/]+") + ".*?$";
        Distribution distribution = load(file, proprietaryPath);
        ScannerClassLoader scannerClassLoader = null;
        try {
            scannerClassLoader = load(distribution);
            List<ClassDef> classDefs = analyze(distribution, scannerClassLoader, proprietary + ".*?$");
            HashSet<URL> externals = new HashSet<URL>();
            for (ClassDef classDef : classDefs) {
                externals.addAll(classDef.getFilteredHierarchy().values());
            }
            Map<URL, Artifact> externalLicense = findArtifacts(externals);
            Artifact artifact = null;
            for (URL jar : externalLicense.keySet()) {
                artifact = externalLicense.get(jar);
                if (null != artifact) {
                    for (ClassDef classDef : classDefs) {
                        for (Class<?> classObj : classDef.getFilteredHierarchy().keySet()) {
                            classDef.getFilteredArtifact().put(classObj, externalLicense.get(classDef.getFilteredHierarchy().get(classObj)));
                        }
                    }
                }
            }
            boolean hasFiltered = false;
            for (ClassDef classDef : classDefs) {
                hasFiltered = false;
                for (Class<?> classObj : classDef.getFilteredArtifact().keySet()) {
                    artifact = classDef.getFilteredArtifact().get(classObj);
                    if (null == artifact) {
                        hasFiltered = true;
                        break;
                    } else {
                        for (License license : filterLicenses) {
                            if (artifact.getLicenses().contains(license)) {
                                hasFiltered = true;
                                break;
                            }
                        }
                    }
                    if (hasFiltered) {
                        break;
                    }
                }
                if (hasFiltered) {
                    System.out.println("Processing " + classDef.getClassObj().getName());
                    for (Class<?> classObj : classDef.getFilteredArtifact().keySet()) {
                        artifact = classDef.getFilteredArtifact().get(classObj);
                        System.out.println(classObj.getName() + " - " + (null == artifact ? "Unknown licenses" : artifact.getLicenses()));
                    }
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
