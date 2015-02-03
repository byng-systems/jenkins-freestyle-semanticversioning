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
package co.byng.versioningplugin.versioning;

import com.github.zafarkhaja.semver.ParseException;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author M.D.Ward <matthew.ward@byng-systems.com>
 */
public class StaticVersionFactoryTest {
    
    private StaticVersionFactory versionFactory;
    
    @Before
    public void setUp() {
        this.versionFactory = new StaticVersionFactory();
    }
    
    /**
     * Test of buildVersionFromString method, of class StaticVersionFactory.
     */
    @Test(expected = ParseException.class)
    public void testBuildVersionFromString() {
        String versionString = "1.0.0";
        
        assertEquals(
            versionString,
            this.versionFactory.buildVersionFromString(versionString).toString()
        );
        
        this.versionFactory.buildVersionFromString("RASIDOAJSIFU0UM308RY80NYM80");
    }
    
}
