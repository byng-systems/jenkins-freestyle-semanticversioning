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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 *
 * @author M.D.Ward <matthew.ward@byng-systems.com>
 */
public class PropertyFileIoHandlerTest {
    
    private PropertyFileIoHandler ioHandler;
    private Class<? extends InputStream> inputStreamClass;
    private Class<? extends OutputStream> outputStreamClass;
    
    @Before
    public void setUp() {
        this.ioHandler = new PropertyFileIoHandler();
        
        this.inputStreamClass = mock(InputStream.class).getClass();
        this.outputStreamClass = mock(OutputStream.class).getClass();
    }
    
    /**
     * 
     */
    @Test
    public void testSetInputStreamClass() {
        this.ioHandler.setInputStreamClass(this.inputStreamClass);

        assertSame(this.inputStreamClass, this.ioHandler.inputStreamClass);
    }
    
    /**
     * 
     */
    @Test(expected = IllegalArgumentException.class)
    public void testSetInputClassThrowsExceptionForNullValues() {
        this.ioHandler.setInputStreamClass(null);
    }

    /**
     * 
     */
    @Test
    public void testSetOutputStreamClass() {
        this.ioHandler.setOutputStreamClass(this.outputStreamClass);

        assertSame(this.outputStreamClass, this.ioHandler.outputStreamClass);
    }
    
    /**
     * 
     */
    @Test(expected = IllegalArgumentException.class)
    public void testSetOutputClassThrowsExceptionForNullValues() {
        this.ioHandler.setOutputStreamClass(null);
    }

    @Test
    public void testBuildInputStreamClassReturnsStream() throws Exception {
        this.ioHandler.setInputStreamClass(FileConstructorInputStream.class);
        
        assertTrue(this.ioHandler.buildInputStream(mock(File.class)) instanceof FileConstructorInputStream);
    }
    
    @Test(expected = IOException.class)
    public void testBuildInputStreamClassThrowsExceptionIfNoFileConstructor() throws Exception {
        this.ioHandler.setInputStreamClass(EmptyConstructorInputStream.class);
        this.ioHandler.buildInputStream(mock(File.class));
    }
    
    @Test
    public void testBuildOutputStreamClassReturnsStream() throws Exception {
        this.ioHandler.setOutputStreamClass(FileConstructorOutputStream.class);
        
        assertTrue(this.ioHandler.buildOutputStream(mock(File.class)) instanceof FileConstructorOutputStream);
    }
    
    @Test(expected = IOException.class)
    public void testBuildOutputStreamClassThrowsExceptionIfNoFileConstructor() throws Exception {
        this.ioHandler.setOutputStreamClass(EmptyConstructorOutputStream.class);
        this.ioHandler.buildOutputStream(mock(File.class));
    }
    
    @Test
    public void testBuildPropertiesReturnsProperties() {
        assertTrue(this.ioHandler.buildProperties() instanceof Properties);
    }

    /**
     * 
     */
    @Test
    public void testLoadPropertiesFromFile() throws Exception {
        InputStream inputStream = mock(InputStream.class);
        File f = mock(File.class);
        Properties properties = mock(Properties.class);
        
        this.ioHandler = mock(this.ioHandler.getClass());
        when(this.ioHandler.buildInputStream(same(f))).thenReturn(inputStream);
        when(this.ioHandler.buildProperties()).thenReturn(properties);
        when(this.ioHandler.loadPropertiesFromFile(same(f))).thenCallRealMethod();
        
        assertSame(properties, this.ioHandler.loadPropertiesFromFile(f));
        
        verify(this.ioHandler, times(1)).buildInputStream(same(f));
        verify(this.ioHandler, times(1)).buildProperties();
    }

    /**
     * 
     */
    @Test
    public void testSavePropertiesToFile() throws Exception {
        OutputStream outputStream = mock(OutputStream.class);
        File f = mock(File.class);
        Properties properties = mock(Properties.class);

        doNothing().when(properties).store(same(outputStream), eq(""));

        this.ioHandler = mock(this.ioHandler.getClass());
        when(this.ioHandler.buildOutputStream(same(f))).thenReturn(outputStream);
        doCallRealMethod().when(this.ioHandler).savePropertiesToFile(same(properties), same(f));
        
        this.ioHandler.savePropertiesToFile(properties, f);
        
        verify(this.ioHandler, times(1)).buildOutputStream(same(f));
    }
    
}



class EmptyConstructorInputStream extends InputStream {

    public EmptyConstructorInputStream() {
    }

    @Override
    public int read() throws IOException {
        return -1;
    }
    
}



class FileConstructorInputStream extends EmptyConstructorInputStream {

    public final File f;
    
    public FileConstructorInputStream(File f) {
        super();
        
        this.f = f;
    }
    
}



class EmptyConstructorOutputStream extends OutputStream {

    public EmptyConstructorOutputStream() {
    }

    @Override
    public void write(int b) throws IOException {
    }
    
}



class FileConstructorOutputStream extends EmptyConstructorOutputStream {

    public final File f;
    
    public FileConstructorOutputStream(File f) {
        super();
        
        this.f = f;
    }
    
}
