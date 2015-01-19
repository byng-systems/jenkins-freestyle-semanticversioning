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
package co.byng.versioningplugin.handler;

import com.github.zafarkhaja.semver.Version;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/**
 *
 * @author matt
 */
public class PropertyFileHandler extends PropertyHandler implements VersionCommittingInterface {

    protected File propertyFilePath;

    public PropertyFileHandler(File propertyFilePath) throws IOException {
        this.setPropertyFilePath(propertyFilePath);
    }
    
    public PropertyFileHandler(String propertyFilePath) throws IOException {
        this.setPropertyFilePath(propertyFilePath);
    }
    
    public File getPropertyFilePath() {
        return propertyFilePath;
    }

    public void setPropertyFilePath(File propertyFilePath) throws IOException {
        if (propertyFilePath.exists()) {
            throw new FileNotFoundException(
                "Property file " + propertyFilePath.getName() + " not found at " + propertyFilePath.getPath()
            );
        }
        
        Properties loadedProperties = new Properties();
        loadedProperties.load(new FileInputStream(propertyFilePath));
        
        super.setProperties(loadedProperties);
        
        this.propertyFilePath = propertyFilePath;
    }
    
    public void setPropertyFilePath(String propertyFilePath) throws IOException {
        this.setPropertyFilePath(new File(propertyFilePath));
    }
    
    @Override
    @Deprecated
    public void setProperties(Properties properties) {
        throw new UnsupportedOperationException("Direct property setting is not supported in this implementation");
    }

    @Override
    public boolean saveVersion(Version version) throws IOException {
        return true;
    }
    
}
