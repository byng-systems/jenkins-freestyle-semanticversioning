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

import co.byng.versioningplugin.versioning.VersionFactory;
import com.github.zafarkhaja.semver.Version;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import static org.mockito.Mockito.*;
import org.mockito.runners.MockitoJUnitRunner;

/**
 *
 * @author M.D.Ward <matthew.ward@byng-systems.com>
 */
@RunWith(MockitoJUnitRunner.class)
public class PropertyFileVersionHandlerTest {
    
    private PropertyFileIoHandler ioHandler;
    private VersionFactory versionFactory;
    private PropertyFileVersionHandler propHandler;
    
    @Before
    public void setUp() {
        this.ioHandler = mock(PropertyFileIoHandler.class);
        this.versionFactory = mock(VersionFactory.class);
        
        this.propHandler = new PropertyFileVersionHandler(
            this.ioHandler,
            this.versionFactory
        );
    }

    @Test
    public void testDefaultPropertyKeyConstructor() {
        File propertyFile = mock(File.class);
        
        this.propHandler = new PropertyFileVersionHandler(
            this.ioHandler,
            this.versionFactory,
            propertyFile
        );

        assertSame(PropertyFileVersionHandler.DEFAULT_PROPERTY_KEY, this.propHandler.getPropertyKey());
        assertSame(propertyFile, this.propHandler.getPropertyFilePath());
    }
    
    /**
     * 
     */
    @Test
    public void testGetAndSetFileHandler() {
        
        assertSame(this.ioHandler, this.propHandler.getFileHandler());
        
        assertSame(this.propHandler, this.propHandler.setFileHandler(null));
        assertNull(this.propHandler.getFileHandler());
    }
    
    /**
     * 
     */
    @Test
    public void testGetAndSetVersionFactory() {
        
        assertSame(this.versionFactory, this.propHandler.getVersionFactory());
 
        assertSame(this.propHandler, this.propHandler.setVersionFactory(null));
        assertNull(this.propHandler.getVersionFactory());
    }
    
    /**
     * 
     * 
     */
    @Test
    public void testGetAndSetPropertyFilePath() throws IOException {
        File propertyFile = mock(File.class);
        
        assertSame(this.propHandler, this.propHandler.setPropertyFilePath(propertyFile));
        assertSame(propertyFile, this.propHandler.getPropertyFilePath());
    }

    /**
     * 
     */
    @Test
    public void testGetAndSetPropertyKey() {
        String propertyKey = "NEW PROPERTY KEY";
        
        assertSame(this.propHandler, this.propHandler.setPropertyKey(propertyKey));
        assertSame(propertyKey, this.propHandler.getPropertyKey());
    }

    /**
     * 
     */
    @Test
    public void testLoadVersion() throws IOException {
        File propertyFile = mock(File.class);
        when(propertyFile.exists()).thenReturn(true);
        
        final String propertyKey = "ARBITRARY PROPERTY KEY";
        final String versionString = "1.2.3.4";
        
        Properties properties = mock(Properties.class);
        when(this.ioHandler.loadPropertiesFromFile(same(propertyFile))).thenReturn(properties);
        when(properties.containsKey(same(propertyKey))).thenReturn(true);
        when(properties.get(same(propertyKey))).thenReturn(versionString);
        
        Version version = mock(Version.class);
        when(this.versionFactory.buildVersionFromString(same(versionString))).thenReturn(version);
        
        assertSame(
            version,
            this.propHandler
                .setPropertyFilePath(propertyFile)
                .setPropertyKey(propertyKey)
                .loadVersion()
        );
        
        verify(propertyFile, times(1)).exists();
        
        verify(this.ioHandler, times(1)).loadPropertiesFromFile(same(propertyFile));
        verify(properties, times(1)).containsKey(same(propertyKey));
        verify(properties, times(1)).get(same(propertyKey));
        
        verify(this.versionFactory, times(1)).buildVersionFromString(same(versionString));
    }
    
    @Test(expected = IllegalStateException.class)
    public void testLoadVersionHandlesNullFile() throws IOException {
        this.propHandler.loadVersion();
    }
    
    @Test(expected = FileNotFoundException.class)
    public void testLoadVersionHandlesNonExistentFile() throws IOException {
        File propertyFile = mock(File.class);
        when(propertyFile.exists()).thenReturn(false);
        
        try {
            this.propHandler.setPropertyFilePath(propertyFile).loadVersion();
        } catch (FileNotFoundException ex) {
            verify(propertyFile, times(1)).exists();
            
            throw ex;
        }
    }
    
    @Test
    public void testLoadVersionReturnsNullIfNoPropertyKey() throws IOException {
        File propertyFile = mock(File.class);
        when(propertyFile.exists()).thenReturn(true);
        
        final String propertyKey = "ARBITRARY PROPERTY KEY";
        
        Properties properties = mock(Properties.class);
        when(this.ioHandler.loadPropertiesFromFile(same(propertyFile))).thenReturn(properties);
        when(properties.containsKey(same(propertyKey))).thenReturn(false);
        
        assertNull(
            this.propHandler
                    .setPropertyFilePath(propertyFile)
                    .setPropertyKey(propertyKey)
                    .loadVersion()
        );
        
        verify(propertyFile, times(1)).exists();
        
        verify(this.ioHandler, times(1)).loadPropertiesFromFile(same(propertyFile));
        verify(properties, times(1)).containsKey(same(propertyKey));
    }

    /**
     * Test of saveVersion method, of class PropertyFileVersionHandler.
     */
    @Test
    public void testSaveVersion() throws Exception {
        File propertyFile = mock(File.class);
        when(propertyFile.exists()).thenReturn(false);
        
        final String propertyKey = "ARBITRARY PROPERTY KEY";
        final String versionString = "VERSION STRING";
        final Version version = mock(Version.class);
        when(version.toString()).thenReturn(versionString);
        
        Properties properties = mock(Properties.class);
        when(properties.setProperty(same(propertyKey), same(versionString))).thenReturn(null);
        doNothing().when(this.ioHandler).savePropertiesToFile(same(properties), same(propertyFile));
        
        assertTrue(this.propHandler.saveVersion(version));
    }
    
}
