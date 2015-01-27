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

import co.byng.versioningplugin.handler.file.PropertyFileVersionHandler;
import com.github.zafarkhaja.semver.ParseException;
import com.github.zafarkhaja.semver.Version;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/**
 *
 * @author matt
 */
public class AutoCreatingPropertyFileVersionHandler extends PropertyFileVersionHandler {

    public static final String DEFAULT_DEFAULT_VERSION_STRING = "1.0.0";
    
    private String defaultVersionString;

    public AutoCreatingPropertyFileVersionHandler(PropertyFileIoHandler fileHandler, File propertyFilePath, String propertyKey) throws IOException {
        super(fileHandler, propertyFilePath, propertyKey);
    }

    public AutoCreatingPropertyFileVersionHandler(PropertyFileIoHandler propertyFileTool, File propertyPath) throws IOException {
        super(propertyFileTool, propertyPath);
    }

    public AutoCreatingPropertyFileVersionHandler(PropertyFileIoHandler fileHandler) throws IOException {
        super(fileHandler);
    }

    @Override
    public PropertyFileVersionHandler setPropertyFilePath(File propertyFilePath) throws IOException {
        if (propertyFilePath != null && !propertyFilePath.exists()) {
            propertyFilePath.createNewFile();
        }
        
        return super.setPropertyFilePath(propertyFilePath);
    }
    
    @Override
    public Version loadVersion() throws IOException {
        if (!this.getPropertyFilePath().exists()) {
            throw new FileNotFoundException("");
        }
        
        return super.loadVersion(); //To change body of generated methods, choose Tools | Templates.
    }
    
    
        
}
