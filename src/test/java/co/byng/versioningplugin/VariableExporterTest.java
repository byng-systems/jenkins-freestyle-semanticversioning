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
package co.byng.versioningplugin;

import hudson.model.AbstractBuild;
import java.lang.reflect.Field;
import java.util.Map;
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
public class VariableExporterTest {
    
    private Map<String, String> envVars;
    private AddEnvVarsAction addEnvVarsAction;
    private VariableExporter varExporter;
    
    @Before
    public void setUp() {
        this.envVars = mock(Map.class);
        this.addEnvVarsAction = new AddEnvVarsAction(this.envVars);
        this.varExporter = new VariableExporter(this.addEnvVarsAction);
    }
    
    @Test
    public void testConstructorAssignsInjectedValue() {
        try {
            
            Field actionProperty = VariableExporter.class.getDeclaredField("action");
            actionProperty.setAccessible(true);

            assertSame(this.addEnvVarsAction, actionProperty.get(this.varExporter));
            
        } catch (Throwable t) {
            fail(
                "Exception occurred attempt to access protected field 'envVars' value: "
                    + t.getClass().getSimpleName()
                    + " - "
                    + t.getMessage()
            );
        }
    }

    @Test
    public void testAddVariableToExport() {
        String name = "";
        String value = "";
        
        when(this.envVars.put(eq(name), eq(value))).thenReturn(value);
        
        this.varExporter.addVariableToExport(name, value);
        
        verify(this.envVars, times(1)).put(eq(name), eq(value));
    }

    /**
     * Test of export method, of class VariableExporter.
     */
    @Test
    public void testExport() {
        AbstractBuild build = mock(AbstractBuild.class);
        
        doNothing().when(build).replaceAction(this.addEnvVarsAction);
        
        this.varExporter.export(build);
        
        verify(build, times(1)).replaceAction(this.addEnvVarsAction);
    }
    
}
