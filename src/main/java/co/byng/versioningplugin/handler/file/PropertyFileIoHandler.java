/*
 * The MIT License
 *
 * Copyright 2015 M.D.Ward <matthew.ward@byng-systems.com>.
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 *
 * @author M.D.Ward <matthew.ward@byng-systems.com>
 */
public class PropertyFileIoHandler {
    
    /**
     * 
     * @param f
     * @return
     * @throws IOException 
     */
    public Properties loadPropertiesFromFile(File f) throws IOException {
        Properties properties = new Properties();
        
        properties.load(new FileInputStream(f));
        
        return properties;
    }
    
    /**
     * 
     * @param properties
     * @param f
     * @throws IOException 
     */
    public void savePropertiesToFile(Properties properties, File f) throws IOException {
        properties.store(new FileOutputStream(f), "");
    }
    
}
