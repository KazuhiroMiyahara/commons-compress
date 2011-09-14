/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.commons.compress.compressors.pack200;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Pack200;

/**
 * Utility methods for Pack200.
 *
 * @ThreadSafe
 * @since Apache Commons Compress 1.3
 */
public class Pack200Utils {
    private Pack200Utils() { }

    /**
     * Normalizes a JAR archive so it can be safely signed and packed.
     *
     * <p>As stated in <a
     * href="http://download.oracle.com/javase/1.5.0/docs/api/java/util/jar/Pack200.Packer.html">Pack200.Packer's</a>
     * javadocs applying a Pack200 compression to a JAR archive will
     * in general make its sigantures invalid.  In order to prepare a
     * JAR for signing it should be "normalized" by packing and
     * unpacking it.  This is what this method does.</p>
     *
     * <p>This method does not replace the existing archive but creates
     * a new one.</p>
     *
     * @param from the JAR archive to normalize
     * @param to the normalized archive
     * @param props properties to set for the pack operation.  This
     * method will implicitly set the segment limit to -1.
     */
    public static void normalize(File from, File to, Map<String, String> props)
        throws IOException {
        props.put(Pack200.Packer.SEGMENT_LIMIT, "-1");
        File f = File.createTempFile("commons-compress", "pack200normalize");
        f.deleteOnExit();
        try {
            OutputStream os = new FileOutputStream(f);
            try {
                Pack200.Packer p = Pack200.newPacker();
                p.properties().putAll(props);
                p.pack(new JarFile(from), os);
                os.close();
                os = null;

                Pack200.Unpacker u = Pack200.newUnpacker();
                os = new JarOutputStream(new FileOutputStream(to));
                u.unpack(f, (JarOutputStream) os);
            } finally {
                if (os != null) {
                    os.close();
                }
            }
        } finally {
            f.delete();
        }
    }
}