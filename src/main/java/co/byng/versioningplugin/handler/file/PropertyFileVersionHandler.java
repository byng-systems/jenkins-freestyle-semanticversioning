/*
 * The MIT License
 *
 * Copyright 2015 matt.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package co.byng.versioningplugin.handler.file;

import co.byng.versioningplugin.handler.VersionCommittable;
import co.byng.versioningplugin.handler.VersionRetrievable;
import co.byng.versioningplugin.versioning.VersionFactory;
import com.github.zafarkhaja.semver.Version;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/**
 *
 * @author matt
 */
public class PropertyFileVersionHandler implements VersionRetrievable, VersionCommittable {

    /**
     * 
     */
    public static final String DEFAULT_PROPERTY_KEY = "version";
    
    /**
     * 
     */
    protected PropertyFileIoHandler fileHandler;
    
    /**
     * 
     */
    protected VersionFactory versionFactory;
    
    /**
     * 
     */
    protected File propertyFilePath;
    
    /**
     * 
     */
    protected String propertyKey;
    
    
    
    /**
     * 
     * @param fileHandler
     * @param versionFactory
     * @param propertyFilePath
     * @param propertyKey
     */
    public PropertyFileVersionHandler(
        PropertyFileIoHandler fileHandler,
        VersionFactory versionFactory,
        File propertyFilePath,
        String propertyKey
    ) {
        this.fileHandler = fileHandler;
        this.versionFactory = versionFactory;
        this.propertyFilePath = propertyFilePath;
        this.propertyKey = propertyKey;
    }
    
    /**
     * 
     * @param fileHandler
     * @param propertyPath
     */
    public PropertyFileVersionHandler(
        PropertyFileIoHandler fileHandler,
        VersionFactory versionFactory,
        File propertyPath
    ) {
        this(fileHandler, versionFactory, propertyPath, DEFAULT_PROPERTY_KEY);
    }
    
    /**
     * 
     * @param fileHandler
     */
    public PropertyFileVersionHandler(
        PropertyFileIoHandler fileHandler,
        VersionFactory versionFactory
    ) {
        this(fileHandler, versionFactory, null);
    }

    /**
     * 
     * @return 
     */
    public PropertyFileIoHandler getFileHandler() {
        return fileHandler;
    }

    /**
     * 
     * @param fileHandler
     * @return 
     */
    public PropertyFileVersionHandler setFileHandler(PropertyFileIoHandler fileHandler) {
        this.fileHandler = fileHandler;
        
        return this;
    }

    /**
     * 
     * @return 
     */
    public VersionFactory getVersionFactory() {
        return versionFactory;
    }

    /**
     * 
     * @param versionFactory
     * @return 
     */
    public PropertyFileVersionHandler setVersionFactory(VersionFactory versionFactory) {
        this.versionFactory = versionFactory;
        
        return this;
    }

    /**
     * 
     * @return 
     */
    public File getPropertyFilePath() {
        return propertyFilePath;
    }

    /**
     * 
     * @param propertyFilePath
     * @return
     */
    public PropertyFileVersionHandler setPropertyFilePath(
        File propertyFilePath
    ) throws IOException {
        this.propertyFilePath = propertyFilePath;
        
        return this;
    }
    
    /**
     * 
     * @return 
     */
    public String getPropertyKey() {
        return propertyKey;
    }

    /**
     * 
     * @param propertyKey
     * @return 
     */
    public PropertyFileVersionHandler setPropertyKey(String propertyKey) {
        this.propertyKey = propertyKey;
        
        return this;
    }

    @Override
    public Version loadVersion() throws IllegalStateException, IOException {
        
        if (this.propertyFilePath == null) {
            throw new IllegalStateException("Property file path is not set");
        }
        
        if (!this.propertyFilePath.exists()) {
            throw new FileNotFoundException(
                "Property file " + this.propertyFilePath.getName() + " not found at " + this.propertyFilePath.getPath()
            );
        }

        Properties properties = this.fileHandler.loadPropertiesFromFile(propertyFilePath);

        if (properties.containsKey(this.propertyKey)) {
            return this.versionFactory.buildVersionFromString((String) properties.get(this.propertyKey));
        }
        
        return null;
    }
    
    /**
     * 
     * @param version
     * @return
     * @throws IOException 
     */
    @Override
    public boolean saveVersion(Version version) throws IOException {
        Properties properties = new Properties();
        properties.setProperty(this.propertyKey, version.toString());
        
        this.fileHandler.savePropertiesToFile(properties, this.propertyFilePath);
        
        return true;
    }
    
}
