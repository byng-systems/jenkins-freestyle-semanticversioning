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
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

/**
 *
 * @author M.D.Ward <matthew.ward@byng-systems.com>
 */
public class PropertyFileIoHandler {
    
    /**
     * 
     */
    protected Class<? extends InputStream> inputStreamClass = FileInputStream.class;
    
    /**
     * 
     */
    protected Class<? extends OutputStream> outputStreamClass = FileOutputStream.class;



    /**
     * 
     * @param inputStreamClass 
     */
    public void setInputStreamClass(Class<? extends InputStream> inputStreamClass) {
        if (inputStreamClass == null) {
            throw new IllegalArgumentException("Input stream class cannot be null");
        }
        
        this.inputStreamClass = inputStreamClass;
    }

    /**
     * 
     * @param outputStreamClass 
     */
    public void setOutputStreamClass(Class<? extends OutputStream> outputStreamClass) {
        if (outputStreamClass == null) {
            throw new IllegalArgumentException("Input stream class cannot be null");
        }
        
        this.outputStreamClass = outputStreamClass;
    }
    
    /**
     * 
     * @param f
     * @return
     * @throws IOException 
     */
    protected InputStream buildInputStream(File f) throws IOException, Exception {
        try {
            return (InputStream) this.inputStreamClass.getConstructor(File.class).newInstance(f);
        } catch (NoSuchMethodException ex) {
            throw new IOException(
                "Unable to instanstiate stream; "
                        + this.inputStreamClass.getSimpleName()
                        + " does not have a constructor accepting a single File argument",
                ex
            );
        }
    }
    
    /**
     * 
     * @param f
     * @return
     * @throws IOException 
     */
    protected OutputStream buildOutputStream(File f) throws IOException, Exception {
        try {
            return (OutputStream) this.outputStreamClass.getConstructor(File.class).newInstance(f);
        } catch (NoSuchMethodException ex) {
            throw new IOException(
                "Unable to instanstiate stream; "
                        + this.outputStreamClass.getSimpleName()
                        + " does not have a constructor accepting a single File argument",
                ex
            );
        }
    }
    
    /**
     * 
     * @return 
     */
    protected Properties buildProperties() {
        return new Properties();
    }

    /**
     * 
     * @param f
     * @return
     * @throws IOException 
     */
    public Properties loadPropertiesFromFile(File f) throws IOException {
        Properties properties = this.buildProperties();
        
        try {
            properties.load(this.buildInputStream(f));
            
            return properties;
        } catch (IOException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IOException("Unable to instantiate stream", ex);
        }
    }
    
    /**
     * 
     * @param properties
     * @param f
     * @throws IOException 
     */
    public void savePropertiesToFile(Properties properties, File f) throws IOException {
        try {
            properties.store(this.buildOutputStream(f), "");
        } catch (IOException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IOException("Unable to instantiate stream", ex);
        }
    }
    
}
